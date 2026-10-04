package org.techwithkaushik.imageforge.vision

import android.graphics.Bitmap
import android.graphics.Rect
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

public class AndroidOpenCvContourDetector : ContourDetector {
    override suspend fun detect(
        rgbaPixels: IntArray,
        width: Int,
        height: Int,
        options: ContourDetectionOptions,
    ): List<VisionContour> {
        require(width > 0 && height > 0)
        require(rgbaPixels.size == width * height)

        if (!OpenCVLoader.initLocal()) {
            error("OpenCV native library initialization failed.")
        }

        currentCoroutineContext().ensureActive()

        val sourceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val source = Mat()
        val gray = Mat()
        val edges = Mat()
        val hierarchy = Mat()
        val contours = ArrayList<MatOfPoint>()

        try {
            sourceBitmap.setPixels(rgbaPixels, 0, width, 0, 0, width, height)
            Utils.bitmapToMat(sourceBitmap, source)
            currentCoroutineContext().ensureActive()

            Imgproc.cvtColor(source, gray, Imgproc.COLOR_RGBA2GRAY)
            Imgproc.GaussianBlur(gray, gray, Size(3.0, 3.0), 0.0)
            Imgproc.Canny(gray, edges, options.cannyLowThreshold, options.cannyHighThreshold)
            currentCoroutineContext().ensureActive()

            Imgproc.findContours(
                edges,
                contours,
                hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE,
            )
            currentCoroutineContext().ensureActive()

            val result = ArrayList<VisionContour>(minOf(contours.size, options.maxContours))
            for (contour in contours) {
                currentCoroutineContext().ensureActive()

                val area = Imgproc.contourArea(contour)
                if (area < options.minAreaPx) continue

                val bounds = Imgproc.boundingRect(contour)
                val points = contour.toArray()
                val curve = MatOfPoint2f(*points)
                val perimeter = try {
                    Imgproc.arcLength(curve, true)
                } finally {
                    curve.release()
                }

                result += VisionContour(
                    bounds = Rect(
                        bounds.x,
                        bounds.y,
                        bounds.x + bounds.width,
                        bounds.y + bounds.height,
                    ),
                    areaPx = area,
                    perimeterPx = perimeter,
                    points = points.map { VisionPoint(it.x, it.y) },
                )
            }

            return result
                .sortedByDescending { it.areaPx }
                .take(options.maxContours)
        } finally {
            contours.forEach(MatOfPoint::release)
            hierarchy.release()
            edges.release()
            gray.release()
            source.release()
            sourceBitmap.recycle()
        }
    }
}
