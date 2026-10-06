package org.techwithkaushik.imageforge.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
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

        assertEquals(ToolDestination.EDITOR, pdf.destination)
        assertEquals(ToolDestination.EDITOR, ocr.destination)
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
    public fun imageToolsExposeSharedFlexibleCropByDefault() {
        val tools = listOf(
            "Resize Image Pixel",
            "Compress to 50KB",
            "JPEG to JPG",
            "Rotate Image",
            "Blur Image",
            "View Metadata",
        )

        tools.forEach { title ->
            val definition = ToolCatalog.definition(title)
            assertEquals(ToolType.IMAGE, definition.toolType)
            assertTrue(ToolCapability.CROP in definition.capabilities)
            assertEquals(CropMode.FLEXIBLE, definition.configuration.crop.mode)
            assertTrue(definition.configuration.crop.allowZoom)
            assertTrue(definition.configuration.crop.allowPan)
        }
    }

    @Test
    public fun fixedSizeToolsLockCropFrameButAllowImagePositioning() {
        val definition = ToolCatalog.definition("35mm x 45mm")

        assertEquals(FunctionType.FIXED_SIZE_CROP_RESIZE, definition.functionType)
        assertEquals(CropMode.FIXED, definition.configuration.crop.mode)
        assertEquals(DimensionUnit.MM, definition.configuration.crop.unit)
        assertEquals(35.0, definition.configuration.crop.width!!, 0.0)
        assertEquals(45.0, definition.configuration.crop.height!!, 0.0)
        assertTrue(definition.configuration.crop.allowZoom)
        assertTrue(definition.configuration.crop.allowPan)
        assertFalse(definition.configuration.crop.allowFrameResize)
    }

    @Test
    public fun noCropSocialPresetDisablesCropExplicitly() {
        val definition = ToolCatalog.definition("Instagram (No Crop)")

        assertEquals(CropMode.DISABLED, definition.configuration.crop.mode)
        assertFalse(ToolCapability.CROP in definition.capabilities)
    }

    @Test
    public fun pdfAndDocumentToolsDoNotExposeImageCrop() {
        val pdf = ToolCatalog.definition("Image to PDF")
        val ocr = ToolCatalog.definition("Image to Text (OCR)")

        assertEquals(ToolType.PDF, pdf.toolType)
        assertEquals(CropMode.DISABLED, pdf.configuration.crop.mode)
        assertEquals(ToolType.DOCUMENT, ocr.toolType)
        assertEquals(CropMode.DISABLED, ocr.configuration.crop.mode)
    }

    @Test
    public fun toolIdIsStableForSameTitle() {
        val first = ToolCatalog.definition("Resize Image Pixel")
        val second = ToolCatalog.definition("Resize Image Pixel")

        assertEquals(first.id, second.id)
    }
}
