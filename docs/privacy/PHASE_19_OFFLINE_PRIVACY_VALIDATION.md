# Phase 19 — Offline & Privacy Validation

## Roadmap scope
The production roadmap requires the core workflow to remain usable with Internet disabled, core images to never require server upload, temporary files to be cleaned, and EXIF privacy controls to be provided.

## Offline validation
- The application manifest declares no `android.permission.INTERNET` permission.
- Repository search found no HTTP client/network API usage in the application/core source reviewed for this phase.
- Core processing uses Android media/storage APIs, local bitmap processing, local OpenCV/ML/OCR components, and local temporary files.
- CI includes a guard that fails if the app manifest gains an INTERNET permission.

## Image privacy
`MetadataPolicy` exposes three explicit modes:
- `PRESERVE_SUPPORTED`: preserve supported non-sensitive metadata.
- `STRIP_SENSITIVE`: remove GPS and other sensitive identity/device fields while retaining supported technical metadata.
- `STRIP_ALL`: remove metadata and report that all metadata was stripped.

EXIF normalization also writes normal orientation after a bitmap has been physically normalized.

## Temporary-file lifecycle
Image processing writes intermediate outputs under the application cache processing directory. Failure and cancellation paths delete temporary output files. Owned bitmaps are recycled in `finally`; PDF page bitmaps are released per page. Successful output remains available because it is the artifact returned to the caller for the subsequent save flow.

## Validation boundary
A true device-level airplane-mode run cannot be reproduced by a source-only CI job. Therefore this phase adds static offline/network guardrails and documents the runtime behavior. Device smoke validation should be performed with network connectivity disabled before final release.

## Acceptance
- [x] No INTERNET permission in the app manifest.
- [x] No network dependency identified in reviewed core/application source.
- [x] Explicit EXIF privacy policies exist and are covered by unit tests.
- [x] Failure/cancellation temporary-output cleanup exists.
- [x] Bitmap lifecycle cleanup exists.
- [x] Offline/privacy validation documented.
- [ ] CI green after Phase 19 changes.
- [ ] Final release device smoke test with Internet disabled.