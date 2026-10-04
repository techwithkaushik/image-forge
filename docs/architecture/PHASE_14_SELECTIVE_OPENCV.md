# Phase 14 — Selective OpenCV

## Decision

Phase 14 introduces an isolated `:core:vision-opencv` module for computer-vision operations that are materially better expressed with OpenCV than with Android standard bitmap APIs.

The module is **not connected to `:app`** in this phase. This prevents the full OpenCV Android AAR from increasing the production APK before a concrete feature requires it.

OpenCV 5.0.0.1 is consumed from Maven Central.

## First CV capability

The first capability is bounded contour detection:

- RGBA pixel input
- grayscale conversion
- Gaussian blur
- Canny edge detection
- external contour extraction
- area/perimeter calculation
- bounding rectangles
- deterministic contour ordering
- maximum contour count
- explicit area/threshold limits

This is a foundation for later document/form geometry detection. It does not claim to identify a document, passport photo, signature, or semantic object by itself.

## Safety contract

- No Compose dependency.
- No ViewModel dependency.
- No feature-module dependency.
- No CMake, JNI source, or custom C++ code.
- Input pixels are copied into an owned bitmap before OpenCV processing.
- OpenCV `Mat` objects are released in `finally`.
- Intermediate `MatOfPoint2f` objects are released immediately.
- Coroutine cancellation is checked before and during contour processing.
- Input arrays are never mutated.
- Result count is bounded.

## APK-size boundary

The official OpenCV Android Maven artifact is a complete OpenCV Android distribution rather than a tiny per-function library. Therefore the module remains isolated and is not an application dependency yet. ABI splitting already implemented in CI will reduce architecture-specific delivery size once this module is intentionally connected, but it does not turn the OpenCV AAR into a small library.

Before connecting this module to the application, Phase 14 must measure the resulting ABI APK delta and decide whether a custom minimal OpenCV build is justified.

## Non-goals

- OCR
- ML object detection
- camera preview
- automatic document classification
- automatic passport/signature extraction
- arbitrary native OpenCV modules
- replacing existing Android Bitmap/Canvas operations
