package org.techwithkaushik.imageforge.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

public class OfflineMlModelsTest {
    @Test
    public fun modelRejectsBlankId() {
        assertThrows(IllegalArgumentException::class.java) {
            OfflineMlModel("", "image/jpeg", "classification")
        }
    }

    @Test
    public fun scoreRejectsOutOfRangeValue() {
        assertThrows(IllegalArgumentException::class.java) {
            OfflineMlScore("photo", 1.1f)
        }
    }

    @Test
    public fun resultPreservesModelIdAndScores() {
        val score = OfflineMlScore("photo", 0.9f)
        val result = OfflineMlResult("photo-detector", listOf(score))
        assertEquals("photo-detector", result.modelId)
        assertEquals(listOf(score), result.scores)
    }
}
