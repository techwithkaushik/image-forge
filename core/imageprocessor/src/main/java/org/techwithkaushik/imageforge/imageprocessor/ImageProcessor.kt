package org.techwithkaushik.imageforge.imageprocessor

import org.techwithkaushik.imageforge.common.ForgeResult
import org.techwithkaushik.imageforge.common.ProcessingProgress

public interface ImageProcessor {
    public suspend fun process(
        request: ImageProcessingRequest,
        onProgress: (ProcessingProgress) -> Unit = {},
    ): ForgeResult<ImageArtifact>
}
