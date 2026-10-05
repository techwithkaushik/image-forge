# ImageForge Dashboard Tool Implementation Roadmap

## Goal

Connect every tool currently visible on the ImageForge Dashboard to a real, production-ready offline workflow.

Current dashboard inventory: 11 sections and 132 listed entries (some are aliases/duplicates of the same underlying operation).

## Non-negotiable implementation rules

1. Every dashboard button must execute a real operation; no placeholder success states.
2. Reuse the existing pipeline:
   Dashboard -> navigation -> feature screen -> ViewModel/StateFlow -> UseCase -> Repository -> ImageProcessor/Document/OCR -> Storage.
3. Processing stays offline and cancellable.
4. Heavy processing runs off the main thread.
5. Existing memory/decode budgets remain enforced.
6. Save output through the storage abstraction, not directly from UI.
7. Every operation gets success, failure, progress, and cancellation states.
8. Shared operations use one engine with tool-specific presets instead of duplicated implementations.
9. Add unit tests for operation contracts and JVM-testable logic; add device validation for camera/SAF/MediaStore/bitmap behavior.
10. Do not add a heavy dependency when Android APIs or existing OpenCV/ML modules are sufficient.

## Phase D1 — Core image operations

Connect first because many later dashboard tools depend on these primitives.

- Resize Image Pixel
- Resize Image by Pixel
- Rotate Image
- Flip Image
- Freehand Crop
- Circle Crop
- Square Crop
- Round Corners
- Change Aspect Ratio
- Crop PNG
- Image Color Picker
- Blur Image
- Grayscale Image
- Black & White
- Add Border To Image
- Add White Border To Image

Deliverables:
- reusable ImageOperation implementations
- editor controls
- preview/output state
- save flow
- validation and tests

## Phase D2 — Compression and exact-size engine

Build one target-size engine and expose presets from the dashboard.

- Image Compressor
- Reduce Image Size in KB
- Reduce Size in KB
- Reduce Size in MB
- JPG to KB
- Convert MB to KB
- Convert KB to MB
- Compress to 5KB
- JPEG to 10KB
- Compress to 15KB
- Compress to 20KB
- Compress 20KB-50KB
- JPEG to 25KB
- JPEG to 30KB
- JPEG to 40KB
- Compress to 50KB
- Compress to 60KB
- Compress to 70KB
- Compress to 80KB
- Compress to 90KB
- Resize to 50KB
- Compress to 100KB
- JPEG to 150KB
- Compress to 200KB
- Resize to 200KB
- JPEG to 300KB
- JPEG to 500KB
- Compress to 1MB
- Compress to 2MB

Important:
- target bytes must be enforced by the existing compression contract
- distinguish under-target, closest-target and exact-target behavior
- never claim exact size when the encoder cannot guarantee it
- show final byte count

## Phase D3 — Format conversion

Use one conversion pipeline with MIME-specific presets.

- Image Converter
- Image to JPG
- JPEG to JPG
- HEIC to JPG
- WEBP to JPG
- WebP to PNG
- AVIF to JPG
- JFIF to JPG
- JPEG to PNG
- PNG to JPEG
- PNG to ICO
- Favicon Generator

Validation:
- input/output MIME detection
- unsupported-format errors
- transparency handling
- EXIF/privacy policy
- output readability

## Phase D4 — Standard dimensions and official-size presets

Create a reusable physical-size/DPI conversion model.

- Resize in Centimeters
- Resize in Millimeters
- Resize in Inches
- Resize Image In Centimeter
- Resize Image (3.5cm x 4.5cm)
- A4 Size
- SSC Photo Resize
- PAN Card
- UPSC Photo
- PSC Photo

Requirements:
- px/mm/cm/in conversion
- DPI-aware dimensions
- print-safe output
- preview dimensions
- user-editable DPI where applicable

## Phase D5 — Passport, ID and signature

Use the existing Passport/Signature operation contracts instead of separate implementations.

- Passport Photo Maker
- Passport Photo Maker (duplicate dashboard entry)
- Red Background Passport
- White Background Passport
- Resize Signature
- Generate Signature
- Resize Sign 6cm x 2cm (300 DPI)
- 3.5cm x 4.5cm
- Signature 50mm x 20mm
- 35mm x 45mm
- 2 x 2 Inch
- 3 x 4 Inch
- 4 x 6 Inch
- 600x600 Pixels
- Merge Photo & Signature

Requirements:
- passport crop/aspect presets
- background color selection
- signature extraction/cleanup
- signature resize presets
- photo + signature composition
- DPI metadata
- output-size validation

## Phase D6 — OCR and document workflows

Connect OCR and document tools through the existing offline OCR/document modules.

- Image to Text (OCR)
- JPG to Text
- PNG to Text
- Text to Handwriting
- Image to Word
- Image to PDF
- PDF to JPG
- JPG to PDF (Under 50KB)
- JPG to PDF (Under 100KB)
- JPG to PDF (Under 150KB)
- JPEG to PDF (Under 200KB)
- JPG to PDF (Under 250KB)
- JPG to PDF (Under 300KB)
- JPG to PDF (Under 400KB)
- JPG to PDF (Under 500KB)
- JPG to PDF (Under 1MB)
- JPG to PDF (Under 2MB)

Requirements:
- fully offline OCR
- Latin + Devanagari
- multi-page document handling
- PDF size presets
- output preview
- save/share through storage abstraction

## Phase D7 — Basic editing and composition

- Watermark Images
- Add Name & DOB on Photo
- Add Text to Image
- Add Logo to Image
- Join Multiple Images
- Split Image
- Merge Photo & Signature
- Photo Collage Maker
- Instagram Grid Maker

Requirements:
- reusable composition canvas
- text/logo positioning
- image ordering
- crop/fit modes
- output resolution control
- memory-safe multi-image processing

## Phase D8 — Blur, pixelate, censor and effects

- Blur Background
- Blur Image
- Blur Face
- Pixelate Image
- Pixelate Face
- Censor Photo
- Motion Blur
- Deep Fry Photo
- Beautify Image
- Add Border To Image
- Blemishes Remover
- Retouch Image
- Picture to Pixel Art

Detection-heavy tools should use the existing selective OpenCV/ML architecture where needed; simple filters should remain Android-native.

## Phase D9 — Background/object/face intelligence

- Remove Background
- Remove Object from Photo
- Unblur Image
- Unblur Face
- AI Photo Enhancer
- AI Face Generator
- Super Resolution
- Upscale Image With AI

Rules:
- offline-only models
- use traditional processing first
- invoke ML only when required
- keep model size/RAM/CPU measurable
- no network fallback
- graceful failure when a model is unavailable

## Phase D10 — DPI and quality

- Increase Image Quality
- Convert DPI (200, 300, 600)
- Check Image DPI
- Super Resolution
- Increase Image Size In KB

Separate:
- metadata-only DPI changes
- pixel-density changes
- actual image enhancement
- target-byte compression

Do not incorrectly claim that changing DPI alone improves pixel quality.

## Phase D11 — Social-media and special presets

- Instagram (No Crop)
- Instagram Grid Maker
- WhatsApp DP
- YouTube Banner
- Zoom Out Image

Implement these as dimension/aspect/padding presets on the shared resize/crop/composition engines.

## Phase D12 — Metadata and privacy

- Edit Metadata
- View Metadata
- Remove Metadata

Use the existing MetadataPolicy/ExifMetadataHandler.

Presets:
- Preserve supported
- Strip sensitive
- Strip all

Validate GPS/device/software metadata removal and orientation handling.

## Phase D13 — Batch processing

Connect:
- Bulk Image Resizer
- multi-image compression
- multi-image conversion
- multi-image PDF
- collage/grid inputs
- batch metadata operations

Reuse the existing sequential BatchImageProcessor. Do not introduce uncontrolled parallel bitmap processing.

## Phase D14 — Dashboard routing and UX completion

Replace current generic routing rules such as “PDF/OCR/Passport/Text -> Documents” with explicit tool IDs/presets.

For every dashboard item:
- stable ToolId
- category
- operation/preset
- required input count
- editor/document screen
- output type
- save action
- error state
- progress/cancellation behavior

Search must return the same stable tool definitions.

## Phase D15 — Testing and physical validation

For every implemented tool:
- operation contract test
- invalid input test
- metadata/privacy test where applicable
- output MIME test
- target-size test where applicable
- cancellation test
- memory-boundary test
- real-device smoke test

Final device matrix should include:
- Android 10 low-end device
- modern ARM64 device
- large image input
- repeated processing
- offline/airplane mode
- MediaStore save
- SAF/custom-folder save

## Recommended execution order

1. D1 Core image operations
2. D2 Compression/exact-size
3. D3 Format conversion
4. D4 Physical/official sizes
5. D5 Passport/signature
6. D6 OCR/PDF/documents
7. D7 Composition/basic editing
8. D8 Effects
9. D9 Offline AI/vision
10. D10 DPI/quality
11. D11 Social presets
12. D12 Metadata/privacy
13. D13 Batch
14. D14 Routing/UX cleanup
15. D15 Full testing/device validation

## Definition of Done

A dashboard tool is considered implemented only when:

- tapping it opens the correct workflow
- the actual input is processed
- output is generated locally
- progress is visible for long operations
- cancellation does not corrupt output
- errors are user-readable
- output can be saved
- relevant tests pass
- no placeholder message remains
- CI remains green
- the tool passes physical-device smoke validation

## Important note

The dashboard currently contains aliases/duplicates. They should not create duplicate processing engines. For example, multiple “Compress to X KB” entries should map to one target-size engine with different presets, and multiple passport/signature/resize entries should map to shared engines with different presets.
