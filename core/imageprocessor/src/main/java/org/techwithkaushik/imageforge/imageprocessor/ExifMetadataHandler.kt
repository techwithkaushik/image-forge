package org.techwithkaushik.imageforge.imageprocessor

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.InputStream

internal class ExifMetadataHandler(
    private val openInputStream: (Uri) -> InputStream?,
) {
    fun inspect(uri: Uri): ImageMetadataDetails? =
        runCatching {
            openInputStream(uri)?.use { input -> ExifInterface(input).toDetails() }
        }.getOrNull()

    fun normalizeBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        if (orientation == ExifInterface.ORIENTATION_UNDEFINED ||
            orientation == ExifInterface.ORIENTATION_NORMAL
        ) return bitmap

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> matrix.setValues(
                floatArrayOf(0f, 1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f),
            )
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> matrix.setValues(
                floatArrayOf(0f, -1f, 0f, -1f, 0f, 0f, 0f, 0f, 1f),
            )
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true,
        )
    }

    fun writeMetadata(
        sourceUri: Uri,
        outputFile: File,
        outputMimeType: String,
        policy: MetadataPolicy,
        orientationAlreadyNormalized: Boolean,
    ): ImageMetadataDetails {
        if (policy == MetadataPolicy.STRIP_ALL) {
            return ImageMetadataDetails(
                orientation = ExifInterface.ORIENTATION_NORMAL,
                hasGpsLocation = false,
                cameraMake = null,
                cameraModel = null,
                dateTimeOriginal = null,
                warning = "All metadata was stripped by policy.",
            )
        }

        val source = runCatching {
            openInputStream(sourceUri)?.use { ExifInterface(it) }
        }.getOrNull()

        if (source == null || !ExifInterface.isSupportedMimeType(outputMimeType)) {
            return ImageMetadataDetails(
                orientation = ExifInterface.ORIENTATION_NORMAL,
                hasGpsLocation = false,
                cameraMake = null,
                cameraModel = null,
                dateTimeOriginal = null,
                warning = "Metadata preservation is unsupported for this input or output format.",
            )
        }

        val output = runCatching { ExifInterface(outputFile) }.getOrNull()
            ?: return ImageMetadataDetails(
                orientation = ExifInterface.ORIENTATION_NORMAL,
                hasGpsLocation = false,
                cameraMake = null,
                cameraModel = null,
                dateTimeOriginal = null,
                warning = "Output metadata could not be opened for writing.",
            )

        val technicalTags = listOf(
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_ORIGINAL,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_F_NUMBER,
            ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_WHITE_BALANCE,
            ExifInterface.TAG_COLOR_SPACE,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
        )
        technicalTags.forEach { tag ->
            source.getAttribute(tag)?.let { output.setAttribute(tag, it) }
        }

        if (policy == MetadataPolicy.STRIP_SENSITIVE) {
            listOf(
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LATITUDE_REF,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_LONGITUDE_REF,
                ExifInterface.TAG_GPS_ALTITUDE,
                ExifInterface.TAG_GPS_ALTITUDE_REF,
                ExifInterface.TAG_GPS_TIMESTAMP,
                ExifInterface.TAG_GPS_DATESTAMP,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_SOFTWARE,
                ExifInterface.TAG_ARTIST,
                ExifInterface.TAG_COPYRIGHT,
                ExifInterface.TAG_USER_COMMENT,
                ExifInterface.TAG_XMP,
            ).forEach { tag -> output.setAttribute(tag, null) }
        }

        if (orientationAlreadyNormalized) {
            output.setAttribute(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL.toString(),
            )
        }
        output.saveAttributes()

        return ImageMetadataDetails(
            orientation = ExifInterface.ORIENTATION_NORMAL,
            hasGpsLocation = policy == MetadataPolicy.PRESERVE_SUPPORTED && source.latLong != null,
            cameraMake = if (policy == MetadataPolicy.PRESERVE_SUPPORTED) source.getAttribute(ExifInterface.TAG_MAKE) else null,
            cameraModel = if (policy == MetadataPolicy.PRESERVE_SUPPORTED) source.getAttribute(ExifInterface.TAG_MODEL) else null,
            dateTimeOriginal = source.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL),
            preserved = true,
        )
    }

    private fun ExifInterface.toDetails(): ImageMetadataDetails =
        ImageMetadataDetails(
            orientation = getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            ),
            hasGpsLocation = latLong != null,
            cameraMake = getAttribute(ExifInterface.TAG_MAKE),
            cameraModel = getAttribute(ExifInterface.TAG_MODEL),
            dateTimeOriginal = getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL),
        )
}
