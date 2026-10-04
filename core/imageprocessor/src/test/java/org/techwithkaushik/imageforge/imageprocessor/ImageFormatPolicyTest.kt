package org.techwithkaushik.imageforge.imageprocessor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

public class ImageFormatPolicyTest {
    @Test
    public fun supportedOutputFormatsAreExplicit() {
        assertTrue(ImageFormatPolicy.isSupportedOutput("image/jpeg"))
        assertTrue(ImageFormatPolicy.isSupportedOutput("image/png"))
        assertTrue(ImageFormatPolicy.isSupportedOutput("image/webp"))
    }

    @Test
    public fun outputFormatValidationIsCaseAndWhitespaceTolerant() {
        assertTrue(ImageFormatPolicy.isSupportedOutput(" IMAGE/JPEG "))
    }

    @Test
    public fun unsupportedOutputFormatsAreRejected() {
        assertFalse(ImageFormatPolicy.isSupportedOutput("image/avif"))
        assertFalse(ImageFormatPolicy.isSupportedOutput("image/heic"))
        assertFalse(ImageFormatPolicy.isSupportedOutput(""))
    }
}
