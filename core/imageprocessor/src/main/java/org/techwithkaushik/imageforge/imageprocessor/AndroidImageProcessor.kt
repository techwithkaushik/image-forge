package org.techwithkaushik.imageforge.imageprocessor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import org.techwithkaushik.imageforge.common.ForgeError
import org.techwithkaushik.imageforge.common.ForgeResult
import org.techwithkaushik.imageforge.common.ProcessingProgress

public class AndroidImageProcessor(
    context: Context,
    private val policy: ImageProcessingPolicy = ImageProcessingPolicy(),
) : ImageProcessor {
    private val resolver = context.applicationContext.contentResolver
    private val cacheDir = File(context.applicationContext.cacheDir, "imageforge-processing")

    override suspend fun process(
        request: ImageProcessingRequest,
        onProgress: (ProcessingProgress) -> Unit,
    ): ForgeResult<ImageArtifact> = withContext(Dispatchers.IO) {
        var outputFile: File? = null
        val ownedBitmaps = ArrayList<Bitmap>(2)
        try {
            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(0, 100, "Inspecting image"))

            val bounds = readBounds(request.input.uri)
                ?: return@withContext ForgeResult.Failure(
                    ForgeError.InvalidInput("The selected file is not a readable image."),
                )

            val operation = request.operation
            if (operation is ImageOperation.Crop && !isCropWithinBounds(operation, bounds.outWidth, bounds.outHeight)) {
                return@withContext ForgeResult.Failure(
                    ForgeError.InvalidInput("Crop bounds exceed the source image."),
                )
            }

            if (operation is ImageOperation.Resize) {
                val requestedPixels = operation.width.toLong() * operation.height.toLong()
                if (requestedPixels > policy.maxOutputPixels ||
                    requestedPixels > policy.maxDecodePixels ||
                    requestedPixels > policy.maxBitmapBytes / 4L
                ) {
                    return@withContext ForgeResult.Failure(
                        ForgeError.InvalidInput("Requested resize exceeds the configured bitmap memory budget."),
                    )
                }
            }

            if (operation is ImageOperation.Crop) {
                val cropPixels = operation.width.toLong() * operation.height.toLong()
                if (cropPixels > policy.maxDecodePixels ||
                    cropPixels > policy.maxBitmapBytes / 4L
                ) {
                    return@withContext ForgeResult.Failure(
                        ForgeError.InvalidInput("Requested crop exceeds the configured bitmap memory budget."),
                    )
                }
            }

            if (operation is ImageOperation.Convert && !isWithinDecodeBudget(bounds.outWidth, bounds.outHeight)) {
                return@withContext ForgeResult.Failure(
                    ForgeError.InvalidInput("Format conversion requires a full-resolution bitmap within the configured memory budget."),
                )
            }

            if (operation is ImageOperation.Inspect) {
                return@withContext ForgeResult.Success(
                    ImageArtifact(
                        uri = request.input.uri,
                        metadata = ImageMetadata(
                            width = bounds.outWidth,
                            height = bounds.outHeight,
                            mimeType = request.input.mimeType ?: resolver.getType(request.input.uri).orEmpty(),
                            byteCount = querySize(request.input.uri),
                        ),
                    ),
                )
            }

            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(20, 100, "Decoding image"))

            val decoded = when (operation) {
                is ImageOperation.Resize -> decodeForResize(
                    request.input.uri,
                    bounds,
                    operation.width,
                    operation.height,
                )
                is ImageOperation.Crop -> decodeCrop(request.input.uri, operation)
                is ImageOperation.Convert -> decodeFullResolution(request.input.uri)
                else -> decodeSampled(request.input.uri, bounds)
            } ?: return@withContext ForgeResult.Failure(
                ForgeError.InvalidInput("The image could not be decoded within the memory budget."),
            )
            ownedBitmaps += decoded

            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(55, 100, "Applying operation"))

            val processed = when (operation) {
                is ImageOperation.Resize -> resize(decoded, operation.width, operation.height)
                is ImageOperation.Crop -> decoded
                is ImageOperation.Convert -> prepareForConversion(decoded, operation.mimeType)
                is ImageOperation.Compress -> decoded
                ImageOperation.Inspect -> decoded
            }

            if (processed !== decoded) ownedBitmaps += processed

            coroutineContext.ensureActive()

            if (operation is ImageOperation.Compress) {
                onProgress(ProcessingProgress(75, 100, "Finding target size"))
                val width = processed.width
                val height = processed.height
                val compressed = compressToTarget(processed, operation)
                    ?: return@withContext ForgeResult.Failure(
                        ForgeError.ProcessingFailed(
                            "The requested target size could not be satisfied exactly or under the target.",
                        ),
                    )
                outputFile = compressed.file
                onProgress(ProcessingProgress(100, 100, "Complete"))
                return@withContext ForgeResult.Success(
                    ImageArtifact(
                        uri = compressed.uri,
                        metadata = ImageMetadata(
                            width = width,
                            height = height,
                            mimeType = operation.mimeType.lowercase(),
                            byteCount = compressed.byteCount,
                        ),
                    ),
                )
            }

            onProgress(ProcessingProgress(75, 100, "Encoding output"))

            val mimeType = when (operation) {
                is ImageOperation.Convert -> ImageFormatPolicy.normalizeOutputMimeType(operation.mimeType)
                else -> when (request.input.mimeType ?: resolver.getType(request.input.uri)) {
                    "image/png", "image/webp", "image/jpeg" -> request.input.mimeType ?: resolver.getType(request.input.uri)!!
                    else -> "image/jpeg"
                }
            }
            val output = encode(processed, mimeType)
            outputFile = output.file
            val outputWidth = processed.width
            val outputHeight = processed.height
            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(100, 100, "Complete"))

            ForgeResult.Success(
                ImageArtifact(
                    uri = output.uri,
                    metadata = ImageMetadata(
                        width = outputWidth,
                        height = outputHeight,
                        mimeType = mimeType,
                        byteCount = output.byteCount,
                    ),
                ),
            )
        } catch (e: CancellationException) {
            outputFile?.delete()
            throw e
        } catch (e: OutOfMemoryError) {
            outputFile?.delete()
            ForgeResult.Failure(ForgeError.ProcessingFailed("Image exceeds the available memory budget.", e))
        } catch (e: IllegalArgumentException) {
            outputFile?.delete()
            ForgeResult.Failure(ForgeError.InvalidInput(e.message ?: "Invalid image processing request."))
        } catch (e: Exception) {
            outputFile?.delete()
            ForgeResult.Failure(ForgeError.ProcessingFailed("Image processing failed.", e))
        } finally {
            ownedBitmaps.distinct().forEach { bitmap ->
                if (!bitmap.isRecycled) bitmap.recycle()
            }
        }
    }

    private fun isWithinDecodeBudget(width: Int, height: Int): Boolean {
        val pixels = width.toLong() * height.toLong()
        return pixels <= policy.maxDecodePixels &&
            pixels <= policy.maxBitmapBytes / 4L
    }

    private fun isCropWithinBounds(
        crop: ImageOperation.Crop,
        sourceWidth: Int,
        sourceHeight: Int,
    ): Boolean {
        val right = crop.left.toLong() + crop.width.toLong()
        val bottom = crop.top.toLong() + crop.height.toLong()
        return crop.left >= 0 &&
            crop.top >= 0 &&
            crop.width > 0 &&
            crop.height > 0 &&
            right <= sourceWidth.toLong() &&
            bottom <= sourceHeight.toLong()
    }

    private fun readBounds(uri: Uri): BitmapFactory.Options? =
        resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.Options().also { options ->
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(input, null, options)
            }.takeIf { it.outWidth > 0 && it.outHeight > 0 }
        }

    private fun decodeSampled(
        uri: Uri,
        bounds: BitmapFactory.Options,
    ): Bitmap? {
        val sample = calculateSampleSize(bounds.outWidth, bounds.outHeight)
        return resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }.let { options -> BitmapFactory.decodeStream(input, null, options) }
        }
    }

    private fun decodeFullResolution(uri: Uri): Bitmap? =
        resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.Options().apply {
                inSampleSize = 1
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }.let { options -> BitmapFactory.decodeStream(input, null, options) }
        }

    @Suppress("DEPRECATION")
    private fun decodeForResize(
        uri: Uri,
        bounds: BitmapFactory.Options,
        targetWidth: Int,
        targetHeight: Int,
    ): Bitmap? {
        val sample = calculateSampleSizeForTarget(
            bounds.outWidth,
            bounds.outHeight,
            targetWidth,
            targetHeight,
        )
        return resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }.let { options -> BitmapFactory.decodeStream(input, null, options) }
        }
    }

    @Suppress("DEPRECATION")
    private fun decodeCrop(
        uri: Uri,
        crop: ImageOperation.Crop,
    ): Bitmap? {
        val regionDecoder = resolver.openInputStream(uri)?.use { input ->
            BitmapRegionDecoder.newInstance(input, false)
        } ?: return null

        return try {
            BitmapFactory.Options().apply {
                inSampleSize = 1
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }.let { options ->
                regionDecoder.decodeRegion(
                    android.graphics.Rect(
                        crop.left,
                        crop.top,
                        crop.left + crop.width,
                        crop.top + crop.height,
                    ),
                    options,
                )
            }
        } finally {
            regionDecoder.recycle()
        }
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (
            width.toLong() / sample * (height.toLong() / sample) > policy.maxDecodePixels ||
            width.toLong() / sample * (height.toLong() / sample) > policy.maxBitmapBytes / 4L
        ) {
            sample *= 2
        }
        return sample
    }

    private fun calculateSampleSizeForTarget(
        width: Int,
        height: Int,
        targetWidth: Int,
        targetHeight: Int,
    ): Int {
        var sample = 1
        while (true) {
            val next = sample * 2
            val decodedWidth = (width + next - 1L) / next
            val decodedHeight = (height + next - 1L) / next
            if (decodedWidth < targetWidth || decodedHeight < targetHeight) break
            sample = next.toInt()
        }
        return sample
    }

    private fun resize(source: Bitmap, width: Int, height: Int): Bitmap {
        if (source.width == width && source.height == height) return source
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    private fun prepareForConversion(source: Bitmap, mimeType: String): Bitmap {
        if (ImageFormatPolicy.normalizeOutputMimeType(mimeType) != ImageFormatPolicy.JPEG) {
            return source
        }

        val flattened = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        Canvas(flattened).apply {
            drawColor(Color.WHITE)
            drawBitmap(source, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        }
        return flattened
    }

    private data class EncodedOutput(
        val file: File,
        val uri: Uri,
        val byteCount: Long,
    )

    private suspend fun compressToTarget(
        bitmap: Bitmap,
        operation: ImageOperation.Compress,
    ): EncodedOutput? {
        var low = 1
        var high = 100
        var best: EncodedOutput? = null
        var bestSize = -1L

        while (low <= high) {
            coroutineContext.ensureActive()
            val quality = (low + high) ushr 1
            val candidate = encode(bitmap, operation.mimeType.lowercase(), quality)
            when {
                candidate.byteCount == operation.targetBytes -> {
                    best?.file?.delete()
                    return candidate
                }
                candidate.byteCount < operation.targetBytes -> {
                    if (candidate.byteCount > bestSize) {
                        best?.file?.delete()
                        best = candidate
                        bestSize = candidate.byteCount
                    } else {
                        candidate.file.delete()
                    }
                    low = quality + 1
                }
                else -> {
                    candidate.file.delete()
                    high = quality - 1
                }
            }
        }

        return when (operation.mode) {
            ImageOperation.CompressionMode.UNDER_TARGET,
            ImageOperation.CompressionMode.CLOSEST_TO_TARGET -> best
            ImageOperation.CompressionMode.EXACT_BYTES -> {
                best?.file?.delete()
                null
            }
        }
    }

    private fun encode(bitmap: Bitmap, mimeType: String, quality: Int = 92): EncodedOutput {
        cacheDir.mkdirs()
        val extension = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/jpeg" -> "jpg"
            else -> throw IllegalArgumentException("Unsupported output MIME type: $mimeType")
        }
        val file = File.createTempFile("forge-", ".$extension", cacheDir)
        val format = when (mimeType) {
            "image/png" -> Bitmap.CompressFormat.PNG
            "image/webp" -> if (android.os.Build.VERSION.SDK_INT >= 30) {
                Bitmap.CompressFormat.WEBP_LOSSLESS
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
            "image/jpeg" -> Bitmap.CompressFormat.JPEG
            else -> throw IllegalArgumentException("Unsupported output MIME type: $mimeType")
        }
        try {
            FileOutputStream(file).use { output ->
                check(bitmap.compress(format, quality, output)) { "Unable to encode image." }
            }
            return EncodedOutput(
                file = file,
                uri = Uri.fromFile(file),
                byteCount = file.length(),
            )
        } catch (e: Throwable) {
            file.delete()
            throw e
        }
    }

    private fun querySize(uri: Uri): Long =
        resolver.query(uri, arrayOf(android.provider.OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (index >= 0 && !cursor.isNull(index)) cursor.getLong(index) else 0L
                } else 0L
            } ?: 0L
}
