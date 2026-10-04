package org.techwithkaushik.imageforge.imageprocessor

import org.techwithkaushik.imageforge.common.ForgeError
import org.techwithkaushik.imageforge.common.ProcessingProgress

public data class BatchImageInput(
    val id: String,
    val input: ImageInput,
) {
    init {
        require(id.isNotBlank()) { "Batch item id must not be blank." }
    }
}

public data class BatchProcessingRequest(
    val items: List<BatchImageInput>,
    val operation: ImageOperation,
    val metadataPolicy: MetadataPolicy = MetadataPolicy.PRESERVE_SUPPORTED,
    val maxRetries: Int = 0,
) {
    init {
        require(items.isNotEmpty()) { "Batch must contain at least one image." }
        require(items.map(BatchImageInput::id).distinct().size == items.size) {
            "Batch item ids must be unique."
        }
        require(maxRetries in 0..3) { "maxRetries must be between 0 and 3." }
    }

    public fun requestFor(item: BatchImageInput): ImageProcessingRequest =
        ImageProcessingRequest(
            input = item.input,
            operation = operation,
            metadataPolicy = metadataPolicy,
        )
}

public sealed interface BatchItemOutcome {
    public val id: String
    public val attempts: Int

    public data class Success(
        override val id: String,
        override val attempts: Int,
        val artifact: ImageArtifact,
    ) : BatchItemOutcome

    public data class Skipped(
        override val id: String,
        override val attempts: Int,
        val error: ForgeError,
    ) : BatchItemOutcome
}

public data class BatchProcessingResult(
    val outcomes: List<BatchItemOutcome>,
) {
    public val successes: List<BatchItemOutcome.Success>
        get() = outcomes.filterIsInstance<BatchItemOutcome.Success>()

    public val skipped: List<BatchItemOutcome.Skipped>
        get() = outcomes.filterIsInstance<BatchItemOutcome.Skipped>()

    public val completedCount: Int
        get() = outcomes.size

    public val successCount: Int
        get() = successes.size

    public val skippedCount: Int
        get() = skipped.size

    public val artifactsForSaveAll: List<ImageArtifact>
        get() = successes.map(BatchItemOutcome.Success::artifact)
}

internal fun BatchProgress(
    completedItems: Int,
    totalItems: Int,
    itemProgress: ProcessingProgress,
    itemId: String,
): ProcessingProgress {
    val totalSteps = totalItems * 100
    val completedSteps = (completedItems * 100 + itemProgress.completedSteps).coerceAtMost(totalSteps)
    return ProcessingProgress(
        completedSteps = completedSteps,
        totalSteps = totalSteps,
        message = itemId + ": " + (itemProgress.message ?: "Processing"),
    )
}