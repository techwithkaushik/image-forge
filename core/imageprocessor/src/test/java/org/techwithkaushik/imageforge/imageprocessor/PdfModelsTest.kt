package org.techwithkaushik.imageforge.imageprocessor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

public class PdfModelsTest {
    @Test
    public fun a4PortraitUsesExpectedPageGeometry() {
        val request = ImageToPdfRequest(
            inputs = listOf(ImageInput(android.net.Uri.parse("content://image/1"))),
        )
        assertEquals(595, request.pageWidth())
        assertEquals(842, request.pageHeight())
    }

    @Test
    public fun landscapeSwapsPageGeometry() {
        val request = ImageToPdfRequest(
            inputs = listOf(ImageInput(android.net.Uri.parse("content://image/1"))),
            pageSize = PdfPageSize.LETTER,
            orientation = PdfOrientation.LANDSCAPE,
        )
        assertEquals(792, request.pageWidth())
        assertEquals(612, request.pageHeight())
    }

    @Test
    public fun marginsAreAccepted() {
        val request = ImageToPdfRequest(
            inputs = listOf(ImageInput(android.net.Uri.parse("content://image/1"))),
            margins = PdfMargins(10, 20, 30, 40),
        )
        assertTrue(request.pageWidth() > 40)
        assertTrue(request.pageHeight() > 60)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun pdfRequestRejectsEmptyInputs() {
        ImageToPdfRequest(emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    public fun marginsRejectNegativeValues() {
        PdfMargins(leftPoints = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun splitOptionsRejectInvalidDpi() {
        PdfSplitOptions(targetDpi = 35)
    }

    @Test
    public fun splitOptionsDefaultToPng() {
        assertEquals(PdfSplitImageFormat.PNG, PdfSplitOptions().format)
    }
}
