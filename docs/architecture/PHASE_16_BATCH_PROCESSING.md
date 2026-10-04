# Phase 16 — Batch Processing

## Decision

Batch processing is a thin orchestration layer over the existing ImageProcessor contract. It does not duplicate image-processing logic and does not introduce a second processing engine.

## Behavior

- A batch contains one or more uniquely identified input images.
- One ImageOperation and metadata policy are shared by the batch.
- Items are processed sequentially by default. This limits peak memory pressure from concurrent bitmap allocations.
- Each item reports progress through the existing ProcessingProgress contract. Batch progress maps the current item onto a total of items multiplied by 100 steps.
- Each item may retry up to three times. The configured retry count is bounded to 0..3.
- After the retry budget is exhausted, the item becomes Skipped and the batch continues.
- Coroutine cancellation is not converted into a normal batch failure; CancellationException propagates to the caller.
- Successful artifacts remain in input order and are exposed through artifactsForSaveAll for the storage/UI layer.

## Architecture

Compose -> ViewModel -> Batch orchestrator -> ImageProcessor -> Android APIs / selective OpenCV -> Storage

The batch layer has no Compose dependency and does not modify the existing single-image ImageProcessor API.

## Memory and reliability

Sequential execution is the default memory-safety policy. Every item reuses the existing processor cancellation, decode-budget, bitmap cleanup and output-file cleanup behavior.

Batch orchestration does not catch or suppress coroutine cancellation.

## Scope boundary

This phase does not add UI screens, storage implementation changes, parallel processing, a job database, background WorkManager jobs, or a new image-processing dependency.

## Save All

artifactsForSaveAll is a storage-neutral collection of successful ImageArtifact values. The existing storage layer can persist these outputs in one user action without coupling the image-processing core to MediaStore or SAF UI behavior.