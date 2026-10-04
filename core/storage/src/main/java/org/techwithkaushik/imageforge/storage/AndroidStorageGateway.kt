package org.techwithkaushik.imageforge.storage

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.techwithkaushik.imageforge.common.ForgeError
import org.techwithkaushik.imageforge.common.ForgeResult

public class AndroidStorageGateway(
    context: Context,
) : StorageGateway {
    private val resolver: ContentResolver = context.applicationContext.contentResolver

    override suspend fun saveImage(
        source: Uri,
        destination: StorageDestination,
        displayName: String,
        mimeType: String,
    ): ForgeResult<Uri> = withContext(Dispatchers.IO) {
        try {
            if (displayName.isBlank()) {
                return@withContext ForgeResult.Failure(ForgeError.InvalidInput("A non-empty display name is required."))
            }
            if (mimeType.isBlank()) {
                return@withContext ForgeResult.Failure(ForgeError.InvalidInput("A MIME type is required."))
            }

            val safeName = sanitizeDisplayName(displayName)
            val target = when (destination) {
                StorageDestination.Pictures -> createMediaStoreTarget(
                    collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    displayName = safeName,
                    mimeType = mimeType,
                    relativePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        "${Environment.DIRECTORY_PICTURES}/ImageForge"
                    } else {
                        null
                    },
                )
                StorageDestination.Downloads -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        return@withContext ForgeResult.Failure(
                            ForgeError.StorageFailed("Public Downloads export requires Android 10 or newer."),
                        )
                    }
                    createMediaStoreTarget(
                        collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        displayName = safeName,
                        mimeType = mimeType,
                        relativePath = "${Environment.DIRECTORY_DOWNLOADS}/ImageForge",
                    )
                }
                is StorageDestination.SafTree -> createSafTarget(
                    treeUri = destination.treeUri,
                    displayName = safeName,
                    mimeType = mimeType,
                )
            }

            if (target is ForgeResult.Failure) return@withContext target

            val targetUri = (target as ForgeResult.Success).value
            try {
                resolver.openInputStream(source).use { input ->
                    if (input == null) {
                        return@withContext ForgeResult.Failure(ForgeError.StorageFailed("Unable to read the source image."))
                    }
                    resolver.openOutputStream(targetUri, "w").use { output ->
                        if (output == null) {
                            return@withContext ForgeResult.Failure(
                                ForgeError.StorageFailed("Unable to open the destination for writing."),
                            )
                        }
                        input.copyTo(output)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetUri.authority == MediaStore.AUTHORITY) {
                    resolver.update(
                        targetUri,
                        ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                        null,
                        null,
                    )
                }
                ForgeResult.Success(targetUri)
            } catch (e: CancellationException) {
                resolver.delete(targetUri, null, null)
                throw e
            } catch (e: Exception) {
                resolver.delete(targetUri, null, null)
                ForgeResult.Failure(ForgeError.StorageFailed("Unable to save the image.", e))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            ForgeResult.Failure(ForgeError.StorageFailed("Storage permission was not granted.", e))
        } catch (e: Exception) {
            ForgeResult.Failure(ForgeError.StorageFailed("Unable to create the destination image.", e))
        }
    }

    override suspend fun delete(uri: Uri): ForgeResult<Unit> = withContext(Dispatchers.IO) {
        try {
            if (resolver.delete(uri, null, null) > 0) ForgeResult.Success(Unit)
            else ForgeResult.Failure(ForgeError.StorageFailed("The image could not be deleted."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            ForgeResult.Failure(ForgeError.StorageFailed("Storage permission was not granted.", e))
        } catch (e: Exception) {
            ForgeResult.Failure(ForgeError.StorageFailed("Unable to delete the image.", e))
        }
    }

    override suspend fun persistTreePermission(treeUri: Uri): ForgeResult<Unit> =
        withContext(Dispatchers.IO) {
            try {
                resolver.takePersistableUriPermission(
                    treeUri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
                ForgeResult.Success(Unit)
            } catch (e: SecurityException) {
                ForgeResult.Failure(
                    ForgeError.StorageFailed("The selected folder does not allow persistent access.", e),
                )
            } catch (e: Exception) {
                ForgeResult.Failure(ForgeError.StorageFailed("Unable to persist folder access.", e))
            }
        }

    override fun hasPersistedTreePermission(treeUri: Uri): Boolean =
        resolver.persistedUriPermissions.any { it.uri == treeUri && it.isWritePermission }

    private fun createMediaStoreTarget(
        collection: Uri,
        displayName: String,
        mimeType: String,
        relativePath: String?,
    ): ForgeResult<Uri> {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && relativePath != null) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(collection, values)
            ?: return ForgeResult.Failure(ForgeError.StorageFailed("Android could not create the destination image."))
        return ForgeResult.Success(uri)
    }

    private fun createSafTarget(
        treeUri: Uri,
        displayName: String,
        mimeType: String,
    ): ForgeResult<Uri> {
        if (!hasPersistedTreePermission(treeUri)) {
            return ForgeResult.Failure(ForgeError.StorageFailed("The selected folder is no longer accessible."))
        }
        val uri = DocumentsContract.createDocument(resolver, treeUri, mimeType, displayName)
            ?: return ForgeResult.Failure(
                ForgeError.StorageFailed("The selected folder could not create the image."),
            )
        return ForgeResult.Success(uri)
    }

    private fun sanitizeDisplayName(displayName: String): String =
        displayName.replace('/', '_').replace('\\', '_').trim().ifBlank { "image" }
}
