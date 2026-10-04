package org.techwithkaushik.imageforge.imageprocessor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

public class PassportSignatureModelsTest {
    @Test
    public fun indiaPassportPresetHasExactDigitalDimensions() {
        val preset = PassportPreset.INDIA_35X45_MM_300_DPI
        assertEquals(413, preset.widthPx)
        assertEquals(531, preset.heightPx)
        assertEquals(300, preset.dpi)
        assertEquals(35.0, preset.widthMm, 0.001)
        assertEquals(45.0, preset.heightMm, 0.001)
    }

    @Test
    public fun twoByTwoPresetIsSquare() {
        val preset = PassportPreset.US_2X2_IN_300_DPI
        assertEquals(preset.widthPx, preset.heightPx)
        assertEquals(600, preset.widthPx)
    }

    @Test
    public fun signatureDefaultsAreConservative() {
        val options = SignatureOptions()
        assertTrue(options.luminanceThreshold in 1..254)
        assertTrue(options.paddingPx <= 64)
        assertTrue(options.minimumInkPixels > 0)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun signatureRejectsInvalidThreshold() {
        SignatureOptions(luminanceThreshold = 255)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun signatureRejectsNegativePadding() {
        SignatureOptions(paddingPx = -1)
    }

    @Test
    public fun passportOperationCarriesPreset() {
        val operation = ImageOperation.PassportPhoto()
        assertEquals(
            PassportPreset.INDIA_35X45_MM_300_DPI,
            operation.options.preset,
        )
    }

    @Test
    public fun signatureOperationCarriesOptions() {
        val options = SignatureOptions(luminanceThreshold = 175, paddingPx = 6)
        assertEquals(
            options,
            ImageOperation.ExtractSignature(options).options,
        )
    }
}
