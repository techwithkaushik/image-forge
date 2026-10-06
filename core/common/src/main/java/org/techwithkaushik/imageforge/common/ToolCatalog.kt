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
        val lower = normalized.lowercase()

        if (normalized.isBlank()) {
            return ToolDefinition(
                id = ToolId("unknown"),
                title = normalized,
                family = ProcessingFamily.EDITING,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.EDIT),
                toolType = ToolType.IMAGE,
                functionType = FunctionType.EDITING,
            )
        }

        return when {
            lower.contains("pdf") && lower.contains("to jpg") -> imageOrPdfDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.PDF,
                functionType = FunctionType.PDF_TO_IMAGE,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.PDF),
                toolType = ToolType.PDF,
                crop = CropConfiguration.disabled,
                outputMimeType = "image/jpeg",
            )

            lower.contains("pdf") -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.PDF,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.PDF),
                destination = ToolDestination.EDITOR,
                preset = normalized,
                toolType = ToolType.PDF,
                functionType = FunctionType.IMAGE_TO_PDF,
                configuration = ToolConfiguration(
                    crop = CropConfiguration.disabled,
                    targetKb = targetKbPreset(normalized),
                    presetLabel = normalized,
                ),
            )

            lower.contains("ocr") ||
                lower.endsWith(" to text") -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.OCR,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.OCR),
                destination = ToolDestination.EDITOR,
                preset = normalized,
                toolType = ToolType.DOCUMENT,
                functionType = FunctionType.OCR,
                configuration = ToolConfiguration(crop = CropConfiguration.disabled, presetLabel = normalized),
            )

            lower.contains("text to handwriting") ||
                lower.contains("image to word") -> ToolDefinition(
                id = ToolId(key),
                title = normalized,
                family = ProcessingFamily.EDITING,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.EDIT),
                destination = ToolDestination.DOCUMENTS,
                preset = normalized,
                toolType = ToolType.DOCUMENT,
                functionType = FunctionType.EDITING,
                configuration = ToolConfiguration(crop = CropConfiguration.disabled, presetLabel = normalized),
            )

            lower.contains("passport photo maker") -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.PASSPORT_ID,
                functionType = FunctionType.PASSPORT_PHOTO,
                capabilities = setOf(
                    ToolCapability.IMPORT,
                    ToolCapability.RESIZE,
                    ToolCapability.CROP,
                    ToolCapability.PASSPORT,
                ),
                configuration = ToolConfiguration(
                    crop = CropConfiguration.flexible,
                    presetLabel = normalized,
                ),
            )

            lower.contains("passport") ||
                lower.contains("pan card") ||
                lower.contains("ssc photo") ||
                lower.contains("upsc photo") ||
                lower.contains("psc photo") ||
                lower.contains("35mm") ||
                lower.contains("3.5cm") ||
                lower.contains("2 x 2 inch") -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.PASSPORT_ID,
                functionType = FunctionType.FIXED_SIZE_CROP_RESIZE,
                capabilities = setOf(
                    ToolCapability.IMPORT,
                    ToolCapability.RESIZE,
                    ToolCapability.CROP,
                    ToolCapability.PASSPORT,
                ),
                configuration = fixedSizeConfiguration(normalized),
            )

            lower.contains("signature") || lower.contains("sign") -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.SIGNATURE,
                functionType = FunctionType.SIGNATURE,
                capabilities = setOf(
                    ToolCapability.IMPORT,
                    ToolCapability.RESIZE,
                    ToolCapability.CROP,
                    ToolCapability.SIGNATURE,
                ),
                configuration = signatureConfiguration(normalized),
            )

            lower.contains("compress") ||
                Regex("""\b\d+(?:\.\d+)?\s*(?:kb|mb)\b""").containsMatchIn(lower) -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.COMPRESSION,
                functionType = FunctionType.COMPRESSION,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.CROP, ToolCapability.COMPRESS),
                configuration = ToolConfiguration(
                    crop = CropConfiguration.flexible,
                    targetKb = targetKbPreset(normalized),
                    presetLabel = normalized,
                ),
            )

            lower.contains("converter") ||
                lower.contains(" to jpg") ||
                lower.contains(" to jpeg") ||
                lower.contains(" to png") ||
                lower.contains(" to webp") ||
                lower.contains(" to ico") ||
                lower.contains("favicon") -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.CONVERSION,
                functionType = FunctionType.CONVERSION,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.CROP, ToolCapability.CONVERT),
                configuration = ToolConfiguration(
                    crop = CropConfiguration.flexible,
                    outputMimeType = conversionMime(normalized),
                    presetLabel = normalized,
                ),
            )

            lower.contains("ai") || lower.contains("upscale") -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.AI,
                functionType = FunctionType.AI,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.CROP, ToolCapability.AI),
                configuration = ToolConfiguration(crop = CropConfiguration.flexible, presetLabel = normalized),
            )

            lower.contains("dpi") ||
                lower.contains("quality") ||
                lower.contains("super resolution") -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.DPI_QUALITY,
                functionType = FunctionType.DPI_QUALITY,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.CROP, ToolCapability.DPI),
                configuration = ToolConfiguration(crop = CropConfiguration.flexible, presetLabel = normalized),
            )

            lower.contains("crop") -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.CROP,
                functionType = FunctionType.CROP,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.CROP),
                configuration = ToolConfiguration(crop = CropConfiguration.flexible, presetLabel = normalized),
            )

            lower.contains("rotate") || lower.contains("flip") -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.TRANSFORM,
                functionType = FunctionType.TRANSFORM,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.CROP, ToolCapability.TRANSFORM),
                configuration = ToolConfiguration(crop = CropConfiguration.flexible, presetLabel = normalized),
            )

            lower.contains("blur") ||
                lower.contains("pixelate") ||
                lower.contains("censor") ||
                lower.contains("grayscale") ||
                lower.contains("black & white") ||
                lower.contains("retouch") ||
                lower.contains("beautify") -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.EFFECTS,
                functionType = FunctionType.EFFECT,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.CROP, ToolCapability.EFFECT),
                configuration = ToolConfiguration(crop = CropConfiguration.flexible, presetLabel = normalized),
            )

            lower.contains("metadata") -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.METADATA,
                functionType = FunctionType.METADATA,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.CROP, ToolCapability.METADATA),
                configuration = ToolConfiguration(crop = CropConfiguration.flexible, presetLabel = normalized),
            )

            lower.contains("resize") ||
                lower.contains("size") ||
                lower.contains("instagram") ||
                lower.contains("whatsapp") ||
                lower.contains("youtube") ||
                lower.contains("a4") -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.RESIZE,
                functionType = FunctionType.CROP_RESIZE,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.RESIZE, ToolCapability.CROP),
                configuration = resizeConfiguration(normalized),
            )

            else -> imageDefinition(
                key = key,
                title = normalized,
                family = ProcessingFamily.EDITING,
                functionType = FunctionType.EDITING,
                capabilities = setOf(ToolCapability.IMPORT, ToolCapability.CROP, ToolCapability.EDIT),
                configuration = ToolConfiguration(crop = CropConfiguration.flexible, presetLabel = normalized),
            )
        }
    }

    private fun imageDefinition(
        key: String,
        title: String,
        family: ProcessingFamily,
        functionType: FunctionType,
        capabilities: Set<ToolCapability>,
        configuration: ToolConfiguration,
    ): ToolDefinition = ToolDefinition(
        id = ToolId(key),
        title = title,
        family = family,
        capabilities = capabilities,
        destination = ToolDestination.EDITOR,
        preset = title,
        toolType = ToolType.IMAGE,
        functionType = functionType,
        configuration = configuration,
    )

    private fun imageOrPdfDefinition(
        key: String,
        title: String,
        family: ProcessingFamily,
        functionType: FunctionType,
        capabilities: Set<ToolCapability>,
        toolType: ToolType,
        crop: CropConfiguration,
        outputMimeType: String?,
    ): ToolDefinition = ToolDefinition(
        id = ToolId(key),
        title = title,
        family = family,
        capabilities = capabilities,
        destination = ToolDestination.EDITOR,
        preset = title,
        toolType = toolType,
        functionType = functionType,
        configuration = ToolConfiguration(
            crop = crop,
            outputMimeType = outputMimeType,
            presetLabel = title,
        ),
    )

    private fun resizeConfiguration(title: String): ToolConfiguration {
        val lower = title.lowercase()
        if ("instagram (no crop)" in lower) {
            return ToolConfiguration(
                crop = CropConfiguration.disabled,
                outputWidth = 1080.0,
                outputHeight = 1080.0,
                presetLabel = title,
            )
        }
        val dimensions = fixedDimensions(title)
        return if (dimensions != null) {
            ToolConfiguration(
                crop = CropConfiguration.fixed(
                    width = dimensions.first,
                    height = dimensions.second,
                    unit = dimensions.third,
                    aspectRatio = aspectRatio(dimensions.first, dimensions.second),
                ),
                outputWidth = dimensions.first,
                outputHeight = dimensions.second,
                outputUnit = dimensions.third,
                outputDpi = dpiPreset(title),
                presetLabel = title,
            )
        } else {
            ToolConfiguration(crop = CropConfiguration.flexible, presetLabel = title)
        }
    }

    private fun fixedSizeConfiguration(title: String): ToolConfiguration {
        val dimensions = fixedDimensions(title) ?: return ToolConfiguration(
            crop = CropConfiguration.flexible,
            presetLabel = title,
        )
        return ToolConfiguration(
            crop = CropConfiguration.fixed(
                width = dimensions.first,
                height = dimensions.second,
                unit = dimensions.third,
                aspectRatio = aspectRatio(dimensions.first, dimensions.second),
            ),
            outputWidth = dimensions.first,
            outputHeight = dimensions.second,
            outputUnit = dimensions.third,
            outputDpi = dpiPreset(title) ?: 300,
            presetLabel = title,
        )
    }

    private fun signatureConfiguration(title: String): ToolConfiguration {
        val dimensions = fixedDimensions(title)
        return ToolConfiguration(
            crop = if (dimensions != null) {
                CropConfiguration.fixed(
                    dimensions.first,
                    dimensions.second,
                    dimensions.third,
                    aspectRatio(dimensions.first, dimensions.second),
                )
            } else {
                CropConfiguration.flexible
            },
            outputWidth = dimensions?.first,
            outputHeight = dimensions?.second,
            outputUnit = dimensions?.third ?: DimensionUnit.PIXEL,
            outputDpi = dpiPreset(title),
            presetLabel = title,
        )
    }

    private fun fixedDimensions(title: String): Triple<Double, Double, DimensionUnit>? {
        val lower = title.lowercase()
        return when {
            "35mm x 45mm" in lower || "3.5cm x 4.5cm" in lower ->
                Triple(35.0, 45.0, DimensionUnit.MM)
            "signature 50mm x 20mm" in lower ->
                Triple(50.0, 20.0, DimensionUnit.MM)
            "6cm x 2cm" in lower ->
                Triple(6.0, 2.0, DimensionUnit.CM)
            "2 x 2 inch" in lower ->
                Triple(2.0, 2.0, DimensionUnit.INCH)
            "3 x 4 inch" in lower ->
                Triple(3.0, 4.0, DimensionUnit.INCH)
            "4 x 6 inch" in lower ->
                Triple(4.0, 6.0, DimensionUnit.INCH)
            "600x600" in lower ->
                Triple(600.0, 600.0, DimensionUnit.PIXEL)
            "a4" in lower ->
                Triple(210.0, 297.0, DimensionUnit.MM)
            else -> null
        }
    }

    private fun dpiPreset(title: String): Int? {
        val match = Regex("""(\d+)\s*dpi""").find(title.lowercase())
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun targetKbPreset(title: String): Long? {
        val match = Regex("""(\d+(?:\.\d+)?)\s*(kb|mb)""")
            .find(title.lowercase()) ?: return null
        val value = match.groupValues[1].toDouble()
        return if (match.groupValues[2] == "mb") (value * 1024.0).toLong() else value.toLong()
    }

    private fun conversionMime(title: String): String? = when {
        title.contains("PNG", ignoreCase = true) -> "image/png"
        title.contains("WEBP", ignoreCase = true) -> "image/webp"
        title.contains("JPG", ignoreCase = true) ||
            title.contains("JPEG", ignoreCase = true) ||
            title.contains("JFIF", ignoreCase = true) -> "image/jpeg"
        else -> null
    }

    private fun aspectRatio(width: Double, height: Double): Pair<Int, Int>? {
        if (width <= 0 || height <= 0) return null
        val scale = 1000.0
        val w = kotlin.math.round(width / kotlin.math.min(width, height) * scale).toInt()
        val h = kotlin.math.round(height / kotlin.math.min(width, height) * scale).toInt()
        val gcd = gcd(w, h)
        return (w / gcd) to (h / gcd)
    }

    private fun gcd(a: Int, b: Int): Int =
        if (b == 0) kotlin.math.abs(a) else gcd(b, a % b)
}
