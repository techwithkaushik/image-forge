package org.techwithkaushik.imageforge.ocr

import android.graphics.Rect
import android.net.Uri

public enum class OcrScript { LATIN, DEVANAGARI }

public data class OcrInput(val uri: Uri, val mimeType: String? = null, val displayName: String? = null)

public data class OcrOptions(
    val script: OcrScript = OcrScript.LATIN,
    val maxInputBytes: Long = 25L * 1024L * 1024L,
    val maxDimensionPx: Int = 8192,
) {
    init { require(maxInputBytes > 0); require(maxDimensionPx > 0) }
}

public data class OcrElement(val text: String, val boundingBox: Rect?, val confidence: Float?)
public data class OcrLine(val text: String, val boundingBox: Rect?, val confidence: Float?, val elements: List<OcrElement>)
public data class OcrBlock(val text: String, val boundingBox: Rect?, val confidence: Float?, val lines: List<OcrLine>)
public data class OcrResult(val text: String, val blocks: List<OcrBlock>, val script: OcrScript)

public interface OcrProcessor {
    public suspend fun recognize(input: OcrInput, options: OcrOptions = OcrOptions()):
        org.techwithkaushik.imageforge.common.ForgeResult<OcrResult>
}
