package org.techwithkaushik.imageforge.storage

import android.net.Uri
import org.techwithkaushik.imageforge.common.ForgeResult

public interface StorageGateway {
    public suspend fun saveImage(
        source: Uri,
        destination: StorageDestination,
        displayName: String,
        mimeType: String,
    ): ForgeResult<Uri>

    public suspend fun delete(uri: Uri): ForgeResult<Unit>
}

public sealed interface StorageDestination {
    public data object Pictures : StorageDestination
    public data object Downloads : StorageDestination
    public data class SafTree(val treeUri: Uri) : StorageDestination
}