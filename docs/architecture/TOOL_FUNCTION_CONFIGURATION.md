# ImageForge Tool Function & Configuration Architecture

## Purpose

Dashboard entries are product tools, not separate image-processing implementations.

The production model is:

Tool -> ToolType -> FunctionType -> Configuration -> Shared Engine

This keeps the implementation reusable while allowing each tool to expose only the controls it needs.

## Tool types

- IMAGE
- PDF
- DOCUMENT

## Function types

- RESIZE
- CROP
- CROP_RESIZE
- FIXED_SIZE_CROP_RESIZE
- COMPRESSION
- CONVERSION
- TRANSFORM
- PASSPORT_PHOTO
- SIGNATURE
- EFFECT
- DPI_QUALITY
- METADATA
- OCR
- IMAGE_TO_PDF
- PDF_TO_IMAGE
- BATCH
- AI
- EDITING

## Crop policy

Image tools expose a shared crop capability unless the tool explicitly disables it.

### Flexible crop

Used by general image tools such as resize, compression, conversion and effects.

- free crop
- ratio crop
- dimension crop
- pixel/mm/cm/inch units
- zoom
- pan
- resizable crop frame

### Fixed crop

Used by exact-size tools such as 35mm x 45mm.

- crop frame ratio is locked
- configured dimensions cannot be edited
- user can zoom the image
- user can pan the image
- user can scale/reposition the image
- crop frame itself cannot be resized

The configured physical dimensions are converted to output pixels using the configured DPI.

### Disabled crop

Used when crop would contradict the tool contract, for example an explicit Instagram (No Crop) preset or PDF/document workflows.

## Examples

### Resize Image by Pixel

ToolType.IMAGE

FunctionType.CROP_RESIZE

Flexible crop followed by user-selected pixel resize.

### 35mm x 45mm

ToolType.IMAGE

FunctionType.FIXED_SIZE_CROP_RESIZE

Fixed 35 x 45 mm crop frame, zoom/pan only, then exact output sizing.

### Compress to 50KB

ToolType.IMAGE

FunctionType.COMPRESSION

Optional crop first, then target-size compression.

### JPEG to PDF under 500KB

ToolType.PDF

FunctionType.IMAGE_TO_PDF

PDF page configuration and PDF target-size processing; image crop is not exposed by the PDF tool definition.

## Design rule

Do not add a new processing algorithm for every dashboard title. Add a new configuration/preset when the underlying operation is already supported.

The same engine must be reusable by multiple tools while the catalog remains explicit about:

- tool identity
- tool type
- function type
- crop behavior
- dimensions
- DPI
- target size
- output format
- other tool-specific constraints

## Reference behavior

Pi7's current tool catalog and workflows are used as behavioral reference only. ImageForge remains native Kotlin/Compose and offline-first; no Pi7 API or web implementation is used.

Pi7's passport workflow exposes size selection, crop, background and download stages, while its resize/compression tools also document crop before the final resize/compression step. Its image-to-PDF workflow separates page size, orientation, margins, ordering and PDF target size. These behaviors inform ImageForge's shared workflow design without coupling ImageForge to the web service.
