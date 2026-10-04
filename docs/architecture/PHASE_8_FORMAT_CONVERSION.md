# Phase 8 — Production Image Format Conversion

## Contract

ImageForge performs offline format conversion in `:core:imageprocessor` using Android's native bitmap decoder and codecs.

### Supported output formats

- `image/jpeg`
- `image/png`
- `image/webp`

Input decoding continues to rely on Android platform support, including JPEG, PNG, WebP and HEIC/HEIF where the device decoder supports them.

### Geometry

A format conversion is not a resize operation.

- Output width must equal source width.
- Output height must equal source height.
- Conversion must not silently downsample an oversized source.
- If the source exceeds the configured decode/bitmap budget, conversion fails before allocating the full-resolution bitmap.

### Alpha

PNG and WebP preserve the decoded bitmap's alpha channel when the platform encoder supports it.

JPEG has no alpha channel. ImageForge therefore composites transparent pixels onto an explicit opaque white background before JPEG encoding. This makes JPEG conversion deterministic instead of depending on encoder handling of alpha.

### Encoding

- JPEG uses the native JPEG encoder.
- PNG uses the native PNG encoder.
- WebP uses the native WebP encoder; API 30+ uses lossless WebP.
- The actual encoded file length is reported in `ImageMetadata.byteCount`.
- The output MIME type in metadata is the requested normalized output MIME.

### Safety

- Validate output MIME before decoding.
- Enforce `ImageProcessingPolicy` before full-resolution conversion decode.
- Check coroutine cancellation between major stages.
- Track every bitmap created by the processor and recycle it in `finally`.
- Delete temporary output files on cancellation or failure.
- Keep conversion independent from Compose, ViewModel and feature modules.

## Definition of Done

- Supported conversion requests are deterministic and offline.
- Geometry is preserved exactly.
- JPEG alpha flattening is explicit and deterministic.
- Unsupported output MIME types return typed invalid-input failures.
- Large sources are rejected before full-resolution allocation when outside policy.
- Core tests and release APK CI are green.
