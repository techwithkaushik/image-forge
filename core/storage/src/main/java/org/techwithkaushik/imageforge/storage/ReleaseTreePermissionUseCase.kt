package org.techwithkaushik.imageforge.storage

import android.net.Uri
import org.techwithkaushik.imageforge.common.ForgeResult

public class ReleaseTreePermissionUseCase(
    private val gateway: StorageGateway,
) {
    public suspend operator fun invoke(treeUri: Uri): ForgeResult<Unit> =
        gateway.releaseTreePermission(treeUri)
}
