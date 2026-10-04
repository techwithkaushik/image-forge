package org.techwithkaushik.imageforge.imageprocessor

import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.techwithkaushik.imageforge.common.ForgeError
import org.techwithkaushik.imageforge.common.ForgeResult
import org.techwithkaushik.imageforge.common.ProcessingProgress

public class BatchImageProcessorTest {
    private val operation = ImageOperation.Resize(100, 100)

    @Test
    public fun processesAllItemsInInputOrderAndReportsAggregateProgress() = runBlocking {
        val inputs = listOf(
            BatchImageInput("one", ImageInput(Uri.parse("content://one"))),
            BatchImageInput("two", ImageInput(Uri.parse("content://two"))),
        )
        val progress = mutableListOf<ProcessingProgress>()
        val processor = fakeProcessor { request, onProgress ->
            onProgress(ProcessingProgress(50, 100, "Half"))
            ForgeResult.Success(artifact(request.input.uri))
        }

        val result = BatchImageProcessor(processor).process(
            BatchProcessingRequest(inputs, operation),
            progress::add,
        )

        assertEquals(listOf("one", "two"), result.successes.map { it.id })
        assertEquals(2, result.successCount)
        assertEquals(200, progress.last().completedSteps)
        assertEquals(200, progress.last().totalSteps)
    }

    @Test
    public fun retriesFailedItemAndThenContinues() = runBlocking {
        var calls = 0
        val inputs = listOf(
            BatchImageInput("retry", ImageInput(Uri.parse("content://retry"))),
            BatchImageInput("ok", ImageInput(Uri.parse("content://ok"))),
        )
        val processor = fakeProcessor { request, _ ->
            if (request.input.uri.toString() == "content://retry" && calls++ < 2) {
                ForgeResult.Failure(ForgeError.ProcessingFailed("temporary"))
            } else {
                ForgeResult.Success(artifact(request.input.uri))
            }
        }

        val result = BatchImageProcessor(processor).process(
            BatchProcessingRequest(inputs, operation, maxRetries = 2),
        )

        assertEquals(listOf("retry", "ok"), result.successes.map { it.id })
        assertEquals(3, result.successes.first().attempts)
    }

    @Test
    public fun skipsAfterRetryBudgetIsExhausted() = runBlocking {
        var calls = 0
        val inputs = listOf(
            BatchImageInput("bad", ImageInput(Uri.parse("content://bad"))),
            BatchImageInput("good", ImageInput(Uri.parse("content://good"))),
        )
        val processor = fakeProcessor { request, _ ->
            if (request.input.uri.toString() == "content://bad") {
                calls++
                ForgeResult.Failure(ForgeError.InvalidInput("bad input"))
            } else {
                ForgeResult.Success(artifact(request.input.uri))
            }
        }

        val result = BatchImageProcessor(processor).process(
            BatchProcessingRequest(inputs, operation, maxRetries = 1),
        )

        assertEquals(2, calls)
        assertEquals(listOf("bad"), result.skipped.map { it.id })
        assertEquals(listOf("good"), result.successes.map { it.id })
        assertEquals(listOf("content://good"), result.artifactsForSaveAll.map { it.uri.toString() })
    }

    @Test
    public fun cancellationIsPropagated() = runBlocking {
        val processor = object : ImageProcessor {
            override suspend fun process(
                request: ImageProcessingRequest,
                onProgress: (ProcessingProgress) -> Unit,
            ): ForgeResult<ImageArtifact> {
                throw CancellationException("cancelled")
            }
        }

        try {
            BatchImageProcessor(processor).process(
                BatchProcessingRequest(
                    listOf(BatchImageInput("cancel", ImageInput(Uri.parse("content://cancel")))),
                    operation,
                ),
            )
            throw AssertionError("Expected cancellation")
        } catch (expected: CancellationException) {
            assertTrue(expected.message.orEmpty().contains("cancelled"))
        }
    }

    @Test
    public fun rejectsInvalidBatchConfiguration() {
        assertTrue(runCatching { BatchProcessingRequest(emptyList(), operation) }.isFailure)
        assertTrue(
            runCatching {
                BatchProcessingRequest(
                    listOf(
                        BatchImageInput("same", ImageInput(Uri.parse("content://one"))),
                        BatchImageInput("same", ImageInput(Uri.parse("content://two"))),
                    ),
                    operation,
                )
            }.isFailure,
        )
    }

    private fun artifact(uri: Uri): ImageArtifact =
        ImageArtifact(
            uri = uri,
            metadata = ImageMetadata(
                width = 100,
                height = 100,
                mimeType = "image/jpeg",
                byteCount = 10,
            ),
        )

    private fun fakeProcessor(
        block: suspend (ImageProcessingRequest, (ProcessingProgress) -> Unit) -> ForgeResult<ImageArtifact>,
    ): ImageProcessor =
        object : ImageProcessor {
            override suspend fun process(
                request: ImageProcessingRequest,
                onProgress: (ProcessingProgress) -> Unit,
            ): ForgeResult<ImageArtifact> = block(request, onProgress)
        }
}