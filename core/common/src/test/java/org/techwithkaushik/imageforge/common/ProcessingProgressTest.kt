package org.techwithkaushik.imageforge.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProcessingProgressTest {
    @Test
    fun fraction_is_calculated_from_steps() {
        assertEquals(0.5f, ProcessingProgress(2, 4).fraction)
    }

    @Test
    fun invalid_progress_is_rejected() {
        assertFailsWith<IllegalArgumentException> {
            ProcessingProgress(5, 4)
        }
    }
}
