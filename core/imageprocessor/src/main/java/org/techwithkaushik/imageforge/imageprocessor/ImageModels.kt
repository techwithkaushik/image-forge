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
) {
    init {
        require(width > 0)
        require(height > 0)
        require(mimeType.isNotBlank())
        require(byteCount >= 0)
    }
}

public data class ImageArtifact(
    val uri: Uri,
    val metadata: ImageMetadata,
)

public sealed interface ImageOperation {
    public data object Inspect : ImageOperation
    public data class Resize(val width: Int, val height: Int) : ImageOperation {
        init {
            require(width > 0)
            require(height > 0)
        }
    }
    public data class Crop(val left: Int, val top: Int, val width: Int, val height: Int) : ImageOperation {
        init {
            require(left >= 0)
            require(top >= 0)
            require(width > 0)
            require(height > 0)
        }
    }
    public data class Convert(val mimeType: String) : ImageOperation
}

public data class ImageProcessingRequest(
    val input: ImageInput,
    val operation: ImageOperation,
)