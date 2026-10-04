package org.techwithkaushik.imageforge.imageprocessor

public enum class PassportPreset(
    public val widthPx: Int,
    public val heightPx: Int,
    public val dpi: Int,
    public val widthMm: Double,
    public val heightMm: Double,
) {
    INDIA_35X45_MM_300_DPI(
        widthPx = 413,
        heightPx = 531,
        dpi = 300,
        widthMm = 35.0,
        heightMm = 45.0,
    ),
    US_2X2_IN_300_DPI(
        widthPx = 600,
        heightPx = 600,
        dpi = 300,
        widthMm = 50.8,
        heightMm = 50.8,
    ),
}

public data class PassportOptions(
    val preset: PassportPreset = PassportPreset.INDIA_35X45_MM_300_DPI,
    val backgroundArgb: Int = 0xFFFFFFFF.toInt(),
) {
    init {
        require(backgroundArgb ushr 24 == 0xFF)
    }
}

public data class SignatureOptions(
    val luminanceThreshold: Int = 190,
    val paddingPx: Int = 4,
    val minimumInkPixels: Int = 20,
    val removeBorderNoise: Boolean = true,
) {
    init {
        require(luminanceThreshold in 1..254)
        require(paddingPx in 0..64)
        require(minimumInkPixels > 0)
    }
}
