package org.techwithkaushik.imageforge.imageprocessor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
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
        try {
            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(0, 100, "Inspecting image"))

            val bounds = readBounds(request.input.uri)
                ?: return@withContext ForgeResult.Failure(
                    ForgeError.InvalidInput("The selected file is not a readable image."),
                )

            val operation = request.operation
            if (operation is ImageOperation.Crop) {
                if (operation.left + operation.width > bounds.outWidth ||
                    operation.top + operation.height > bounds.outHeight
                ) {
                    return@withContext ForgeResult.Failure(
                        ForgeError.InvalidInput("Crop bounds exceed the source image."),
                    )
                }
            }

            if (operation is ImageOperation.Resize) {
                val requestedPixels = operation.width.toLong() * operation.height.toLong()
                if (requestedPixels > policy.maxOutputPixels ||
                    requestedPixels > policy.maxDecodePixels ||
                    requestedPixels * 4L > policy.maxBitmapBytes
                ) {
                    return@withContext ForgeResult.Failure(
                        ForgeError.InvalidInput("Requested resize exceeds the configured bitmap memory budget."),
                    )
                }
            }

            if (operation is ImageOperation.Crop) {
                val cropPixels = operation.width.toLong() * operation.height.toLong()
                if (cropPixels > policy.maxDecodePixels ||
                    cropPixels * 4L > policy.maxBitmapBytes
                ) {
                    return@withContext ForgeResult.Failure(
                        ForgeError.InvalidInput("Requested crop exceeds the configured bitmap memory budget."),
                    )
                }
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
                is ImageOperation.Crop -> decodeCrop(request.input.uri, operation, bounds)
                else -> decodeSampled(request.input.uri, bounds)
            } ?: return@withContext ForgeResult.Failure(
                ForgeError.InvalidInput("The image could not be decoded within the memory budget."),
            )

            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(55, 100, "Applying operation"))

            val processed = when (operation) {
                is ImageOperation.Resize -> resize(decoded, operation.width, operation.height)
                is ImageOperation.Crop -> decoded
                is ImageOperation.Convert -> decoded
                ImageOperation.Inspect -> decoded
            }

            if (processed !== decoded) decoded.recycle()

            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(75, 100, "Encoding output"))

            val mimeType = when (operation) {
                is ImageOperation.Convert -> operation.mimeType.lowercase()
                else -> when (request.input.mimeType ?: resolver.getType(request.input.uri)) {
                    "image/png", "image/webp", "image/jpeg" -> request.input.mimeType ?: resolver.getType(request.input.uri)!!
                    else -> "image/jpeg"
                }
            }
            val output = encode(processed, mimeType)
            outputFile = output.file
            val outputWidth = processed.width
            val outputHeight = processed.height
            processed.recycle()

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
        }
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
        bounds: BitmapFactory.Options,
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
            width.toLong() / sample * (height.toLong() / sample) * 4L > policy.maxBitmapBytes
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

    private data class EncodedOutput(
        val file: File,
        val uri: Uri,
        val byteCount: Long,
    )

    private fun encode(bitmap: Bitmap, mimeType: String): EncodedOutput {
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
                check(bitmap.compress(format, 92, output)) { "Unable to encode image." }
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
