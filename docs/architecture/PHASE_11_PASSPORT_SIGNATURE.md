# Phase 11 — Passport & Signature Processing

## Passport photo
Passport processing is deterministic and offline. A preset defines physical size, DPI metadata for the product contract, and exact pixel dimensions. The processor center-crops to the requested aspect ratio and then resizes to the exact dimensions.

Current presets:
- India: 35 × 45 mm at 300 DPI → 413 × 531 px
- US: 2 × 2 inch at 300 DPI → 600 × 600 px

The source is never modified.

## Signature extraction
Signature extraction is deliberately conservative:
1. Decode within the existing processor memory budget.
2. Scan luminance for dark ink.
3. Reject inputs with fewer than the configured minimum ink pixels.
4. Compute a tight bounding box with configurable padding.
5. Render on a white background.
6. Optionally binarize to black/white for clean signature output.

This is a deterministic native baseline, not an ML classifier. OpenCV remains an extension point for later CV improvements when native processing is insufficient.

## Safety
- All operations are offline.
- Existing metadata policy is applied to outputs.
- Existing target-size compression can be composed after these operations.
- Cancellation is checked during signature scanning.
- Source files are never overwritten.
