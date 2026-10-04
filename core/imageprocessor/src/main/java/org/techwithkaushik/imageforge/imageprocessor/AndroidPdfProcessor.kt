package org.techwithkaushik.imageforge.imageprocessor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
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

public class AndroidPdfProcessor(
    context: Context,
    private val policy: ImageProcessingPolicy = ImageProcessingPolicy(),
) : PdfProcessor {
    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver
    private val cacheDir = File(appContext.cacheDir, "imageforge-processing")
    private val exif = ExifMetadataHandler { uri -> resolver.openInputStream(uri) }

    override suspend fun createPdf(
        request: ImageToPdfRequest,
        onProgress: (ProcessingProgress) -> Unit,
    ): ForgeResult<PdfArtifact> = withContext(Dispatchers.IO) {
        var output: File? = null
        val owned = ArrayList<Bitmap>(2)
        val document = PdfDocument()
        try {
            coroutineContext.ensureActive()
            cacheDir.mkdirs()
            onProgress(ProcessingProgress(0, 100, "Preparing PDF"))

            request.inputs.forEachIndexed { index, input ->
                coroutineContext.ensureActive()
                val decoded = decodeForPdf(input.uri)
                    ?: throw IllegalArgumentException("Unable to decode image.")
                owned += decoded

                val pageInfo = PdfDocument.PageInfo.Builder(
                    request.pageWidth(),
                    request.pageHeight(),
                    index + 1,
                ).create()
                val page = document.startPage(pageInfo)
                try {
                    page.canvas.drawColor(request.backgroundArgb)
                    drawBitmapOnPage(page.canvas, decoded, request)
                } finally {
                    document.finishPage(page)
                }

                onProgress(
                    ProcessingProgress(
                        ((index + 1) * 80 / request.inputs.size).coerceAtMost(80),
                        100,
                        "Added page " + (index + 1) + " of " + request.inputs.size,
                    ),
                )
            }

            coroutineContext.ensureActive()
            output = File.createTempFile("forge-", ".pdf", cacheDir)
            FileOutputStream(output).use { document.writeTo(it) }
            coroutineContext.ensureActive()

            val result = PdfArtifact(
                uri = Uri.fromFile(output),
                pageCount = request.inputs.size,
                byteCount = output.length(),
            )
            output = null
            onProgress(ProcessingProgress(100, 100, "Complete"))
            ForgeResult.Success(result)
        } catch (e: CancellationException) {
            output?.delete()
            throw e
        } catch (e: OutOfMemoryError) {
            output?.delete()
            ForgeResult.Failure(
                ForgeError.ProcessingFailed("PDF generation exceeded the available memory budget.", e),
            )
        } catch (e: IllegalArgumentException) {
            output?.delete()
            ForgeResult.Failure(ForgeError.InvalidInput(e.message ?: "Invalid PDF request."))
        } catch (e: Exception) {
            output?.delete()
            ForgeResult.Failure(ForgeError.ProcessingFailed("PDF generation failed.", e))
        } finally {
            document.close()
            owned.distinct().forEach { bitmap ->
                if (!bitmap.isRecycled) bitmap.recycle()
            }
        }
    }

    override suspend fun splitPdf(
        input: ImageInput,
        options: PdfSplitOptions,
        onProgress: (ProcessingProgress) -> Unit,
    ): ForgeResult<PdfSplitArtifact> = withContext(Dispatchers.IO) {
        val outputFiles = ArrayList<File>()
        var descriptor: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            coroutineContext.ensureActive()
            descriptor = resolver.openFileDescriptor(input.uri, "r")
                ?: throw IllegalArgumentException("Unable to open PDF.")
            renderer = PdfRenderer(descriptor!!)

            if (renderer!!.pageCount <= 0) {
                throw IllegalArgumentException("PDF contains no pages.")
            }

            val pages = ArrayList<ImageArtifact>(renderer!!.pageCount)
            for (index in 0 until renderer!!.pageCount) {
                coroutineContext.ensureActive()
                val page = renderer!!.openPage(index)
                var bitmap: Bitmap? = null
                try {
                    val dimensions = calculateRenderSize(page.width, page.height, options)
                    bitmap = Bitmap.createBitmap(
                        dimensions.first,
                        dimensions.second,
                        Bitmap.Config.ARGB_8888,
                    )
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                    val extension = if (options.format == PdfSplitImageFormat.PNG) ".png" else ".jpg"
                    val file = File.createTempFile("forge-page-" + (index + 1) + "-", extension, cacheDir)
                    outputFiles += file
                    encodePage(bitmap, file, options.format)

                    pages += ImageArtifact(
                        uri = Uri.fromFile(file),
                        metadata = ImageMetadata(
                            width = bitmap.width,
                            height = bitmap.height,
                            mimeType = if (options.format == PdfSplitImageFormat.PNG) "image/png" else "image/jpeg",
                            byteCount = file.length(),
                        ),
                    )
                } finally {
                    page.close()
                    bitmap?.recycle()
                }

                onProgress(
                    ProcessingProgress(
                        ((index + 1) * 100 / renderer!!.pageCount).coerceAtMost(100),
                        100,
                        "Rendered page " + (index + 1) + " of " + renderer!!.pageCount,
                    ),
                )
            }

            ForgeResult.Success(PdfSplitArtifact(pages))
        } catch (e: CancellationException) {
            outputFiles.forEach(File::delete)
            throw e
        } catch (e: OutOfMemoryError) {
            outputFiles.forEach(File::delete)
            ForgeResult.Failure(
                ForgeError.ProcessingFailed("PDF page rendering exceeded the available memory budget.", e),
            )
        } catch (e: IllegalArgumentException) {
            outputFiles.forEach(File::delete)
            ForgeResult.Failure(ForgeError.InvalidInput(e.message ?: "Invalid PDF input."))
        } catch (e: Exception) {
            outputFiles.forEach(File::delete)
            ForgeResult.Failure(ForgeError.ProcessingFailed("PDF splitting failed.", e))
        } finally {
            renderer?.close()
            descriptor?.close()
        }
    }

    private fun decodeForPdf(uri: Uri): Bitmap? {
        val bounds = resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.Options().also {
                it.inJustDecodeBounds = true
                BitmapFactory.decodeStream(input, null, it)
            }
        } ?: return null

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val orientation = exif.inspect(uri)?.orientation
            ?: androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
        val swaps = orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 ||
            orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 ||
            orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSPOSE ||
            orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSVERSE

        val width = if (swaps) bounds.outHeight else bounds.outWidth
        val height = if (swaps) bounds.outWidth else bounds.outHeight

        var sample = 1
        while (
            width.toLong() / sample * (height.toLong() / sample) > policy.maxDecodePixels ||
            width.toLong() / sample * (height.toLong() / sample) > policy.maxBitmapBytes / 4L
        ) {
            sample *= 2
        }

        val decoded = resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }.let { BitmapFactory.decodeStream(input, null, it) }
        } ?: return null

        val normalized = exif.normalizeBitmap(decoded, orientation)
        if (normalized !== decoded && !decoded.isRecycled) decoded.recycle()

        val maxPixels = minOf(policy.maxOutputPixels, policy.maxBitmapBytes / 4L)
        val pixels = normalized.width.toLong() * normalized.height.toLong()
        if (pixels <= maxPixels) return normalized

        val scale = kotlin.math.sqrt(maxPixels.toDouble() / pixels.toDouble())
        val targetWidth = (normalized.width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (normalized.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(normalized, targetWidth, targetHeight, true)
        if (scaled !== normalized && !normalized.isRecycled) normalized.recycle()
        return scaled
    }

    private fun drawBitmapOnPage(
        canvas: Canvas,
        bitmap: Bitmap,
        request: ImageToPdfRequest,
    ) {
        val left = request.margins.leftPoints.toFloat()
        val top = request.margins.topPoints.toFloat()
        val right = (request.pageWidth() - request.margins.rightPoints).toFloat()
        val bottom = (request.pageHeight() - request.margins.bottomPoints).toFloat()
        val availableWidth = right - left
        val availableHeight = bottom - top
        val sourceRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val targetRatio = availableWidth / availableHeight

        val sourceRect: Rect?
        val destination: RectF

        when (request.fit) {
            PdfImageFit.CONTAIN -> {
                sourceRect = null
                val scale = minOf(
                    availableWidth / bitmap.width,
                    availableHeight / bitmap.height,
                )
                val width = bitmap.width * scale
                val height = bitmap.height * scale
                destination = RectF(
                    left + (availableWidth - width) / 2f,
                    top + (availableHeight - height) / 2f,
                    left + (availableWidth + width) / 2f,
                    top + (availableHeight + height) / 2f,
                )
            }
            PdfImageFit.COVER -> {
                destination = RectF(left, top, right, bottom)
                sourceRect = if (sourceRatio > targetRatio) {
                    val cropWidth = (bitmap.height * targetRatio).toInt().coerceAtLeast(1)
                    val cropLeft = (bitmap.width - cropWidth) / 2
                    Rect(cropLeft, 0, cropLeft + cropWidth, bitmap.height)
                } else {
                    val cropHeight = (bitmap.width / targetRatio).toInt().coerceAtLeast(1)
                    val cropTop = (bitmap.height - cropHeight) / 2
                    Rect(0, cropTop, bitmap.width, cropTop + cropHeight)
                }
            }
        }

        canvas.drawBitmap(
            bitmap,
            sourceRect,
            destination,
            Paint(Paint.FILTER_BITMAP_FLAG),
        )
    }

    private fun calculateRenderSize(
        widthPoints: Int,
        heightPoints: Int,
        options: PdfSplitOptions,
    ): Pair<Int, Int> {
        val scale = options.targetDpi / 72f
        var width = (widthPoints * scale).toInt().coerceAtLeast(1)
        var height = (heightPoints * scale).toInt().coerceAtLeast(1)
        val pixels = width.toLong() * height.toLong()
        if (pixels > options.maxOutputPixels) {
            val factor = kotlin.math.sqrt(options.maxOutputPixels.toDouble() / pixels.toDouble())
            width = (width * factor).toInt().coerceAtLeast(1)
            height = (height * factor).toInt().coerceAtLeast(1)
        }
        return width to height
    }

    private fun encodePage(
        bitmap: Bitmap,
        file: File,
        format: PdfSplitImageFormat,
    ) {
        val compressFormat = if (format == PdfSplitImageFormat.PNG) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
        FileOutputStream(file).use { output ->
            check(
                bitmap.compress(
                    compressFormat,
                    if (format == PdfSplitImageFormat.JPEG) 92 else 100,
                    output,
                ),
            )
        }
    }
}
