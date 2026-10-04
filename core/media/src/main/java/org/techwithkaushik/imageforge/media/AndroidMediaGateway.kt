package org.techwithkaushik.imageforge.media

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.techwithkaushik.imageforge.common.ForgeError
import org.techwithkaushik.imageforge.common.ForgeResult

public class AndroidMediaGateway(
    context: Context,
) : MediaGateway {
    private val resolver: ContentResolver = context.applicationContext.contentResolver

    override suspend fun importImage(source: Uri): ForgeResult<MediaImage> = withContext(Dispatchers.IO) {
        try {
            val mimeType = resolver.getType(source)
            if (!MediaTypePolicy.isSupportedImageMimeType(mimeType)) {
                return@withContext ForgeResult.Failure(ForgeError.UnsupportedFormat(mimeType))
            }

            var displayName: String? = null
            var byteCount = -1L

            resolver.query(
                source,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                        displayName = cursor.getString(nameIndex)
                    }
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        byteCount = cursor.getLong(sizeIndex)
                    }
                }
            }

            resolver.openInputStream(source)?.use { input ->
                if (input.read() < 0) {
                    return@withContext ForgeResult.Failure(
                        ForgeError.InvalidInput("The selected image is empty."),
                    )
                }
            } ?: return@withContext ForgeResult.Failure(
                ForgeError.StorageFailed("Unable to open the selected image."),
            )

            ForgeResult.Success(
                MediaImage(
                    uri = source,
                    displayName = displayName,
                    mimeType = mimeType,
                    byteCount = byteCount.coerceAtLeast(0L),
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            ForgeResult.Failure(ForgeError.StorageFailed("Storage permission was not granted.", e))
        } catch (e: Exception) {
            ForgeResult.Failure(ForgeError.StorageFailed("Unable to inspect the selected image.", e))
        }
    }
}
