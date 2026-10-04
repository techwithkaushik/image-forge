package org.techwithkaushik.imageforge.imageprocessor

import org.junit.Assert.assertEquals
import org.junit.Test

public class ImageOperationTest {
    @Test(expected = IllegalArgumentException::class)
    public fun resizeRejectsZeroWidth() {
        ImageOperation.Resize(0, 100)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun cropRejectsZeroHeight() {
        ImageOperation.Crop(0, 0, 100, 0)
    }

    @Test
    public fun compressionDefaultsToUnderTargetJpeg() {
        val operation = ImageOperation.Compress(50_000)
        assertEquals(ImageOperation.CompressionMode.UNDER_TARGET, operation.mode)
        assertEquals("image/jpeg", operation.mimeType)
    }

    @Test
    public fun compressionAcceptsWebpAndClosestMode() {
        val operation = ImageOperation.Compress(
            targetBytes = 50_000,
            mode = ImageOperation.CompressionMode.CLOSEST_TO_TARGET,
            mimeType = "image/webp",
        )
        assertEquals(ImageOperation.CompressionMode.CLOSEST_TO_TARGET, operation.mode)
        assertEquals("image/webp", operation.mimeType)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun compressionRejectsUnsupportedMimeType() {
        ImageOperation.Compress(50_000, mimeType = "image/png")
    }
}
