package org.techwithkaushik.imageforge.ml

import org.techwithkaushik.imageforge.common.ForgeResult

public interface OfflineMlProcessor {
    public suspend fun infer(
        request: OfflineMlRequest,
    ): ForgeResult<OfflineMlResult>
}
