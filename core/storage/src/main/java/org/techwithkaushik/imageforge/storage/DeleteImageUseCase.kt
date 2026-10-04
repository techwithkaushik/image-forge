package org.techwithkaushik.imageforge.storage

import android.net.Uri
import org.techwithkaushik.imageforge.common.ForgeResult

public class DeleteImageUseCase(
    private val gateway: StorageGateway,
) {
    public suspend operator fun invoke(uri: Uri): ForgeResult<Unit> =
        gateway.delete(uri)
}
