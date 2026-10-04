package org.techwithkaushik.imageforge.ml

import org.techwithkaushik.imageforge.common.ForgeError
import org.techwithkaushik.imageforge.common.ForgeResult

public class DisabledOfflineMlProcessor : OfflineMlProcessor {
    override suspend fun infer(request: OfflineMlRequest): ForgeResult<OfflineMlResult> =
        ForgeResult.Failure(
            ForgeError.ProcessingFailed(
                "No offline ML runtime is enabled for model '${request.model.id}'.",
            ),
        )
}
