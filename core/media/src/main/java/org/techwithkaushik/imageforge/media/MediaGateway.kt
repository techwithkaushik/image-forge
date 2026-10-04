package org.techwithkaushik.imageforge.media

import android.net.Uri
import org.techwithkaushik.imageforge.common.ForgeResult

public interface MediaGateway {
    public suspend fun importImage(source: Uri): ForgeResult<MediaImage>
}

public data class MediaImage(
    val uri: Uri,
    val displayName: String?,
    val mimeType: String?,
    val byteCount: Long,
)