# Phase 21 — Physical Device QA & Production Validation

## Purpose

Validate the release APK on real Android hardware after the CI/CD gates from Phase 20.

This phase is intentionally device-driven. GitHub Actions can validate build integrity, signing, APK metadata, static offline guardrails, and automated tests, but it cannot replace real-device interaction, camera/SAF/MediaStore behavior, memory pressure, rotation/background behavior, or airplane-mode validation.

## Device QA matrix

Record one row per physical device/build tested.

| Area | Result | Notes |
|---|---|---|
| Release APK install | PASS / FAIL | Installation was reported successful before Phase 21 continuation. |
| First launch | PASS / FAIL | Startup time, crash, blank screen |
| Dashboard/navigation | PASS / FAIL | Navigation and back behavior |
| Import image | PASS / FAIL | SAF/MediaStore picker |
| Camera (if available) | PASS / FAIL | Permission + capture |
| Resize | PASS / FAIL | Output dimensions/readability |
| Crop/edit | PASS / FAIL | Large and normal images |
| Compression/conversion | PASS / FAIL | JPEG/PNG/WEBP/HEIC where supported |
| EXIF/privacy | PASS / FAIL | Strip/preserve policy behavior |
| Passport/signature | PASS / FAIL | Detection/crop/output quality |
| OCR | PASS / FAIL | Latin + Devanagari offline |
| PDF | PASS / FAIL | Generation/readability |
| Merge/split | PASS / FAIL | Output integrity |
| Batch processing | PASS / FAIL | Progress, cancellation, memory |
| Save/persistence | PASS / FAIL | MediaStore/SAF/custom folder |
| Re-open saved files | PASS / FAIL | File remains readable |
| Rotation | PASS / FAIL | State survives configuration change |
| Background/return | PASS / FAIL | No crash/data loss |
| Large image | PASS / FAIL | Memory stability |
| Repeated processing | PASS / FAIL | No progressive slowdown/crash |
| Offline / airplane mode | PASS / FAIL | Complete core workflow without network |
| Permissions | PASS / FAIL | Only required permissions requested |
| Final smoke | PASS / FAIL | End-to-end release candidate |

## Required stress checks

### Large image

Use at least one high-resolution image and verify:

- no OOM/crash
- UI remains responsive
- processing can be cancelled
- output is readable
- temporary files are cleaned

### Repeated processing

Run the same workflow repeatedly and verify:

- memory does not grow uncontrollably
- processing time does not degrade unexpectedly
- old temporary artifacts do not accumulate
- saved outputs remain independent and readable

### Offline validation

Before testing:

1. Enable airplane mode, or otherwise disable all network connectivity.
2. Force-stop and relaunch ImageForge.
3. Run the core local workflow.
4. Import/process/save/re-open an image.
5. Exercise OCR and other bundled local processing.
6. Confirm there is no network-dependent failure.

CI already enforces static offline guardrails; this step is the required physical-device confirmation.

## Acceptance criteria

Phase 21 can be closed only when:

- the release APK installs and launches on target hardware
- core workflows complete without crashes or data loss
- offline operation succeeds with connectivity disabled
- saved files are present and readable after leaving the workflow
- cancellation/progress behave correctly
- large-image and repeated-processing checks do not expose blocking memory regressions
- permissions and rotation/background behavior are acceptable
- no blocking production regression remains
- any production-impacting fix is covered by automated validation where practical
- final CI remains green after fixes

## Defect handling

Only production-impacting defects should be fixed in this phase.

For each defect record:

- device/model
- Android version
- APK ABI/version
- exact reproduction steps
- expected result
- actual result
- crash/log evidence when available
- severity
- regression test added, if applicable

Do not broaden Phase 21 into feature development. Feature requests and non-blocking polish should remain outside the release-validation scope.

## Sign-off

Device:

Android version:

APK/ABI:

Build/commit:

Tester:

Date:

Overall result: PASS / FAIL

Blocking issues:

Follow-up issues:
