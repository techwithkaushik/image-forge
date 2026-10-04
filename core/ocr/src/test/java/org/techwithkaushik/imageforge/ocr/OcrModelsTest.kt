package org.techwithkaushik.imageforge.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

public class OcrModelsTest {
    @Test public fun defaultsUseLatinAndSafeBounds() {
        val options = OcrOptions()
        assertEquals(OcrScript.LATIN, options.script)
        assertEquals(25L * 1024L * 1024L, options.maxInputBytes)
        assertEquals(8192, options.maxDimensionPx)
    }

    @Test public fun invalidBoundsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { OcrOptions(maxInputBytes = 0) }
        assertThrows(IllegalArgumentException::class.java) { OcrOptions(maxDimensionPx = 0) }
    }

    @Test public fun scriptSelectionIsExplicit() {
        assertEquals(OcrScript.DEVANAGARI, OcrOptions(script = OcrScript.DEVANAGARI).script)
    }
}
