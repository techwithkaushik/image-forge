# Phase 12 — Merge, Split & PDF Processing

## Image to PDF

AndroidPdfProcessor.createPdf() accepts an ordered list of image inputs and creates one PDF page per input using Android's native PdfDocument.

Supported configuration:
- A4 or Letter page size
- portrait or landscape
- configurable margins
- contain or cover placement
- opaque page background

Images are decoded sequentially, EXIF orientation is normalized, and the existing bitmap memory policy is enforced. Source files are never modified.

## PDF to images

splitPdf() uses Android's native PdfRenderer to render pages sequentially to PNG or JPEG temporary artifacts.

Output dimensions are bounded by target DPI and an explicit maximum pixel budget. Page order is deterministic and partial outputs are deleted when processing fails or is cancelled.

## Platform boundary

This phase intentionally does not claim lossless arbitrary PDF-object merging or page extraction that preserves embedded PDF vectors or text. Native PdfDocument is a generator and PdfRenderer is a renderer; preserving arbitrary PDF objects requires a dedicated PDF manipulation engine. That capability remains a separate future decision rather than a lossy hidden approximation.

## Storage

The processor returns temporary local artifacts. Final shared-storage publishing remains the responsibility of the existing storage layer. Android's Storage Access Framework supports explicit document creation through ACTION_CREATE_DOCUMENT, including PDF MIME type selection.

## Safety

- Offline only.
- Sequential bitmap processing.
- Existing memory limits.
- Coroutine cancellation checkpoints.
- Source immutability.
- Temporary-file cleanup on failure and cancellation.
