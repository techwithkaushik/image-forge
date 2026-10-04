package org.techwithkaushik.imageforge.imageprocessor

import android.net.Uri

public data class ImageInput(
    val uri: Uri,
    val mimeType: String? = null,
    val displayName: String? = null,
)

public data class ImageMetadata(
    val width: Int,
    val height: Int,
    val mimeType: String,
    val byteCount: Long,
)

public data class ImageArtifact(
    val uri: Uri,
    val metadata: ImageMetadata,
)

public sealed interface ImageOperation {
    public data object Inspect : ImageOperation
    public data class Resize(val width: Int, val height: Int) : ImageOperation
    public data class Crop(val left: Int, val top: Int, val width: Int, val height: Int) : ImageOperation
    public data class Convert(val mimeType: String) : ImageOperation
}

public data class ImageProcessingRequest(
    val input: ImageInput,
    val operation: ImageOperation,
)