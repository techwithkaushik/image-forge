# Phase 6 — Target-Size Compression Design

## Scope
Implement deterministic offline target-size compression for JPEG/WebP using Android codecs.

### Modes
- **Under Target:** final bytes <= target.
- **Closest To Target:** maximize bytes without exceeding target.
- **Exact Bytes:** separate byte-level contract; only claim success after final byte count equals the requested size.

## Algorithm contract
1. Validate target size and supported MIME/encoder.
2. Encode at bounded quality levels.
3. Measure the actual encoded bytes after every candidate.
4. Use bounded binary search while size remains sufficiently monotonic.
5. Keep only the best candidate artifact and delete discarded temporary files immediately.
6. Re-encode/verify the selected candidate before returning it.
7. Cancellation must delete all candidate artifacts.
8. Exact-byte mode must never silently fall back to quality-only compression.

## Memory contract
- Reuse the already-decoded bitmap when possible.
- Never keep all quality candidates in memory.
- Candidate files are temporary and lifecycle-managed.
- Core processor remains independent of Compose/ViewModel/features.

## Acceptance criteria
- Offline and deterministic for the same source/encoder/policy.
- Under Target never returns an artifact larger than target.
- Closest To Target returns the largest verified candidate <= target.
- Exact Bytes only succeeds when the final byte count is exactly the requested count.
- Cancellation/failure leaves no candidate artifacts.
- Existing core tests and release APK remain green.
