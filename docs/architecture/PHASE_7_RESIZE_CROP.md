# Phase 7 — Production Resize & Crop

## Contract

Resize and crop remain deterministic core image operations. Geometry is validated before decoding, large operations are rejected against the configured memory policy, and crop decoding preserves the requested crop dimensions.

### Resize
- Requested width and height must be positive.
- Requested output pixels must fit both the output/decode pixel limits and the ARGB_8888 bitmap byte budget.
- Sampling is selected so the decoded source is never smaller than the requested dimensions.
- The final bitmap is scaled to the exact requested dimensions.

### Crop
- Left/top must be non-negative.
- The crop rectangle must be fully contained in the source bounds.
- Crop pixels must fit the configured decode and bitmap-byte limits.
- Region decoding uses full resolution for the requested rectangle so the returned dimensions are exact.

### Lifecycle
- Bitmap ownership is tracked and released in a single finally block.
- Cancellation propagates as CancellationException.
- Temporary encoded artifacts are deleted on cancellation and failure.

### Scope boundary

This phase does not add UI behavior, Compose dependencies, feature-module dependencies, or full OpenCV. It hardens the existing native Android image engine contract.
