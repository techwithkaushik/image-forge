# ImageForge Architecture

## Product
ImageForge is a native Android image-processing studio designed for offline-first operation.

## Non-negotiable stack
- Kotlin
- Jetpack Compose
- Clean Architecture
- MVVM
- ViewModel + StateFlow
- Coroutines
- Hilt
- Navigation Compose with type-safe routes
- Android native image APIs first
- Selective/minimal OpenCV only where native APIs are insufficient

## Modules
```
:app
:core:common
:core:designsystem
:core:imageprocessor
:core:media
:core:storage
:core:ocr
:feature:dashboard
:feature:editor
:feature:documents
```

## Dependency direction
Feature modules may depend on core modules. Core modules must never depend on feature modules. `:core:imageprocessor` must not depend on Compose, ViewModel, or UI.

## Runtime flow
```
Compose Screen
  -> ViewModel
  -> StateFlow
  -> UseCase
  -> Repository
  -> ImageProcessor
  -> Android APIs / selective OpenCV
  -> Storage
```

## Processing rules
1. Processing is off the main thread.
2. Every long-running operation accepts coroutine cancellation.
3. Large intermediates are not retained unnecessarily.
4. UI observes immutable state; UI never performs image processing directly.
5. Processor APIs return domain results/errors, not UI state.
6. Temporary files are cleaned deterministically.
7. Original input is never overwritten unless an explicit save operation requests it.

## OpenCV rule
OpenCV is not the default image engine. Android Bitmap/Canvas/Matrix, ImageDecoder, BitmapFactory and native codecs are preferred. OpenCV is limited to operations that materially benefit from computer vision, such as document boundary detection, contours, adaptive thresholding, morphology, perspective correction, denoising, and signature cleanup.

## Storage rule
- MediaStore for standard public destinations where appropriate.
- Storage Access Framework for user-selected folders.
- Persist SAF URI permissions after explicit selection.
- Core processing must not require network storage.

## Offline/privacy rule
Core image operations must work with network unavailable. No image is uploaded to a remote service for core functionality.

## Acceptance baseline
- No UI freezes during processing.
- Cancellation is respected.
- Large images do not cause avoidable OOM conditions.
- Processing can be tested independently of Compose.
- APK size impact of CV dependencies is measured before adoption.
