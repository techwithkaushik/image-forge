# Phase 18 — Performance & APK Size

## Objective
Reduce release package weight and avoidable runtime overhead without changing ImageForge behavior or removing required offline OCR, ML, or OpenCV functionality.

## Implemented optimization

### Release R8/resource shrinking
The application release build now enables:
- R8 code shrinking and optimization.
- Resource shrinking.
- The standard Android optimized baseline rules.
- A deliberately minimal project-level ProGuard file.

Dependency-specific consumer rules remain authoritative for ML Kit/OpenCV. No broad package keep rules are added.

## Runtime/memory review
The image-processing implementation already contains important memory protections:
- Decode/output pixel budgets are enforced before expensive operations.
- Resize uses sampled decoding where possible.
- Crop uses BitmapRegionDecoder when EXIF orientation permits.
- Processing runs on Dispatchers.IO.
- Owned bitmaps are tracked and recycled in finally.
- Temporary encoded files are deleted on failure/cancellation.
- PDF rendering recycles page bitmaps after each page.
- Batch processing remains sequential to avoid multiplying peak bitmap memory.

No speculative bitmap-processing rewrite was made in this phase because the current implementation already has explicit lifecycle and memory-budget controls. This keeps the optimization low-risk while preserving correctness.

## Measurement
CI now publishes image-forge-phase18-size-report on non-PR builds. The report records each architecture-specific release APK size and the combined byte total.

Baseline and post-optimization numbers must be taken from actual CI artifacts; they are intentionally not hard-coded here.

## Acceptance checklist
- [x] Release APK shrinking enabled.
- [x] Release resource shrinking enabled.
- [x] No broad keep rules added.
- [x] Existing bitmap lifecycle safeguards preserved.
- [x] Sequential batch memory behavior preserved.
- [x] CI publishes measured APK sizes.
- [ ] CI release build and unit tests green.
- [ ] Post-optimization artifact sizes recorded after successful CI.