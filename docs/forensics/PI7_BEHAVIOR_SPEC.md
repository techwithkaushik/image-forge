# Pi7 Behavior & Capability Specification

## Purpose

This document records externally observable Pi7 image-tool capabilities that ImageForge intends to reproduce offline. It separates verified public behavior from implementation assumptions.

## Verified public capabilities

The current public Pi7 Image Tool advertises:

- image compression to target KB sizes;
- image resizing by pixels and physical dimensions;
- crop, rotate, flip, mirror and aspect-ratio changes;
- borders, blur, grayscale, black/white and invert operations;
- text and watermark operations;
- EXIF metadata viewing/removal;
- passport and ID-photo workflows;
- signature resizing and crop workflows;
- JPG/PNG/WebP and other format conversions;
- image-to-PDF and PDF-to-image workflows;
- OCR/image-to-text workflows;
- batch/multiple-image processing;
- specialized government-form dimensions and target sizes.

Pi7's public image-tool pages also advertise exact-size compression workflows and passport/signature workflows. These are product-level capabilities, not evidence of Pi7's internal implementation.

## Verified source references

- Pi7 Image Tool home/tool catalog: https://image.pi7.org/
- Pi7 image-tool list: https://image.pi7.org/image-tool-list
- Pi7 merge photo and signature: https://image.pi7.org/merge-photo-and-signature
- Pi7 resize signature: https://image.pi7.org/resize-signature

## Important distinction

The public pages establish user-visible features and advertised behavior. They do **not** establish Pi7's private encoder settings, exact compression algorithm, CV model, server implementation, or byte-level algorithm.

ImageForge therefore must not claim binary/algorithmic equivalence to Pi7 without direct reproducible evidence.

## Offline parity target

ImageForge targets functional parity for useful image workflows while remaining:

- fully offline for core processing;
- native Android;
- memory-safe and cancellable;
- free from Pi7 API/server dependencies.

## Assumptions requiring validation

The following require implementation testing rather than inference:

1. Exact byte-level reproduction of a Pi7 output.
2. Exact encoder parameters used by Pi7.
3. Internal document/face/signature detection algorithms.
4. Whether a particular Pi7 tool is processed entirely client-side or server-side.
5. Pixel-for-pixel equivalence across all input formats.

## Acceptance rule

When a future implementation is compared with Pi7, record:

- input file and dimensions;
- selected Pi7 options;
- observed output dimensions/format/byte count;
- ImageForge options;
- ImageForge output dimensions/format/byte count;
- visual differences;
- metadata differences;
- reproducibility notes.

No inferred implementation detail should be promoted to a verified fact.
