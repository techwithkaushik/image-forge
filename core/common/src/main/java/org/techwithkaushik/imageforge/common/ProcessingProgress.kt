package org.techwithkaushik.imageforge.common

public data class ProcessingProgress(
    val completedSteps: Int,
    val totalSteps: Int,
    val message: String? = null,
) {
    init {
        require(completedSteps >= 0)
        require(totalSteps >= 0)
        require(completedSteps <= totalSteps || totalSteps == 0)
    }

    public val fraction: Float
        get() = if (totalSteps == 0) 0f else completedSteps.toFloat() / totalSteps
}