package org.techwithkaushik.imageforge.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class DashboardSection(
    val title: String,
    val tools: List<String>,
)

private val sections = listOf(
    DashboardSection(
        "Most Used Tools",
        listOf(
            "Passport Photo Maker",
            "Reduce Image Size in KB",
            "Resize Image Pixel",
            "Text to Handwriting",
            "Image to Text (OCR)",
            "Photo Collage Maker",
            "Generate Signature",
            "Increase Image Size In KB",
            "AI Photo Enhancer",
            "Resize Signature",
            "Resize Image In Centimeter",
            "Resize Image (3.5cm x 4.5cm)",
        ),
    ),
    DashboardSection(
        "Basic Editing",
        listOf(
            "Blur Background",
            "Remove Background",
            "Remove Object from Photo",
            "Add Name & DOB on Photo",
            "Rotate Image",
            "Flip Image",
            "Watermark Images",
            "Freehand Crop",
            "Circle Crop",
            "Square Crop",
            "Round Corners",
            "Change Aspect Ratio",
            "Merge Photo & Signature",
            "Join Multiple Images",
            "Split Image",
            "Image Color Picker",
            "Edit Metadata",
            "View Metadata",
            "Remove Metadata",
            "Crop PNG",
        ),
    ),
    DashboardSection(
        "Blur, Pixlate and Special Effects",
        listOf(
            "Beautify Image",
            "Deep Fry Photo",
            "Unblur Image",
            "Blur Image",
            "Blur Face",
            "Unblur Face",
            "Add Border To Image",
            "Pixelate Image",
            "Pixelate Face",
            "Censor Photo",
            "Motion Blur",
            "Grayscale Image",
            "Black & White",
            "Picture to Pixel Art",
            "Add White Border To Image",
            "AI Face Generator",
            "Blemishes Remover",
            "Retouch Image",
            "Add Text to Image",
            "Add Logo to Image",
        ),
    ),
    DashboardSection(
        "DPI & Quality",
        listOf(
            "Increase Image Quality",
            "Convert DPI (200, 300, 600)",
            "Check Image DPI",
            "Super Resolution",
        ),
    ),
    DashboardSection(
        "General Resizing",
        listOf(
            "Resize Image by Pixel",
            "Resize in Centimeters",
            "Resize in Millimeters",
            "Resize in Inches",
            "Bulk Image Resizer",
            "Upscale Image With AI",
        ),
    ),
    DashboardSection(
        "Resize Other Official Sizes",
        listOf(
            "A4 Size",
            "SSC Photo Resize",
            "PAN Card",
            "UPSC Photo",
            "PSC Photo",
        ),
    ),
    DashboardSection(
        "Passport & ID Photo Sizes",
        listOf(
            "Passport Photo Maker",
            "Red Background Passport",
            "White Background Passport",
            "Resize Sign 6cm x 2cm (300 DPI)",
            "3.5cm x 4.5cm",
            "Signature 50mm x 20mm",
            "35mm x 45mm",
            "2 x 2 Inch",
            "3 x 4 Inch",
            "4 x 6 Inch",
            "600x600 Pixels",
        ),
    ),
    DashboardSection(
        "Resize For Social Media",
        listOf(
            "Instagram (No Crop)",
            "Instagram Grid Maker",
            "WhatsApp DP",
            "YouTube Banner",
            "Zoom Out Image",
        ),
    ),
    DashboardSection(
        "Format Conversions",
        listOf(
            "Image Converter",
            "Image to JPG",
            "JPEG to JPG",
            "HEIC to JPG",
            "WEBP to JPG",
            "WebP to PNG",
            "AVIF to JPG",
            "JFIF to JPG",
            "JPEG to PNG",
            "PNG to JPEG",
            "PNG to ICO",
            "Image to Word",
            "JPG to Text",
            "PNG to Text",
            "Favicon Generator",
        ),
    ),
    DashboardSection(
        "Image to PDF",
        listOf(
            "Image to PDF",
            "PDF to JPG",
            "JPG to PDF (Under 50KB)",
            "JPG to PDF (Under 100KB)",
            "JPG to PDF (Under 150KB)",
            "JPEG to PDF (Under 200KB)",
            "JPG to PDF (Under 250KB)",
            "JPG to PDF (Under 300KB)",
            "JPG to PDF (Under 400KB)",
            "JPG to PDF (Under 500KB)",
            "JPG to PDF (Under 1MB)",
            "JPG to PDF (Under 2MB)",
        ),
    ),
    DashboardSection(
        "General Compression",
        listOf(
            "Image Compressor",
            "Reduce Size in KB",
            "Reduce Size in MB",
            "JPG to KB",
            "Convert MB to KB",
            "Convert KB to MB",
        ),
    ),
    DashboardSection(
        "Exact Target Sizes",
        listOf(
            "Compress to 5KB",
            "JPEG to 10KB",
            "Compress to 15KB",
            "Compress to 20KB",
            "Compress 20KB-50KB",
            "JPEG to 25KB",
            "JPEG to 30KB",
            "JPEG to 40KB",
            "Compress to 50KB",
            "Compress to 60KB",
            "Compress to 70KB",
            "Compress to 80KB",
            "Compress to 90KB",
            "Resize to 50KB",
            "Compress to 100KB",
            "JPEG to 150KB",
            "Compress to 200KB",
            "Resize to 200KB",
            "JPEG to 300KB",
            "JPEG to 500KB",
            "Compress to 1MB",
            "Compress to 2MB",
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
    val filteredSections = remember(query) {
        val normalized = query.trim()
        if (normalized.isEmpty()) {
            sections
        } else {
            sections.mapNotNull { section ->
                val tools = section.tools.filter { it.contains(normalized, ignoreCase = true) }
                if (tools.isEmpty()) null else section.copy(tools = tools)
            }
        }
    }

    ForgeDashboardSurface {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Pi",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Pi7 IMAGE TOOL")
                        }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 28.dp,
                    bottom = 40.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                item {
                    Text(
                        "Compress, Resize & Edit Pictures",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(22.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Search Tool") },
                    )
                }

                if (query.isBlank()) {
                    item {
                        OutlinedButton(
                            onClick = onImportImage,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Select Image / Start")
                        }
                    }
                }

                items(
                    items = filteredSections,
                    key = { it.title },
                ) { section ->
                    DashboardToolSection(
                        section = section,
                        onToolClick = { tool ->
                            if (tool.contains("PDF", ignoreCase = true) ||
                                tool.contains("OCR", ignoreCase = true) ||
                                tool.contains("Passport", ignoreCase = true) ||
                                tool.contains("Signature", ignoreCase = true) ||
                                tool.contains("Text", ignoreCase = true)
                            ) {
                                onOpenDocuments()
                            } else {
                                onOpenEditor()
                            }
                        },
                    )
                }

                if (filteredSections.isEmpty()) {
                    item {
                        Text(
                            "No tools found for "$query".",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardToolSection(
    section: DashboardSection,
    onToolClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            section.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        section.tools.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                row.forEach { tool ->
                    OutlinedButton(
                        onClick = { onToolClick(tool) },
                        modifier = Modifier
                            .weight(1f)
                            .height(78.dp),
                    ) {
                        Text(
                            tool,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                if (row.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ForgeDashboardSurface(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
        content = content,
    )
}
