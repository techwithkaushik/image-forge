package org.techwithkaushik.imageforge.storage

import android.net.Uri
import org.techwithkaushik.imageforge.common.ForgeResult

public class SaveImageUseCase(
    private val gateway: StorageGateway,
) {
    public suspend operator fun invoke(
        source: Uri,
        destination: StorageDestination,
        displayName: String,
        mimeType: String,
    ): ForgeResult<Uri> = gateway.saveImage(
        source = source,
        destination = destination,
        displayName = displayName,
        mimeType = mimeType,
    )
}
