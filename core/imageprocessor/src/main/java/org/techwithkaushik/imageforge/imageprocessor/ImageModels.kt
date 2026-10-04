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
    val metadata: ImageMetadataDetails? = null,
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

    public data class Convert(val mimeType: String) : ImageOperation {
        init {
            require(ImageFormatPolicy.isSupportedOutput(mimeType))
        }
    }

    public data class Rotate(val degrees: Int) : ImageOperation {
        init {
            require(degrees == 90 || degrees == 180 || degrees == 270)
        }
    }

    public data class Flip(
        val horizontal: Boolean = false,
        val vertical: Boolean = false,
    ) : ImageOperation {
        init {
            require(horizontal || vertical)
        }
    }

    public data class ColorAdjust(
        val brightness: Float = 0f,
        val contrast: Float = 1f,
        val saturation: Float = 1f,
    ) : ImageOperation {
        init {
            require(brightness in -1f..1f)
            require(contrast in 0f..2f)
            require(saturation in 0f..2f)
        }
    }

    public enum class CompressionMode {
        UNDER_TARGET,
        CLOSEST_TO_TARGET,
        EXACT_BYTES,
    }

    public data class Compress(
        val targetBytes: Long,
        val mode: CompressionMode = CompressionMode.UNDER_TARGET,
        val mimeType: String = "image/jpeg",
    ) : ImageOperation {
        init {
            require(targetBytes > 0)
            require(mimeType.lowercase() == "image/jpeg" || mimeType.lowercase() == "image/webp")
        }
    }
}

public data class ImageProcessingRequest(
    val input: ImageInput,
    val operation: ImageOperation,
    val metadataPolicy: MetadataPolicy = MetadataPolicy.PRESERVE_SUPPORTED,
)
