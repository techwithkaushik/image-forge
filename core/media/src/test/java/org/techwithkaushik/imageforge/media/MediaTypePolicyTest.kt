package org.techwithkaushik.imageforge.media

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

public class MediaTypePolicyTest {
    @Test
    public fun supportedImageMimeTypesAreAccepted() {
        assertTrue(MediaTypePolicy.isSupportedImageMimeType("image/jpeg"))
        assertTrue(MediaTypePolicy.isSupportedImageMimeType("IMAGE/PNG"))
        assertTrue(MediaTypePolicy.isSupportedImageMimeType("image/webp"))
    }

    @Test
    public fun unsupportedMimeTypesAreRejected() {
        assertFalse(MediaTypePolicy.isSupportedImageMimeType("application/pdf"))
        assertFalse(MediaTypePolicy.isSupportedImageMimeType(null))
    }
}
