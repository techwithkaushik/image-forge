package org.techwithkaushik.imageforge.ocr

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import org.techwithkaushik.imageforge.common.ForgeError
import org.techwithkaushik.imageforge.common.ForgeResult
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public class AndroidOcrProcessor(private val context: Context) : OcrProcessor {
    private val resolver: ContentResolver get() = context.contentResolver

    override suspend fun recognize(input: OcrInput, options: OcrOptions): ForgeResult<OcrResult> = try {
        ensureInputBounds(input.uri, options)
        val image = InputImage.fromFilePath(context, input.uri)
        val recognizer = when (options.script) {
            OcrScript.LATIN -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            OcrScript.DEVANAGARI -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        }
        try {
            ForgeResult.Success(awaitText(recognizer.process(image)).toDomain(options.script))
        } finally {
            recognizer.close()
        }
    } catch (t: Throwable) {
        if (t is kotlinx.coroutines.CancellationException) throw t
        else ForgeResult.Failure(ForgeError.ProcessingFailed(t.message ?: "Offline OCR failed.", t))
    }

    private fun ensureInputBounds(uri: Uri, options: OcrOptions) {
        val length = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        if (length > options.maxInputBytes) throw IllegalArgumentException("OCR input exceeds ${options.maxInputBytes} bytes.")
        resolver.openInputStream(uri)?.use { stream ->
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(stream, null, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IllegalArgumentException("Unsupported or unreadable image.")
            if (bounds.outWidth > options.maxDimensionPx || bounds.outHeight > options.maxDimensionPx) {
                throw IllegalArgumentException("OCR image exceeds ${options.maxDimensionPx}px dimension limit.")
            }
        } ?: throw IllegalArgumentException("Unable to open OCR input.")
    }

    private suspend fun awaitText(task: com.google.android.gms.tasks.Task<Text>): Text =
        suspendCancellableCoroutine { continuation ->
            task.addOnSuccessListener { value -> if (continuation.isActive) continuation.resume(value) }
            task.addOnFailureListener { error -> if (continuation.isActive) continuation.resumeWithException(error) }
        }

    private fun Text.toDomain(script: OcrScript) = OcrResult(text, textBlocks.map { it.toDomain() }, script)
    private fun Text.TextBlock.toDomain() = OcrBlock(text, boundingBox, confidence, lines.map { it.toDomain() })
    private fun Text.Line.toDomain() = OcrLine(text, boundingBox, confidence, elements.map { it.toDomain() })
    private fun Text.Element.toDomain() = OcrElement(text, boundingBox, null)
}
