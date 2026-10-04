package org.techwithkaushik.imageforge.imageprocessor

public data class ImageProcessingPolicy(
    val maxDecodePixels: Long = 12_000_000L,
    val maxOutputPixels: Long = 16_000_000L,
    val maxBitmapBytes: Long = 64L * 1024L * 1024L,
) {
    init {
        require(maxDecodePixels > 0)
        require(maxOutputPixels > 0)
        require(maxBitmapBytes >= 4L)
    }
}
