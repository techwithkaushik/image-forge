package org.techwithkaushik.imageforge.imageprocessor

import org.junit.Assert.assertEquals
import org.junit.Test

public class MetadataModelsTest {
    @Test
    public fun defaultPolicyPreservesSupportedMetadata() {
        val request = ImageProcessingRequest(
            input = ImageInput(android.net.Uri.parse("content://example/image")),
            operation = ImageOperation.Inspect,
        )
        assertEquals(MetadataPolicy.PRESERVE_SUPPORTED, request.metadataPolicy)
    }

    @Test
    public fun sensitivePolicyIsExplicit() {
        assertEquals(
            MetadataPolicy.STRIP_SENSITIVE,
            ImageProcessingRequest(
                input = ImageInput(android.net.Uri.parse("content://example/image")),
                operation = ImageOperation.Convert("image/jpeg"),
                metadataPolicy = MetadataPolicy.STRIP_SENSITIVE,
            ).metadataPolicy,
        )
    }

    @Test
    public fun stripAllPolicyIsExplicit() {
        assertEquals(MetadataPolicy.STRIP_ALL, MetadataPolicy.valueOf("STRIP_ALL"))
    }

    @Test
    public fun metadataDetailsExposeOrientationAndPrivacySignals() {
        val details = ImageMetadataDetails(
            orientation = 6,
            hasGpsLocation = true,
            cameraMake = "Example",
            cameraModel = "Camera",
            dateTimeOriginal = "2026:10:04 18:00:00",
        )
        assertEquals(6, details.orientation)
        assertEquals(true, details.hasGpsLocation)
        assertEquals("Example", details.cameraMake)
    }
}
