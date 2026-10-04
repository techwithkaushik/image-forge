package org.techwithkaushik.imageforge.imageprocessor

import org.junit.Assert.assertEquals
import org.junit.Test

public class ImageProcessingPolicyTest {
    @Test
    public fun defaultPolicyHasProductionSafeBudgets() {
        val policy = ImageProcessingPolicy()
        assertEquals(12_000_000L, policy.maxDecodePixels)
        assertEquals(16_000_000L, policy.maxOutputPixels)
        assertEquals(64L * 1024L * 1024L, policy.maxBitmapBytes)
    }
}
