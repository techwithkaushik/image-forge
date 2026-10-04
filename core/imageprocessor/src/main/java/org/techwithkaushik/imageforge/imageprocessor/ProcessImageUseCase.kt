package org.techwithkaushik.imageforge.imageprocessor

import org.techwithkaushik.imageforge.common.ForgeResult
import org.techwithkaushik.imageforge.common.ProcessingProgress

public class ProcessImageUseCase(
    private val repository: ImageRepository,
) {
    public suspend operator fun invoke(
        request: ImageProcessingRequest,
        onProgress: (ProcessingProgress) -> Unit = {},
    ): ForgeResult<ImageArtifact> = repository.process(request, onProgress)
}
