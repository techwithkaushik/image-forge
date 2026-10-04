package org.techwithkaushik.imageforge.imageprocessor

import org.techwithkaushik.imageforge.common.ForgeResult

public interface ImageProcessor {
    public suspend fun process(request: ImageProcessingRequest): ForgeResult<ImageArtifact>
}