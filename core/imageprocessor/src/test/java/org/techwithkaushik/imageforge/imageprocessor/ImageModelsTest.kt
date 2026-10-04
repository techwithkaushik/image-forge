package org.techwithkaushik.imageforge.imageprocessor

import android.net.Uri
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ImageModelsTest {
    @Test
    fun resize_requires_positive_dimensions() {
        assertFailsWith<IllegalArgumentException> {
            ImageOperation.Resize(0, 100)
        }
    }

    @Test
    fun crop_requires_positive_dimensions_and_non_negative_origin() {
        assertFailsWith<IllegalArgumentException> {
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
        assertEquals(1234, metadata.byteCount)
    }

    @Test
    fun input_accepts_content_uri() {
        val input = ImageInput(Uri.parse("content://imageforge/test"))

        assertEquals("content://imageforge/test", input.uri.toString())
    }
}
