# Image Processing Contract

## Operation model
Each operation receives an immutable request and returns an immutable result or typed processing error.

Operations must:
- run off the main thread;
- be cancellable;
- avoid unnecessary bitmap copies;
- avoid retaining full-resolution intermediates longer than required;
- preserve EXIF/orientation according to operation policy;
- never silently mutate the caller's original bitmap/file.

## Pipeline
Input URI/File -> Decode + bounds/orientation -> Validate dimensions/memory budget -> Operation pipeline -> Encode -> Result -> Optional storage

## Error categories
- invalid input
- unsupported format
- decode failure
- insufficient memory
- cancelled
- processing failure
- encode failure
- storage failure

## Compression modes
### Under Target
Output bytes must be <= target where the selected encoder permits it.

### Closest To Target
Search encoder parameters to maximize size without exceeding target and verify final bytes.

### Exact Bytes
Exact byte equality is a separate contract. Quality alone does not guarantee exact byte count. Exact-size output requires a deterministic byte-level strategy and must be distinguished from visually optimized output.

## Quality search
Use bounded binary search for quality-based encoders where size is sufficiently monotonic. Always verify final encoded byte count.

## Ownership
A processing result owns generated output until persisted or released. Temporary output is cleaned on cancellation/failure.
