package org.techwithkaushik.imageforge.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

public class ToolCatalogTest {
    @Test
    public fun resizeToolsShareResizeFamily() {
        val pixel = ToolCatalog.definition("Resize Image Pixel")
        val byPixel = ToolCatalog.definition("Resize Image by Pixel")
        val passport = ToolCatalog.definition("Passport Photo Maker")

        assertEquals(ProcessingFamily.RESIZE, pixel.family)
        assertEquals(ProcessingFamily.RESIZE, byPixel.family)
        assertEquals(ProcessingFamily.PASSPORT_ID, passport.family)
        assertNotEquals(pixel.id, byPixel.id)
    }

    @Test
    public fun compressionAndConversionToolsUseSharedFamilies() {
        assertEquals(
            ProcessingFamily.COMPRESSION,
            ToolCatalog.definition("Compress to 50KB").family,
        )
        assertEquals(
            ProcessingFamily.PDF,
            ToolCatalog.definition("JPEG to PDF (Under 500KB)").family,
        )
        assertEquals(
            ProcessingFamily.CONVERSION,
            ToolCatalog.definition("JPEG to JPG").family,
        )
    }

    @Test
    public fun documentToolsHaveDocumentDestination() {
        val pdf = ToolCatalog.definition("Image to PDF")
        val ocr = ToolCatalog.definition("Image to Text (OCR)")

        assertEquals(ToolDestination.DOCUMENTS, pdf.destination)
        assertEquals(ToolDestination.DOCUMENTS, ocr.destination)
        assertTrue(ToolCapability.PDF in pdf.capabilities)
        assertTrue(ToolCapability.OCR in ocr.capabilities)
    }

    @Test
    public fun aiClassificationWinsOverGenericQualityOrResize() {
        assertEquals(
            ProcessingFamily.AI,
            ToolCatalog.definition("AI Photo Enhancer").family,
        )
        assertEquals(
            ProcessingFamily.AI,
            ToolCatalog.definition("Upscale Image With AI").family,
        )
    }

    @Test
    public fun toolIdIsStableForSameTitle() {
        val first = ToolCatalog.definition("Resize Image Pixel")
        val second = ToolCatalog.definition("Resize Image Pixel")

        assertEquals(first.id, second.id)
    }
}
