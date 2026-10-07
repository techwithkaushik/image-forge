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
    private val exif = ExifMetadataHandler { uri -> resolver.openInputStream(uri) }

    override suspend fun process(
        request: ImageProcessingRequest,
        onProgress: (ProcessingProgress) -> Unit,
    ): ForgeResult<ImageArtifact> = withContext(Dispatchers.IO) {
        var outputFile: File? = null
        val ownedBitmaps = ArrayList<Bitmap>(2)
        try {
            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(0, 100, "Inspecting image"))

            val sourceMetadata = exif.inspect(request.input.uri)
            val sourceOrientation = sourceMetadata?.orientation ?: androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
            val rawBounds = readBounds(request.input.uri)
                ?: return@withContext ForgeResult.Failure(

                    ForgeError.InvalidInput("The selected file is not a readable image."),
                )

            val operation = request.operation
            val bounds = orientedBounds(rawBounds, sourceOrientation)
            if (operation is ImageOperation.Crop && !isCropWithinBounds(operation, bounds.first, bounds.second)) {
                return@withContext ForgeResult.Failure(
                    ForgeError.InvalidInput("Crop bounds exceed the source image."),
                )
            }

            if (operation is ImageOperation.PassportPhoto) {
                val requestedPixels = operation.options.preset.widthPx.toLong() * operation.options.preset.heightPx.toLong()
                if (!isWithinDecodeBudget(bounds.first, bounds.second) ||
                    requestedPixels > policy.maxOutputPixels ||
                    requestedPixels > policy.maxBitmapBytes / 4L
                ) {
                    return@withContext ForgeResult.Failure(
                        ForgeError.InvalidInput("Passport processing exceeds the configured bitmap memory budget."),
                    )
                }
            }

            if (operation is ImageOperation.ExtractSignature &&
                !isWithinDecodeBudget(bounds.first, bounds.second) &&
                bounds.first.toLong() * bounds.second.toLong() > policy.maxDecodePixels * 4L
            ) {
                return@withContext ForgeResult.Failure(
                    ForgeError.InvalidInput("Signature extraction requires an input within the configured bitmap memory budget."),
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
                val needsFullDecode = sourceOrientation != androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL &&
                    sourceOrientation != androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED
                if (cropPixels > policy.maxDecodePixels ||
                    cropPixels > policy.maxBitmapBytes / 4L ||
                    (needsFullDecode && !isWithinDecodeBudget(bounds.first, bounds.second))
                ) {
                    return@withContext ForgeResult.Failure(
                        ForgeError.InvalidInput("Requested crop exceeds the configured bitmap memory budget."),
                    )
                }
            }

            if (operation.requiresFullResolution() &&
                !isWithinDecodeBudget(
                    outputWidthFor(operation, bounds.first, bounds.second),
                    outputHeightFor(operation, bounds.first, bounds.second),
                )
            ) {
                return@withContext ForgeResult.Failure(
                    ForgeError.InvalidInput("This editing operation requires a full-resolution bitmap within the configured memory budget."),
                )
            }

            if (operation is ImageOperation.Inspect) {
                return@withContext ForgeResult.Success(
                    ImageArtifact(
                        uri = request.input.uri,
                        metadata = ImageMetadata(
                            width = bounds.first,
                            height = bounds.second,
                            mimeType = request.input.mimeType ?: resolver.getType(request.input.uri).orEmpty(),
                            byteCount = querySize(request.input.uri),
                            metadata = sourceMetadata,
                        ),
                    ),
                )
            }

            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(20, 100, "Decoding image"))

            val decoded = when (operation) {
                is ImageOperation.Resize -> decodeForResize(
                    request.input.uri,
                    rawBounds,
                    operation.width,
                    operation.height,
                )
                is ImageOperation.Crop ->
                    if (sourceOrientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL ||
                        sourceOrientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED
                    ) {
                        decodeCrop(request.input.uri, operation)
                    } else {
                        decodeFullResolution(request.input.uri)
                    }
                is ImageOperation.PassportPhoto -> decodeForTargetBudget(
                    request.input.uri,
                    rawBounds,
                    operation.options.preset.widthPx,
                    operation.options.preset.heightPx,
                )
                is ImageOperation.ExtractSignature,
                is ImageOperation.Convert,
                is ImageOperation.Rotate,
                is ImageOperation.Flip,
                is ImageOperation.ColorAdjust -> decodeSampled(request.input.uri, rawBounds)
                else -> decodeSampled(request.input.uri, rawBounds)
            } ?: return@withContext ForgeResult.Failure(
                ForgeError.InvalidInput("The image could not be decoded within the memory budget."),
            )
            ownedBitmaps += decoded

            val normalized = exif.normalizeBitmap(decoded, sourceOrientation)
            if (normalized !== decoded) {
                ownedBitmaps += normalized
                if (!decoded.isRecycled) decoded.recycle()
            }

            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(55, 100, "Applying operation"))

            val processed = when (operation) {
                is ImageOperation.Resize -> resize(normalized, operation.width, operation.height)
                is ImageOperation.PassportPhoto -> createPassportPhoto(normalized, operation.options)
                is ImageOperation.ExtractSignature -> extractSignature(normalized, operation.options)
                is ImageOperation.Crop -> if (sourceOrientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL ||
                    sourceOrientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED
                ) {
                    normalized
                } else {
                    cropBitmap(normalized, operation)
                }
                is ImageOperation.Convert -> prepareForConversion(normalized, operation.mimeType)
                is ImageOperation.Rotate -> rotate(normalized, operation.degrees)
                is ImageOperation.Flip -> flip(normalized, operation.horizontal, operation.vertical)
                is ImageOperation.ColorAdjust -> adjustColors(
                    normalized,
                    operation.brightness,
                    operation.contrast,
                    operation.saturation,
                )
                is ImageOperation.Compress -> normalized
                ImageOperation.Inspect -> normalized
            }

            if (processed !== decoded) ownedBitmaps += processed
            if (processed !== normalized && !normalized.isRecycled) normalized.recycle()

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
                            metadata = exif.writeMetadata(
                                request.input.uri,
                                compressed.file,
                                operation.mimeType.lowercase(),
                                request.metadataPolicy,
                                orientationAlreadyNormalized = true,
                            ),
                        ),
                    ),
                )
            }

            onProgress(ProcessingProgress(75, 100, "Encoding output"))

            val mimeType = when (operation) {
                is ImageOperation.Convert -> ImageFormatPolicy.normalizeOutputMimeType(operation.mimeType)
                else -> ImageFormatPolicy.normalizeOutputMimeType(
                    request.input.mimeType ?: resolver.getType(request.input.uri) ?: "image/jpeg",
                ).takeIf { ImageFormatPolicy.isSupportedOutput(it) } ?: "image/jpeg"
            }
            val output = encode(processed, mimeType)
            outputFile = output.file
            val metadata = exif.writeMetadata(
                request.input.uri,
                output.file,
                mimeType,
                request.metadataPolicy,
                orientationAlreadyNormalized = true,
            )
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
                        metadata = metadata,
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

    private fun ImageOperation.requiresFullResolution(): Boolean =
        this is ImageOperation.PassportPhoto ||
            this is ImageOperation.ExtractSignature ||
            this is ImageOperation.Convert ||
            this is ImageOperation.Rotate ||
            this is ImageOperation.Flip ||
            this is ImageOperation.ColorAdjust

    private fun outputWidthFor(operation: ImageOperation, sourceWidth: Int, sourceHeight: Int): Int =
        if (operation is ImageOperation.Rotate && (operation.degrees == 90 || operation.degrees == 270)) {
            sourceHeight
        } else {
            sourceWidth
        }

    private fun outputHeightFor(operation: ImageOperation, sourceWidth: Int, sourceHeight: Int): Int =
        if (operation is ImageOperation.Rotate && (operation.degrees == 90 || operation.degrees == 270)) {
            sourceWidth
        } else {
            sourceHeight
        }

    private fun isWithinDecodeBudget(width: Int, height: Int): Boolean {
        val pixels = width.toLong() * height.toLong()
        return pixels <= policy.maxDecodePixels &&
            pixels <= policy.maxBitmapBytes / 4L
    }

    private fun orientedBounds(bounds: BitmapFactory.Options, orientation: Int): Pair<Int, Int> {
        val swaps = orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 ||
            orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 ||
            orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSPOSE ||
            orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSVERSE
        return if (swaps) bounds.outHeight to bounds.outWidth else bounds.outWidth to bounds.outHeight
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

    private fun cropBitmap(source: Bitmap, crop: ImageOperation.Crop): Bitmap =
        Bitmap.createBitmap(source, crop.left, crop.top, crop.width, crop.height)

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

    private fun decodeForTargetBudget(
        uri: Uri,
        bounds: BitmapFactory.Options,
        targetWidth: Int,
        targetHeight: Int,
    ): Bitmap? = decodeForResize(uri, bounds, targetWidth, targetHeight)

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

    private fun rotate(source: Bitmap, degrees: Int): Bitmap {
        val matrix = android.graphics.Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun flip(source: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = android.graphics.Matrix().apply {
            postScale(
                if (horizontal) -1f else 1f,
                if (vertical) -1f else 1f,
                source.width / 2f,
                source.height / 2f,
            )
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun adjustColors(
        source: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float,
    ): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val matrix = android.graphics.ColorMatrix()
        matrix.setSaturation(saturation)
        val scale = contrast
        val translate = (-0.5f * scale + 0.5f + brightness) * 255f
        val contrastMatrix = android.graphics.ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        matrix.postConcat(contrastMatrix)
        Canvas(result).drawBitmap(
            source,
            0f,
            0f,
            Paint(Paint.FILTER_BITMAP_FLAG).apply {
                colorFilter = android.graphics.ColorMatrixColorFilter(matrix)
            },
        )
        return result
    }

    private fun createPassportPhoto(source: Bitmap, options: PassportOptions): Bitmap {
        val targetWidth = options.preset.widthPx
        val targetHeight = options.preset.heightPx
        val sourceRatio = source.width.toDouble() / source.height.toDouble()
        val targetRatio = targetWidth.toDouble() / targetHeight.toDouble()

        val cropWidth: Int
        val cropHeight: Int
        if (sourceRatio > targetRatio) {
            cropHeight = source.height
            cropWidth = (source.height * targetRatio).toInt().coerceAtLeast(1)
        } else {
            cropWidth = source.width
            cropHeight = (source.width / targetRatio).toInt().coerceAtLeast(1)
        }

        val left = ((source.width - cropWidth) / 2).coerceAtLeast(0)
        val top = ((source.height - cropHeight) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
        val resized = if (cropWidth == targetWidth && cropHeight == targetHeight) {
            cropped
        } else {
            Bitmap.createScaledBitmap(cropped, targetWidth, targetHeight, true)
        }
        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        Canvas(result).apply {
            drawColor(options.backgroundArgb)
            drawBitmap(resized, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        }
        if (cropped !== source && cropped !== resized && !cropped.isRecycled) cropped.recycle()
        if (resized !== source && resized !== result && !resized.isRecycled) resized.recycle()
        return result
    }

    private suspend fun extractSignature(source: Bitmap, options: SignatureOptions): Bitmap {
        val width = source.width
        val height = source.height
        var minX = width
        var minY = height
        var maxX = -1
        var maxY = -1
        var inkPixels = 0

        val pixels = IntArray(width)
        for (y in 0 until height) {
            coroutineContext.ensureActive()
            source.getPixels(pixels, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                val color = pixels[x]
                val alpha = Color.alpha(color)
                val luminance = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000
                if (alpha >= 32 && luminance <= options.luminanceThreshold) {
                    inkPixels++
                    minX = minOf(minX, x)
                    minY = minOf(minY, y)
                    maxX = maxOf(maxX, x)
                    maxY = maxOf(maxY, y)
                }
            }
        }

        if (inkPixels < options.minimumInkPixels || maxX < minX || maxY < minY) {
            throw IllegalArgumentException("No usable signature strokes were detected.")
        }

        val padding = options.paddingPx
        val left = (minX - padding).coerceAtLeast(0)
        val top = (minY - padding).coerceAtLeast(0)
        val right = (maxX + padding + 1).coerceAtMost(width)
        val bottom = (maxY + padding + 1).coerceAtMost(height)

        val result = Bitmap.createBitmap(right - left, bottom - top, Bitmap.Config.ARGB_8888)
        Canvas(result).drawColor(Color.WHITE)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        Canvas(result).drawBitmap(source, -left.toFloat(), -top.toFloat(), paint)
        if (options.removeBorderNoise) {
            binarizeSignature(result, options.luminanceThreshold)
        }
        return result
    }

    private suspend fun binarizeSignature(bitmap: Bitmap, threshold: Int) {
        val pixels = IntArray(bitmap.width)
        for (y in 0 until bitmap.height) {
            coroutineContext.ensureActive()
            bitmap.getPixels(pixels, 0, bitmap.width, 0, y, bitmap.width, 1)
            for (x in pixels.indices) {
                val color = pixels[x]
                val luminance = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000
                pixels[x] = if (luminance <= threshold) Color.BLACK else Color.WHITE
            }
            bitmap.setPixels(pixels, 0, bitmap.width, 0, y, bitmap.width, 1)
        }
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
