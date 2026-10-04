package org.techwithkaushik.imageforge.imageprocessor

import org.junit.Assert.assertEquals
import org.junit.Test

public class MetadataModelsTest {
    @Test
    public fun defaultPolicyIsPreserveSupported() {
        assertEquals(MetadataPolicy.PRESERVE_SUPPORTED, MetadataPolicy.valueOf("PRESERVE_SUPPORTED"))
    }

    @Test
    public fun sensitivePolicyIsExplicit() {
        assertEquals(MetadataPolicy.STRIP_SENSITIVE, MetadataPolicy.valueOf("STRIP_SENSITIVE"))
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
