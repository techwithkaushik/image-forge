package org.techwithkaushik.imageforge.imageprocessor

public enum class MetadataPolicy {
    PRESERVE_SUPPORTED,
    STRIP_SENSITIVE,
    STRIP_ALL,
}

public data class ImageMetadataDetails(
    val orientation: Int,
    val hasGpsLocation: Boolean,
    val cameraMake: String?,
    val cameraModel: String?,
    val dateTimeOriginal: String?,
    val preserved: Boolean = false,
    val warning: String? = null,
) {
    init {
        require(orientation >= 0)
    }
}
