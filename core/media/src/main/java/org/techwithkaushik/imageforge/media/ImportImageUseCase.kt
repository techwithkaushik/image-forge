package org.techwithkaushik.imageforge.media

import android.net.Uri
import org.techwithkaushik.imageforge.common.ForgeResult

public class ImportImageUseCase(
    private val gateway: MediaGateway,
) {
    public suspend operator fun invoke(source: Uri): ForgeResult<MediaImage> =
        gateway.importImage(source)
}
