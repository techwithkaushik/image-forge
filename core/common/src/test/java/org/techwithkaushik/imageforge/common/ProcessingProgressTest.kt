package org.techwithkaushik.imageforge.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProcessingProgressTest {
    @Test
    fun fraction_is_calculated_from_steps() {
        assertEquals(0.5f, ProcessingProgress(2, 4).fraction)
    }

    @Test
    fun invalid_progress_is_rejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ProcessingProgress(5, 4)
        }
    }
}
