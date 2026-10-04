package org.techwithkaushik.imageforge.imageprocessor

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ImageModelsTest {
    @Test
    fun resize_requires_positive_dimensions() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageOperation.Resize(0, 100)
        }
    }

    @Test
    fun crop_requires_positive_dimensions_and_non_negative_origin() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageOperation.Crop(-1, 0, 10, 10)
        }
    }

    @Test
    fun metadata_preserves_declared_values() {
        val metadata = ImageMetadata(
            width = 100,
            height = 200,
            mimeType = "image/jpeg",
            byteCount = 1234,
        )

        assertEquals(100, metadata.width)
        assertEquals(200, metadata.height)
        assertEquals("image/jpeg", metadata.mimeType)
        assertEquals(1234L, metadata.byteCount)
    }

}
