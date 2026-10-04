# Phase 9 — Production Image Editing

## Contract

Editing remains an offline core operation implemented with Android Bitmap, Canvas, Matrix and ColorMatrix APIs.

### Geometric operations

- Rotate: exactly 90, 180 or 270 degrees.
- Flip: horizontal, vertical, or both.
- Rotation swaps width/height for 90/270 degrees and preserves dimensions for 180 degrees.
- Flip never changes dimensions.

### Color operations

Brightness, contrast and saturation are represented as bounded parameters and applied to a new bitmap.

- Brightness range: -1.0..1.0
- Contrast range: 0.0..2.0
- Saturation range: 0.0..2.0
- Defaults are neutral: brightness 0, contrast 1, saturation 1.

The source bitmap is never mutated in place.

### Memory and cancellation

- Validate expected output dimensions against the configured bitmap budget before allocation.
- Check coroutine cancellation before and after expensive transforms.
- Track every newly allocated bitmap and recycle it in the processor finally block.
- Reuse the existing atomic temporary-output encoding and cleanup path.

### Non-goals

This phase does not add OpenCV, UI editing state, undo/redo persistence, OCR, document detection, or feature-module code.

## Definition of Done

- Typed editing operations are validated at the domain boundary.
- Native transforms are deterministic and offline.
- Geometry is verified by tests.
- Color transforms do not mutate source bitmaps.
- Memory/cancellation/error contracts remain intact.
- Core tests and release APK CI are green.
