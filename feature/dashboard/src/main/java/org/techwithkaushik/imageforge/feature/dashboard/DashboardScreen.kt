package org.techwithkaushik.imageforge.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.techwithkaushik.imageforge.designsystem.ForgeScreenSurface
import org.techwithkaushik.imageforge.designsystem.ForgeToolTile

private data class ToolSection(
    val title: String,
    val tools: List<String>,
)

private val pi7Sections = listOf(
    ToolSection(
        "Most Used Tools",
        listOf(
            "Passport Photo Maker", "Reduce Image Size in KB",
            "Resize Image Pixel", "Text to Handwriting",
            "Image to Text (OCR)", "Photo Collage Maker",
            "Generate Signature", "Increase Image Size In KB",
            "AI Photo Enhancer", "Resize Signature",
            "Resize Image In Centimeter", "Resize Image (3.5cm x 4.5cm)",
        ),
    ),
    ToolSection(
        "Basic Editing",
        listOf(
            "Blur Background", "Remove Background",
            "Remove Object from Photo", "Add Name & DOB on Photo",
            "Rotate Image", "Flip Image",
            "Watermark Images", "Freehand Crop",
            "Circle Crop", "Square Crop",
            "Round Corners", "Change Aspect Ratio",
            "Merge Photo & Signature", "Join Multiple Images",
            "Split Image", "Image Color Picker",
            "Edit Metadata", "View Metadata",
            "Remove Metadata", "Crop PNG",
        ),
    ),
    ToolSection(
        "Blur, Pixlate and Special Effects",
        listOf(
            "Beautify Image", "Deep Fry Photo",
            "Unblur Image", "Blur Image",
            "Blur Face", "Unblur Face",
            "Add Border To Image", "Pixelate Image",
            "Pixelate Face", "Censor Photo",
            "Motion Blur", "Grayscale Image",
            "Black & White", "Picture to Pixel Art",
            "Add White Border To Image", "AI Face Generator",
            "Blemishes Remover", "Retouch Image",
            "Add Text to Image", "Add Logo to Image",
        ),
    ),
    ToolSection(
        "DPI & Quality",
        listOf(
            "Increase Image Quality", "Convert DPI (200, 300, 600)",
            "Check Image DPI", "Super Resolution",
        ),
    ),
    ToolSection(
        "General Resizing",
        listOf(
            "Resize Image by Pixel", "Resize in Centimeters",
            "Resize in Millimeters", "Resize in Inches",
            "Bulk Image Resizer", "Upscale Image With AI",
        ),
    ),
    ToolSection(
        "Resize Other Official Sizes",
        listOf(
            "A4 Size", "SSC Photo Resize",
            "PAN Card", "UPSC Photo", "PSC Photo",
        ),
    ),
    ToolSection(
        "Passport & ID Photo Sizes",
        listOf(
            "Passport Photo Maker", "Red Background Passport",
            "White Background Passport", "Resize Sign 6cm x 2cm (300 DPI)",
            "3.5cm x 4.5cm", "Signature 50mm x 20mm",
            "35mm x 45mm", "2 x 2 Inch",
            "3 x 4 Inch", "4 x 6 Inch", "600x600 Pixels",
        ),
    ),
    ToolSection(
        "Resize For Social Media",
        listOf(
            "Instagram (No Crop)", "Instagram Grid Maker",
            "WhatsApp DP", "YouTube Banner", "Zoom Out Image",
        ),
    ),
    ToolSection(
        "Format Conversions",
        listOf(
            "Image Converter", "Image to JPG",
            "JPEG to JPG", "HEIC to JPG",
            "WEBP to JPG", "WebP to PNG",
            "AVIF to JPG", "JFIF to JPG",
            "JPEG to PNG", "PNG to JPEG",
            "PNG to ICO", "Image to Word",
            "JPG to Text", "PNG to Text", "Favicon Generator",
        ),
    ),
    ToolSection(
        "Image to PDF",
        listOf(
            "Image to PDF", "PDF to JPG",
            "JPG to PDF (Under 50KB)", "JPG to PDF (Under 100KB)",
            "JPG to PDF (Under 150KB)", "JPEG to PDF (Under 200KB)",
            "JPG to PDF (Under 250KB)", "JPG to PDF (Under 300KB)",
            "JPG to PDF (Under 400KB)", "JPG to PDF (Under 500KB)",
            "JPG to PDF (Under 1MB)", "JPG to PDF (Under 2MB)",
        ),
    ),
    ToolSection(
        "General Compression",
        listOf(
            "Image Compressor", "Reduce Size in KB",
            "Reduce Size in MB", "JPG to KB",
            "Convert MB to KB", "Convert KB to MB",
        ),
    ),
    ToolSection(
        "Exact Target Sizes",
        listOf(
            "Compress to 5KB", "JPEG to 10KB",
            "Compress to 15KB", "Compress to 20KB",
            "Compress 20KB-50KB", "JPEG to 25KB",
            "JPEG to 30KB", "JPEG to 40KB",
            "Compress to 50KB", "Compress to 60KB",
            "Compress to 70KB", "Compress to 80KB",
            "Compress to 90KB", "Resize to 50KB",
            "Compress to 100KB", "JPEG to 150KB",
            "Compress to 200KB", "Resize to 200KB",
            "JPEG to 300KB", "JPEG to 500KB",
            "Compress to 1MB", "Compress to 2MB",
        ),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun DashboardScreen(
    onImportImage: () -> Unit = {},
    onOpenEditor: () -> Unit = {},
    onOpenDocuments: () -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    val normalizedQuery = query.trim()

    ForgeScreenSurface {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "IMAGE TOOL",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                Text(
                    text = "Compress, Resize & Edit Pictures",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Start,
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search Tool") },
                    leadingIcon = { Text("⌕", style = MaterialTheme.typography.headlineSmall) },
                )

                pi7Sections.forEach { section ->
                    val visibleTools = section.tools.filter {
                        normalizedQuery.isBlank() ||
                            it.contains(normalizedQuery, ignoreCase = true)
                    }
                    if (visibleTools.isNotEmpty()) {
                        DashboardToolSection(
                            section = section.copy(tools = visibleTools),
                            onOpenEditor = onOpenEditor,
                            onOpenDocuments = onOpenDocuments,
                            onImportImage = onImportImage,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardToolSection(
    section: ToolSection,
    onOpenEditor: () -> Unit,
    onOpenDocuments: () -> Unit,
    onImportImage: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.headlineSmall,
        )

        section.tools.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                pair.forEach { tool ->
                    ForgeToolTile(
                        title = tool,
                        onClick = {
                            when {
                                tool.contains("PDF", ignoreCase = true) ||
                                    tool.contains("Passport", ignoreCase = true) ||
                                    tool.contains("Signature", ignoreCase = true) ||
                                    tool.contains("OCR", ignoreCase = true) ||
                                    tool.contains("Text", ignoreCase = true) -> onOpenDocuments()
                                tool.contains("Image", ignoreCase = true) ||
                                    tool.contains("Resize", ignoreCase = true) ||
                                    tool.contains("Compress", ignoreCase = true) ||
                                    tool.contains("Convert", ignoreCase = true) -> onOpenEditor()
                                else -> onImportImage()
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size == 1) {
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
