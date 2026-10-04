package org.techwithkaushik.imageforge.imageprocessor

import android.net.Uri
import org.techwithkaushik.imageforge.common.ForgeResult
import org.techwithkaushik.imageforge.common.ProcessingProgress

public enum class PdfPageSize(public val widthPoints: Int, public val heightPoints: Int) {
    A4(595, 842),
    LETTER(612, 792),
}

public enum class PdfOrientation { PORTRAIT, LANDSCAPE }

public enum class PdfImageFit { CONTAIN, COVER }

public data class PdfMargins(
    val leftPoints: Int = 24,
    val topPoints: Int = 24,
    val rightPoints: Int = 24,
    val bottomPoints: Int = 24,
) {
    init {
        require(leftPoints >= 0 && topPoints >= 0 && rightPoints >= 0 && bottomPoints >= 0)
    }
}

public data class ImageToPdfRequest(
    val inputs: List<ImageInput>,
    val pageSize: PdfPageSize = PdfPageSize.A4,
    val orientation: PdfOrientation = PdfOrientation.PORTRAIT,
    val margins: PdfMargins = PdfMargins(),
    val fit: PdfImageFit = PdfImageFit.CONTAIN,
    val backgroundArgb: Int = 0xFFFFFFFF.toInt(),
) {
    init {
        require(inputs.isNotEmpty())
        require(backgroundArgb ushr 24 == 0xFF)
        require(pageWidth() > margins.leftPoints + margins.rightPoints)
        require(pageHeight() > margins.topPoints + margins.bottomPoints)
    }

    public fun pageWidth(): Int =
        if (orientation == PdfOrientation.PORTRAIT) pageSize.widthPoints else pageSize.heightPoints

    public fun pageHeight(): Int =
        if (orientation == PdfOrientation.PORTRAIT) pageSize.heightPoints else pageSize.widthPoints
}

public data class PdfArtifact(
    val uri: Uri,
    val pageCount: Int,
    val byteCount: Long,
)

public enum class PdfSplitImageFormat { PNG, JPEG }

public data class PdfSplitOptions(
    val format: PdfSplitImageFormat = PdfSplitImageFormat.PNG,
    val targetDpi: Int = 144,
    val maxOutputPixels: Long = 12_000_000L,
) {
    init {
        require(targetDpi in 36..300)
        require(maxOutputPixels > 0)
    }
}

public data class PdfSplitArtifact(
    val pages: List<ImageArtifact>,
) {
    init { require(pages.isNotEmpty()) }
}

public interface PdfProcessor {
    public suspend fun createPdf(
        request: ImageToPdfRequest,
        onProgress: (ProcessingProgress) -> Unit = {},
    ): ForgeResult<PdfArtifact>

    public suspend fun splitPdf(
        input: ImageInput,
        options: PdfSplitOptions = PdfSplitOptions(),
        onProgress: (ProcessingProgress) -> Unit = {},
    ): ForgeResult<PdfSplitArtifact>
}
