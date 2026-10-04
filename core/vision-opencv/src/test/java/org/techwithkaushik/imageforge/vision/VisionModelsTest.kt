package org.techwithkaushik.imageforge.vision

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

public class VisionModelsTest {
    @Test
    public fun rejectsInvalidContourBounds() {
        assertThrows(IllegalArgumentException::class.java) {
            ContourDetectionOptions(cannyLowThreshold = 200.0, cannyHighThreshold = 100.0)
        }
    }

    @Test
    public fun acceptsDeterministicDefaults() {
        val options = ContourDetectionOptions()
        assertEquals(64.0, options.minAreaPx, 0.0)
        assertEquals(128, options.maxContours)
    }
}
