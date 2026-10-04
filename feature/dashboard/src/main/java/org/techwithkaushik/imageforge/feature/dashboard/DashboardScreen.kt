package org.techwithkaushik.imageforge.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.techwithkaushik.imageforge.designsystem.ForgeActionCard
import org.techwithkaushik.imageforge.designsystem.ForgeScreenSurface

@Composable
public fun DashboardScreen(
    onImportImage: () -> Unit = {},
    onOpenEditor: () -> Unit = {},
) {
    ForgeScreenSurface {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ImageForge") },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Offline image studio",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    "Compress, resize, crop and convert images on your device.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ForgeActionCard(
                    title = "Import an image",
                    description = "Choose an image from device storage and start processing locally.",
                    actionLabel = "Import",
                    onAction = onImportImage,
                )
                ForgeActionCard(
                    title = "Image tools",
                    description = "The editor will provide production image operations without uploading your files.",
                    actionLabel = "Open editor",
                    onAction = onOpenEditor,
                )
                Text(
                    "Your images stay on your device during processing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
