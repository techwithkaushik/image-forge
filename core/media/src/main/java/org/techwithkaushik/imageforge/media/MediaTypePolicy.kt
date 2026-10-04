package org.techwithkaushik.imageforge.media

public object MediaTypePolicy {
    private val supportedImageMimeTypes = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/heic",
        "image/heif",
    )

    public fun isSupportedImageMimeType(mimeType: String?): Boolean =
        mimeType?.lowercase()?.let(supportedImageMimeTypes::contains) == true
}
