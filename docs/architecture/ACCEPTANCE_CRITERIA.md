# Phase 0 Acceptance Criteria

## Architecture
- [ ] Required modules and dependency direction documented.
- [ ] Core processor independent of UI.
- [ ] MVVM + StateFlow boundary documented.
- [ ] New dependencies require architectural justification.

## Processing
- [ ] Operations cancellable.
- [ ] Heavy work never runs on main thread.
- [ ] Large-image memory behavior bounded and tested.
- [ ] Typed failures for decode/process/encode/cancel/storage.
- [ ] Source content is not silently overwritten.

## Compression
- [ ] Under Target verified by encoded byte count.
- [ ] Closest To Target deterministic and verified.
- [ ] Exact Bytes treated as a separate byte-level contract.

## Storage
- [ ] MediaStore path supported.
- [ ] SAF custom-folder path supported.
- [ ] Persisted SAF permissions reused.

## Offline
- [ ] Core processing succeeds with network disabled.
- [ ] No image uploaded by core processing.
- [ ] No Pi7 server/API required.

## Performance
- [ ] No processing-induced UI freeze.
- [ ] Cancellation works for long operations.
- [ ] Dependency size measured before adding CV libraries.
