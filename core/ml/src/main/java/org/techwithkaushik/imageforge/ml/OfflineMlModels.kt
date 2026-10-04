package org.techwithkaushik.imageforge.ml

public data class OfflineMlModel(
    val id: String,
    val inputMimeType: String,
    val outputDescription: String,
) {
    init {
        require(id.isNotBlank()) { "Model id must not be blank." }
        require(inputMimeType.isNotBlank()) { "Input MIME type must not be blank." }
        require(outputDescription.isNotBlank()) { "Output description must not be blank." }
    }
}

public data class OfflineMlRequest(
    val model: OfflineMlModel,
    val input: ByteArray,
) {
    init {
        require(input.isNotEmpty()) { "ML input must not be empty." }
    }
}

public data class OfflineMlScore(
    val label: String,
    val score: Float,
) {
    init {
        require(label.isNotBlank()) { "Score label must not be blank." }
        require(score in 0f..1f) { "Score must be between 0 and 1." }
    }
}

public data class OfflineMlResult(
    val modelId: String,
    val scores: List<OfflineMlScore>,
    val metadata: Map<String, String> = emptyMap(),
)
