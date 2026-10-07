package org.techwithkaushik.imageforge.imageprocessor

import android.content.Context
import org.techwithkaushik.imageforge.common.ForgeResult
import org.techwithkaushik.imageforge.common.ProcessingProgress

public class AndroidImageRepository(
    context: Context,
    private val processor: ImageProcessor = AndroidImageProcessor(context),
) : ImageRepository {
    override suspend fun process(
        request: ImageProcessingRequest,
        onProgress: (ProcessingProgress) -> Unit,
    ): ForgeResult<ImageArtifact> = processor.process(request, onProgress)
}
