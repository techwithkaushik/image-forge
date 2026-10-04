# Phase 13 — Production Offline OCR

## Decision
ImageForge uses bundled ML Kit Text Recognition v2. The OCR model is packaged with the application and is not downloaded at runtime.

## Supported scripts
- Latin
- Devanagari

Each request explicitly selects one script through OcrOptions.script. This avoids running two OCR engines over the same image and keeps CPU/memory usage deterministic.

## Processing contract
- Input is a user-selected Uri.
- OCR performs byte-size and image-dimension checks before inference.
- The source image is never mutated.
- ML Kit receives the URI through InputImage.fromFilePath.
- Recognition is exposed as a suspend API.
- The recognizer is closed after each request.
- Cancellation maps to ForgeError.Cancelled.
- Recognition failures map to ForgeError.ProcessingFailed.
- No network fallback or model download is initiated.

## Result model
The result contains full text, blocks, lines, elements, bounding rectangles, confidence where provided, and the selected script.

The OCR module has no Compose, ViewModel, or feature-module dependency.

## Deliberate limitations
This phase does not provide automatic language detection, handwriting recognition, document layout reconstruction, OCR correction, network OCR, or automatic multi-script inference.

## APK-size tradeoff
Bundled OCR is larger than an unbundled installation because the model ships with the app. This is intentional for deterministic offline behavior. Release size should be measured before any future change that removes this guarantee.
