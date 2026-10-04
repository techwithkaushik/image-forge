# Phase 10 — Production Metadata & EXIF

## Contract

Metadata handling is explicit. ImageForge never claims to preserve metadata that the Android encoder cannot reliably preserve.

### Inspection

Metadata inspection must not decode full pixel buffers. Technical image dimensions and encoded byte size remain available through the existing bounds/stream path.

### Orientation

EXIF orientation is a source-image property. Before pixel geometry edits, orientation must be normalized into pixels. The resulting edited image must not retain a stale orientation declaration.

### Privacy

Metadata stripping is an explicit contract. GPS/location and other sensitive EXIF values are removed when privacy mode is requested.

### Preservation

Preservation is best-effort and format/encoder dependent. Unsupported metadata must not be silently reported as preserved.

### Safety

- metadata work stays offline.
- no Compose/ViewModel/feature dependency in the core processor.
- cancellation remains cooperative.
- temporary artifacts and intermediate resources are cleaned deterministically.
