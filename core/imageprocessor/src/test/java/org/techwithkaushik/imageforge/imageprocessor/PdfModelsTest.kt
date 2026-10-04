package org.techwithkaushik.imageforge.imageprocessor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

public class PdfModelsTest {
    @Test
    public fun a4HasExpectedPageGeometry() {
        assertEquals(595, PdfPageSize.A4.widthPoints)
        assertEquals(842, PdfPageSize.A4.heightPoints)
    }

    @Test
    public fun letterHasExpectedPageGeometry() {
        assertEquals(612, PdfPageSize.LETTER.widthPoints)
        assertEquals(792, PdfPageSize.LETTER.heightPoints)
    }

    @Test
    public fun orientationHasTwoDeterministicModes() {
        assertTrue(PdfOrientation.values().contains(PdfOrientation.PORTRAIT))
        assertTrue(PdfOrientation.values().contains(PdfOrientation.LANDSCAPE))
    }

    @Test
    public fun fitHasContainAndCoverModes() {
        assertTrue(PdfImageFit.values().contains(PdfImageFit.CONTAIN))
        assertTrue(PdfImageFit.values().contains(PdfImageFit.COVER))
    }

    @Test(expected = IllegalArgumentException::class)
    public fun marginsRejectNegativeValues() {
        PdfMargins(leftPoints = -1)
    }

    @Test
    public fun marginsAcceptZeroAndPositiveValues() {
        val margins = PdfMargins(10, 20, 30, 40)
        assertEquals(10, margins.leftPoints)
        assertEquals(20, margins.topPoints)
        assertEquals(30, margins.rightPoints)
        assertEquals(40, margins.bottomPoints)
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
