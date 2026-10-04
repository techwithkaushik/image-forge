# Dependency Rules

## Allowed direction
app -> feature/* -> core/*

Features may depend on core modules. Core modules must never depend on features.

### Core boundaries
- core:designsystem -> core:common
- core:media -> core:common + Android media APIs
- core:storage -> core:common + Android storage APIs
- core:ocr -> core:common + OCR implementation
- core:imageprocessor -> core:common + Android graphics APIs + selective CV only when justified

## Forbidden
- Core modules importing feature modules.
- Image processor importing Compose, ViewModel or UI.
- UI directly running Bitmap/OpenCV pipelines.
- Feature modules reaching into another feature's internals.
- Network dependency for core image processing.
- WebView-based processing.
- Full OpenCV SDK by default.
- NDK/C++/CMake image-processing pipeline.

## Boundary rule
Expose interfaces/contracts at module boundaries. Keep implementations internal where practical. Every new dependency must have a documented reason and must preserve this direction.
