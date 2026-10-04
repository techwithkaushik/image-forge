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
        try {
            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(0, 100, "Inspecting image"))

            val bounds = readBounds(request.input.uri)
                ?: return@withContext ForgeResult.Failure(
                    ForgeError.InvalidInput("The selected file is not a readable image."),
                )

            val operation = request.operation
            if (operation is ImageOperation.Crop) {
                if (operation.left + operation.width > bounds.width ||
                    operation.top + operation.height > bounds.height
                ) {
                    return@withContext ForgeResult.Failure(
                        ForgeError.InvalidInput("Crop bounds exceed the source image."),
                    )
                }
            }

            if (operation is ImageOperation.Resize &&
                operation.width.toLong() * operation.height.toLong() > policy.maxOutputPixels
            ) {
                return@withContext ForgeResult.Failure(
                    ForgeError.InvalidInput("Requested output is larger than the configured pixel budget."),
                )
            }

            if (operation is ImageOperation.Inspect) {
                return@withContext ForgeResult.Success(
                    ImageArtifact(
                        uri = request.input.uri,
                        metadata = ImageMetadata(
                            width = bounds.width,
                            height = bounds.height,
                            mimeType = request.input.mimeType ?: resolver.getType(request.input.uri).orEmpty(),
                            byteCount = querySize(request.input.uri),
                        ),
                    ),
                )
            }

            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(20, 100, "Decoding image"))

            val decoded = when (operation) {
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
            processed.recycle()

            coroutineContext.ensureActive()
            onProgress(ProcessingProgress(100, 100, "Complete"))

            ForgeResult.Success(
                ImageArtifact(
                    uri = output.first,
                    metadata = ImageMetadata(
                        width = output.second.width,
                        height = output.second.height,
                        mimeType = mimeType,
                        byteCount = output.third,
                    ),
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: OutOfMemoryError) {
            ForgeResult.Failure(ForgeError.ProcessingFailed("Image exceeds the available memory budget.", e))
        } catch (e: IllegalArgumentException) {
            ForgeResult.Failure(ForgeError.InvalidInput(e.message ?: "Invalid image processing request."))
        } catch (e: Exception) {
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
    private fun decodeCrop(
        uri: Uri,
        crop: ImageOperation.Crop,
        bounds: BitmapFactory.Options,
    ): Bitmap? {
        val regionDecoder = resolver.openInputStream(uri)?.use { input ->
            BitmapRegionDecoder.newInstance(input, false)
        } ?: return null

        return try {
            val sample = calculateSampleSize(crop.width, crop.height)
            BitmapFactory.Options().apply {
                inSampleSize = sample
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
        while (width.toLong() / sample * (height.toLong() / sample) > policy.maxDecodePixels ||
            width.toLong() / sample * (height.toLong() / sample) * 4L > policy.maxBitmapBytes
        ) {
            sample *= 2
        }
        return sample
    }

    private fun resize(source: Bitmap, width: Int, height: Int): Bitmap {
        if (source.width == width && source.height == height) return source
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    private fun encode(bitmap: Bitmap, mimeType: String): Triple<Uri, Bitmap, Long> {
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
        FileOutputStream(file).use { output ->
            check(bitmap.compress(format, 92, output)) { "Unable to encode image." }
        }
        return Triple(Uri.fromFile(file), bitmap, file.length())
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
