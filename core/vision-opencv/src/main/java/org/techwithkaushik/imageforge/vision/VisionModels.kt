package org.techwithkaushik.imageforge.vision

import android.graphics.Rect

public data class VisionPoint(val x: Double, val y: Double)

public data class VisionContour(
    val bounds: Rect,
    val areaPx: Double,
    val perimeterPx: Double,
    val points: List<VisionPoint>,
)

public data class ContourDetectionOptions(
    val minAreaPx: Double = 64.0,
    val maxContours: Int = 128,
    val cannyLowThreshold: Double = 60.0,
    val cannyHighThreshold: Double = 180.0,
) {
    init {
        require(minAreaPx >= 0.0)
        require(maxContours in 1..10_000)
        require(cannyLowThreshold in 0.0..255.0)
        require(cannyHighThreshold in 0.0..255.0)
        require(cannyHighThreshold > cannyLowThreshold)
    }
}

public interface ContourDetector {
    public suspend fun detect(
        rgbaPixels: IntArray,
        width: Int,
        height: Int,
        options: ContourDetectionOptions = ContourDetectionOptions(),
    ): List<VisionContour>
}
