package org.techwithkaushik.imageforge.imageprocessor

import android.net.Uri
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.techwithkaushik.imageforge.common.ForgeError
import org.techwithkaushik.imageforge.common.ForgeResult
import org.techwithkaushik.imageforge.common.ProcessingProgress

public class ImageModelsTest {
    @Test
    public fun resize_requires_positive_dimensions() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageOperation.Resize(0, 100)
        }
    }

    @Test
    public fun crop_requires_positive_dimensions_and_non_negative_origin() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageOperation.Crop(-1, 0, 10, 10)
        }
    }

    @Test
    public fun metadata_preserves_declared_values() {
        val metadata = ImageMetadata(
            width = 100,
            height = 200,
            mimeType = "image/jpeg",
            byteCount = 1234,
        )

        assertEquals(100, metadata.width)
        assertEquals(200, metadata.height)
        assertEquals("image/jpeg", metadata.mimeType)
        assertEquals(1234L, metadata.byteCount)
    }

    @Test
    public fun processing_policy_uses_safe_defaults() {
        val policy = ImageProcessingPolicy()

        assertEquals(12_000_000L, policy.maxDecodePixels)
        assertEquals(16_000_000L, policy.maxOutputPixels)
        assertEquals(64L * 1024L * 1024L, policy.maxBitmapBytes)
    }

    @Test
    public fun processing_policy_rejects_invalid_limits() {
        assertThrows(IllegalArgumentException::class.java) {
            ImageProcessingPolicy(maxDecodePixels = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ImageProcessingPolicy(maxOutputPixels = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ImageProcessingPolicy(maxBitmapBytes = 3)
        }
    }

    @Test
    public fun processing_policy_accepts_minimum_bitmap_limit() {
        assertEquals(
            4L,
            ImageProcessingPolicy(maxBitmapBytes = 4).maxBitmapBytes,
        )
    }

    @Test
    public fun process_image_use_case_delegates_request_and_progress() = runBlocking {
        val uri = mock(Uri::class.java)
        val request = ImageProcessingRequest(
            input = ImageInput(uri = uri, displayName = "input"),
            operation = ImageOperation.Resize(100, 100),
        )
        val artifact = ImageArtifact(
            uri = uri,
            metadata = ImageMetadata(
                width = 100,
                height = 100,
                mimeType = "image/jpeg",
                byteCount = 10,
            ),
        )
        val progress = mutableListOf<ProcessingProgress>()
        val repository = object : ImageRepository {
            override suspend fun process(
                request: ImageProcessingRequest,
                onProgress: (ProcessingProgress) -> Unit,
            ): ForgeResult<ImageArtifact> {
                onProgress(ProcessingProgress(50, 100, "half"))
                return ForgeResult.Success(artifact)
            }
        }

        val result = ProcessImageUseCase(repository).invoke(request, progress::add)

        assertEquals(ForgeResult.Success(artifact), result)
        assertEquals(50, progress.single().completedSteps)
    }

    @Test
    public fun process_image_use_case_preserves_repository_failure() = runBlocking {
        val failure = ForgeResult.Failure(ForgeError.InvalidInput("bad input"))
        val repository = object : ImageRepository {
            override suspend fun process(
                request: ImageProcessingRequest,
                onProgress: (ProcessingProgress) -> Unit,
            ): ForgeResult<ImageArtifact> = failure
        }

        val result = ProcessImageUseCase(repository).invoke(
            ImageProcessingRequest(
                input = ImageInput(
                    uri = mock(Uri::class.java),
                    displayName = "bad",
                ),
                operation = ImageOperation.Inspect,
            ),
        )

        assertEquals(failure, result)
        assertTrue(result is ForgeResult.Failure)
    }
}
