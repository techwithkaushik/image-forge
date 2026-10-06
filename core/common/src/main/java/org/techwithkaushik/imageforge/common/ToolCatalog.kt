package org.techwithkaushik.imageforge.common

/**
 * Stable processing families shared by dashboard tools.
 *
 * A dashboard tool is a product-level entry point, not a separate algorithm.
 * Multiple tools can therefore reuse one family engine with different presets.
 */
public enum class ProcessingFamily {
    RESIZE,
    COMPRESSION,
    CONVERSION,
    CROP,
    TRANSFORM,
    PASSPORT_ID,
    SIGNATURE,
    OCR,
    PDF,
    EDITING,
    EFFECTS,
    DPI_QUALITY,
    METADATA,
    BATCH,
    AI,
}

/**
 * Stable identifier for a dashboard tool.
 *
 * The display title is intentionally not used for routing.
 */
@JvmInline
public value class ToolId(public val value: String)

public enum class ToolType {
    IMAGE,
    PDF,
    DOCUMENT,
}

public enum class FunctionType {
    RESIZE,
    CROP,
    CROP_RESIZE,
    FIXED_SIZE_CROP_RESIZE,
    COMPRESSION,
    CONVERSION,
    TRANSFORM,
    PASSPORT_PHOTO,
    SIGNATURE,
    EFFECT,
    DPI_QUALITY,
    METADATA,
    OCR,
    IMAGE_TO_PDF,
    PDF_TO_IMAGE,
    BATCH,
    AI,
    EDITING,
}

public enum class DimensionUnit {
    PIXEL,
    MM,
    CM,
    INCH,
}

public enum class CropMode {
    DISABLED,
    FLEXIBLE,
    FIXED,
}

public data class CropConfiguration(
    val mode: CropMode = CropMode.FLEXIBLE,
    val width: Double? = null,
    val height: Double? = null,
    val unit: DimensionUnit = DimensionUnit.PIXEL,
    val aspectRatio: Pair<Int, Int>? = null,
    val allowFreeCrop: Boolean = true,
    val allowRatioCrop: Boolean = true,
    val allowDimensionCrop: Boolean = true,
    val allowZoom: Boolean = true,
    val allowPan: Boolean = true,
    val allowFrameResize: Boolean = true,
) {
    init {
        if (mode == CropMode.FIXED) {
            require(width != null && width > 0)
            require(height != null && height > 0)
            require(!allowFrameResize)
        }
    }

    public companion object {
        public val disabled: CropConfiguration = CropConfiguration(
            mode = CropMode.DISABLED,
            allowFreeCrop = false,
            allowRatioCrop = false,
            allowDimensionCrop = false,
            allowZoom = false,
            allowPan = false,
            allowFrameResize = false,
        )

        public val flexible: CropConfiguration = CropConfiguration()

        public fun fixed(
            width: Double,
            height: Double,
            unit: DimensionUnit,
            aspectRatio: Pair<Int, Int>? = null,
        ): CropConfiguration = CropConfiguration(
            mode = CropMode.FIXED,
            width = width,
            height = height,
            unit = unit,
            aspectRatio = aspectRatio,
            allowFreeCrop = false,
            allowRatioCrop = false,
            allowDimensionCrop = false,
            allowZoom = true,
            allowPan = true,
            allowFrameResize = false,
        )
    }
}

public data class ToolConfiguration(
    val crop: CropConfiguration = CropConfiguration.flexible,
    val outputWidth: Double? = null,
    val outputHeight: Double? = null,
    val outputUnit: DimensionUnit = DimensionUnit.PIXEL,
    val outputDpi: Int? = null,
    val targetKb: Long? = null,
    val outputMimeType: String? = null,
    val presetLabel: String? = null,
)

public enum class ToolDestination {
    EDITOR,
    DOCUMENTS,
}

public enum class ToolCapability {
    IMPORT,
    RESIZE,
    CROP,
    COMPRESS,
    CONVERT,
    TRANSFORM,
    PASSPORT,
    SIGNATURE,
    OCR,
    PDF,
    EDIT,
    EFFECT,
    DPI,
    METADATA,
    BATCH,
    AI,
}

/**
 * Configuration describing what a tool needs from a shared processing family.
 */
public data class ToolDefinition(
    val id: ToolId,
    val title: String,
    val family: ProcessingFamily,
    val capabilities: Set<ToolCapability>,
    val destination: ToolDestination = ToolDestination.EDITOR,
    val preset: String? = null,
    val toolType: ToolType = ToolType.IMAGE,
    val functionType: FunctionType = FunctionType.EDITING,
    val configuration: ToolConfiguration = ToolConfiguration(),
)

/**
 * Single source of truth for tool routing and family selection.
 *
 * The initial catalog is intentionally generated from the existing dashboard
 * titles so the migration can happen without changing the visible catalog.
 * New tools should be registered here with an explicit stable id/preset.
 */
public object ToolCatalog {
    public fun definition(title: String): ToolDefinition {
        val normalized = title.trim()
        val key = normalized
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')

        return when {
            normalized.contains("PDF", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.PDF,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.PDF),
                destination = ToolDestination.EDITOR,
            )

            normalized.contains("OCR", ignoreCase = true) ||
                normalized.endsWith(" to Text", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.OCR,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.OCR),
                destination = ToolDestination.EDITOR,
            )

            normalized.contains("Passport", ignoreCase = true) ||
                normalized.contains("PAN Card", ignoreCase = true) ||
                normalized.contains("SSC Photo", ignoreCase = true) ||
                normalized.contains("UPSC Photo", ignoreCase = true) ||
                normalized.contains("PSC Photo", ignoreCase = true) ||
                normalized.contains("35mm", ignoreCase = true) ||
                normalized.contains("3.5cm", ignoreCase = true) ||
                normalized.contains("2 x 2 Inch", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.PASSPORT_ID,
                capabilities = setOf(
                    ToolCapability.IMPORT,
                    ToolCapability.RESIZE,
                    ToolCapability.CROP,
                    ToolCapability.PASSPORT,
                ),
                preset = normalized,
            )

            normalized.contains("Signature", ignoreCase = true) ||
                normalized.contains("Sign", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.SIGNATURE,
                capabilities = setOf(
                    ToolCapability.IMPORT,
                    ToolCapability.RESIZE,
                    ToolCapability.SIGNATURE,
                ),
                preset = normalized,
            )

            normalized.contains("Compress", ignoreCase = true) ||
                normalized.contains("KB", ignoreCase = true) ||
                normalized.contains("MB", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.COMPRESSION,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.COMPRESS),
                preset = normalized,
            )

            normalized.contains("Converter", ignoreCase = true) ||
                normalized.contains(" to JPG", ignoreCase = true) ||
                normalized.contains(" to JPEG", ignoreCase = true) ||
                normalized.contains(" to PNG", ignoreCase = true) ||
                normalized.contains(" to WEBP", ignoreCase = true) ||
                normalized.contains(" to ICO", ignoreCase = true) ||
                normalized.contains("Favicon", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.CONVERSION,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.CONVERT),
                preset = normalized,
            )

            normalized.contains("AI", ignoreCase = true) ||
                normalized.contains("Upscale", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.AI,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.AI),
                preset = normalized,
            )

            normalized.contains("DPI", ignoreCase = true) ||
                normalized.contains("Quality", ignoreCase = true) ||
                normalized.contains("Super Resolution", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.DPI_QUALITY,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.DPI),
                preset = normalized,
            )

            normalized.contains("Crop", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.CROP,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.CROP),
                preset = normalized,
            )

            normalized.contains("Rotate", ignoreCase = true) ||
                normalized.contains("Flip", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.TRANSFORM,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.TRANSFORM),
                preset = normalized,
            )

            normalized.contains("Blur", ignoreCase = true) ||
                normalized.contains("Pixelate", ignoreCase = true) ||
                normalized.contains("Censor", ignoreCase = true) ||
                normalized.contains("Grayscale", ignoreCase = true) ||
                normalized.contains("Black & White", ignoreCase = true) ||
                normalized.contains("Retouch", ignoreCase = true) ||
                normalized.contains("Beautify", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.EFFECTS,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.EFFECT),
                preset = normalized,
            )

            normalized.contains("Metadata", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.METADATA,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.METADATA),
                preset = normalized,
            )

            normalized.contains("Resize", ignoreCase = true) ||
                normalized.contains("Size", ignoreCase = true) ||
                normalized.contains("Instagram", ignoreCase = true) ||
                normalized.contains("WhatsApp", ignoreCase = true) ||
                normalized.contains("YouTube", ignoreCase = true) -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.RESIZE,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.RESIZE),
                preset = normalized,
            )

            else -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.EDITING,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.EDIT),
                preset = normalized,
            )
        }
    }
}
