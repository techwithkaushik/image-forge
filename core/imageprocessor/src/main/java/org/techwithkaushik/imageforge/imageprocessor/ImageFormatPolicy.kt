package org.techwithkaushik.imageforge.imageprocessor

public object ImageFormatPolicy {
    public const val JPEG: String = "image/jpeg"
    public const val PNG: String = "image/png"
    public const val WEBP: String = "image/webp"

    public val supportedOutputMimeTypes: Set<String> = setOf(JPEG, PNG, WEBP)

    public fun normalizeOutputMimeType(mimeType: String): String =
        mimeType.trim().lowercase()

    public fun isSupportedOutput(mimeType: String): Boolean =
        normalizeOutputMimeType(mimeType) in supportedOutputMimeTypes
}
