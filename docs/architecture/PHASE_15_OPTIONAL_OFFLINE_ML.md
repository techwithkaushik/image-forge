# Phase 15 — Optional Offline ML

## Decision

ImageForge introduces an ML boundary without enabling a model runtime in the application.

The `:core:ml` module owns only immutable model metadata, inference request/result contracts, and a disabled provider. A future local model runtime may implement `OfflineMlProcessor` without leaking ML dependencies into UI, feature modules, or the existing image-processing contracts.

## Rules

- Offline means no model download, network fallback, telemetry, or remote inference.
- ML is optional: the base application remains fully functional when no ML runtime is enabled.
- No model file is bundled in this phase.
- No TensorFlow Lite, ONNX Runtime, or other heavyweight inference dependency is added yet.
- Existing native Android and selective OpenCV paths remain the default processing path.
- `:core:ml` must not depend on Compose, ViewModel, feature modules, or OpenCV.
- Inputs and outputs are immutable at the API boundary.
- Implementations must use suspend APIs and must preserve coroutine cancellation.
- A future model provider must declare explicit input/output contracts and measurable memory/latency limits.
- Any future model must be evaluated on a held-out corpus before it is enabled by default.
- APK size impact must be measured before enabling a runtime or bundling model assets.

## Why this phase is intentionally small

Adding a model runtime without a production model, representative corpus, and measured target would increase APK size and operational complexity without establishing a reliable product capability. This phase creates the seam needed for a measured, offline model integration later.

## Acceptance

1. `:core:ml` compiles and has deterministic unit tests.
2. The application has no ML runtime dependency from this phase.
3. No network permission or model download path is introduced.
4. Existing image/OCR/OpenCV behavior remains unchanged.
5. The disabled provider fails explicitly instead of silently falling back.
6. Future ML implementations can be added behind `OfflineMlProcessor`.
