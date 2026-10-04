# Storage Contract

## MediaStore
Use MediaStore for normal public media/document destinations where the platform collection is appropriate.

## Storage Access Framework
Use SAF when the user explicitly chooses a custom folder or document destination.

After selection:
- persist URI read/write permission when supported;
- reuse the persisted URI for future saves;
- do not repeatedly ask for the same folder unless permission is revoked/unavailable.

## Save behavior
Saving is explicit. Processing never silently overwrites the source.

## Failure handling
Storage errors are typed domain failures. Partial temporary output is cleaned up.

## Privacy
No core save or processing operation requires network access.
