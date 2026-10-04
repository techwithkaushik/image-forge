package org.techwithkaushik.imageforge.imageprocessor

import org.techwithkaushik.imageforge.common.ForgeResult

public interface ImageRepository {
    public suspend fun process(
        request: ImageProcessingRequest,
        onProgress: (org.techwithkaushik.imageforge.common.ProcessingProgress) -> Unit = {},
    ): ForgeResult<ImageArtifact>
}
