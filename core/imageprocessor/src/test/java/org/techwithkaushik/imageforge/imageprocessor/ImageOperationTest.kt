package org.techwithkaushik.imageforge.imageprocessor

import org.junit.Assert.assertEquals
import org.junit.Test

public class ImageOperationTest {
    @Test(expected = IllegalArgumentException::class)
    public fun resizeRejectsZeroWidth() {
        ImageOperation.Resize(0, 100)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun resizeRejectsZeroHeight() {
        ImageOperation.Resize(100, 0)
    }

    @Test
    public fun resizeAcceptsPositiveExactDimensions() {
        val operation = ImageOperation.Resize(1920, 1080)
        assertEquals(1920, operation.width)
        assertEquals(1080, operation.height)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun cropRejectsZeroHeight() {
        ImageOperation.Crop(0, 0, 100, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun cropRejectsNegativeLeft() {
        ImageOperation.Crop(-1, 0, 100, 100)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun cropRejectsNegativeTop() {
        ImageOperation.Crop(0, -1, 100, 100)
    }

    @Test
    public fun cropAcceptsPositiveGeometry() {
        val operation = ImageOperation.Crop(10, 20, 640, 480)
        assertEquals(10, operation.left)
        assertEquals(20, operation.top)
        assertEquals(640, operation.width)
        assertEquals(480, operation.height)
    }

    @Test
    public fun convertAcceptsSupportedOutputFormats() {
        assertEquals("image/jpeg", ImageOperation.Convert("image/jpeg").mimeType)
        assertEquals("image/png", ImageOperation.Convert("image/png").mimeType)
        assertEquals("image/webp", ImageOperation.Convert("image/webp").mimeType)
    }

    @Test
    public fun convertNormalizesOutputMimeType() {
        assertEquals("image/jpeg", ImageOperation.Convert(" IMAGE/JPEG ").mimeType)
    }

    @Test(expected = IllegalArgumentException::class)
    public fun convertRejectsUnsupportedOutputFormat() {
        ImageOperation.Convert("image/avif")
    }

    @Test(expected = IllegalArgumentException::class)
    public fun convertRejectsBlankOutputFormat() {
        ImageOperation.Convert(" ")
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
