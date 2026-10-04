package org.techwithkaushik.imageforge.imageprocessor

import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import org.techwithkaushik.imageforge.common.ForgeError
import org.techwithkaushik.imageforge.common.ForgeResult
import org.techwithkaushik.imageforge.common.ProcessingProgress

public class BatchImageProcessor(
    private val imageProcessor: ImageProcessor,
) {
    public suspend fun process(
        request: BatchProcessingRequest,
        onProgress: (ProcessingProgress) -> Unit = {},
    ): BatchProcessingResult {
        val outcomes = ArrayList<BatchItemOutcome>(request.items.size)

        request.items.forEachIndexed { index, item ->
            coroutineContext.ensureActive()

            var attempts = 0
            var outcome: BatchItemOutcome? = null

            while (attempts <= request.maxRetries && outcome == null) {
                coroutineContext.ensureActive()
                attempts++

                val result = imageProcessor.process(
                    request = request.requestFor(item),
                    onProgress = { progress ->
                        onProgress(
                            BatchProgress(
                                completedItems = index,
                                totalItems = request.items.size,
                                itemProgress = progress,
                                itemId = item.id,
                            ),
                        )
                    },
                )

                when (result) {
                    is ForgeResult.Success -> {
                        outcome = BatchItemOutcome.Success(
                            id = item.id,
                            attempts = attempts,
                            artifact = result.value,
                        )
                    }

                    is ForgeResult.Failure -> {
                        if (result.error is ForgeError.Cancelled) {
                            throw CancellationException("Batch processing cancelled.")
                        }
                        if (attempts > request.maxRetries) {
                            outcome = BatchItemOutcome.Skipped(
                                id = item.id,
                                attempts = attempts,
                                error = result.error,
                            )
                        }
                    }
                }
            }

            outcomes += checkNotNull(outcome)
            onProgress(
                ProcessingProgress(
                    completedSteps = (index + 1) * 100,
                    totalSteps = request.items.size * 100,
                    message = item.id + ": Complete",
                ),
            )
        }

        return BatchProcessingResult(outcomes)
    }
}