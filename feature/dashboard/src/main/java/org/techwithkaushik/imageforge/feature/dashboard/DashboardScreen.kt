package org.techwithkaushik.imageforge.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.techwithkaushik.imageforge.designsystem.ForgeActionCard
import org.techwithkaushik.imageforge.designsystem.ForgeScreenSurface
import org.techwithkaushik.imageforge.designsystem.ForgeStatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun DashboardScreen(
    onImportImage: () -> Unit = {},
    onOpenEditor: () -> Unit = {},
    onOpenDocuments: () -> Unit = {},
) {
    ForgeScreenSurface {
        Scaffold(
            topBar = { TopAppBar(title = { Text("ImageForge") }) },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Compress, Resize & Edit Pictures",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    ForgeStatusPill("100% on-device")
                    Text(
                        "Pi7-inspired tool organization, rebuilt for ImageForge's offline Android workflow.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                DashboardSection(
                    title = "Most Used Tools",
                    description = "Start with the workflows people use most.",
                ) {
                    ForgeActionCard(
                        title = "Resize & Compress",
                        description = "Resize by pixels, dimensions or target file size.",
                        actionLabel = "Open",
                        onAction = onOpenEditor,
                    )
                    ForgeActionCard(
                        title = "Passport & Signature",
                        description = "Prepare passport photos and signatures for forms and applications.",
                        actionLabel = "Open",
                        onAction = onOpenDocuments,
                    )
                    ForgeActionCard(
                        title = "Image to Text (OCR)",
                        description = "Extract text locally with bundled offline OCR.",
                        actionLabel = "Open",
                        onAction = onOpenDocuments,
                    )
                    ForgeActionCard(
                        title = "Photo & Signature",
                        description = "Prepare photo and signature together for document workflows.",
                        actionLabel = "Open",
                        onAction = onOpenDocuments,
                    )
                }

                DashboardSection(
                    title = "Basic Editing",
                    description = "Everyday image adjustments without leaving the app.",
                ) {
                    ForgeActionCard(
                        title = "Crop, Rotate & Flip",
                        description = "Crop images and apply orientation changes.",
                        actionLabel = "Open editor",
                        onAction = onOpenEditor,
                    )
                    ForgeActionCard(
                        title = "Merge & Split",
                        description = "Combine images or split image content into separate outputs.",
                        actionLabel = "Open tools",
                        onAction = onOpenDocuments,
                    )
                    ForgeActionCard(
                        title = "Background & Object Tools",
                        description = "Use available local vision processing for supported workflows.",
                        actionLabel = "Open tools",
                        onAction = onOpenEditor,
                    )
                }

                DashboardSection(
                    title = "DPI & Quality",
                    description = "Prepare images for print and portal requirements.",
                ) {
                    ForgeActionCard(
                        title = "Image Quality",
                        description = "Resize and process images while preserving practical output quality.",
                        actionLabel = "Open editor",
                        onAction = onOpenEditor,
                    )
                    ForgeActionCard(
                        title = "Metadata",
                        description = "View or remove privacy-sensitive EXIF metadata locally.",
                        actionLabel = "Open tools",
                        onAction = onOpenEditor,
                    )
                }

                DashboardSection(
                    title = "Official & Passport Sizes",
                    description = "Common document, ID and application dimensions.",
                ) {
                    ForgeActionCard(
                        title = "A4 & Document",
                        description = "Prepare images for A4 and document-oriented workflows.",
                        actionLabel = "Open documents",
                        onAction = onOpenDocuments,
                    )
                    ForgeActionCard(
                        title = "Passport / ID Photo",
                        description = "Use standard passport and ID-oriented image preparation.",
                        actionLabel = "Open documents",
                        onAction = onOpenDocuments,
                    )
                    ForgeActionCard(
                        title = "Signature",
                        description = "Resize and compress signatures for application portals.",
                        actionLabel = "Open documents",
                        onAction = onOpenDocuments,
                    )
                }

                DashboardSection(
                    title = "Format Conversion",
                    description = "Convert supported image formats locally.",
                ) {
                    ForgeActionCard(
                        title = "Image Converter",
                        description = "Convert supported images to common output formats.",
                        actionLabel = "Open editor",
                        onAction = onOpenEditor,
                    )
                    ForgeActionCard(
                        title = "Image to PDF",
                        description = "Create PDF output from selected images.",
                        actionLabel = "Open documents",
                        onAction = onOpenDocuments,
                    )
                }

                DashboardSection(
                    title = "Exact Target Sizes",
                    description = "Portal-friendly compression targets such as 20KB, 50KB and 100KB.",
                ) {
                    ForgeActionCard(
                        title = "Compress to Target Size",
                        description = "Set a target file size and process locally within supported limits.",
                        actionLabel = "Open editor",
                        onAction = onOpenEditor,
                    )
                    ForgeActionCard(
                        title = "Batch Image Processing",
                        description = "Process multiple images sequentially with progress and cancellation support.",
                        actionLabel = "Open editor",
                        onAction = onOpenEditor,
                    )
                }

                Text(
                    "Private by default: ImageForge processes core image workflows on the device and does not require a network connection.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DashboardSection(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            content()
        }
    }
}
