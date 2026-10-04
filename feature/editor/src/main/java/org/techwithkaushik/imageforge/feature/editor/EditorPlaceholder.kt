package org.techwithkaushik.imageforge.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun EditorScreen(
    imageUri: String? = null,
    onBack: () -> Unit = {},
    onImport: () -> Unit = {},
) {
    ForgeScreenSurface {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editor") },
                    navigationIcon = {
                        androidx.compose.material3.TextButton(onClick = onBack) {
                            Text("Back")
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Editor", style = MaterialTheme.typography.headlineSmall)
                if (imageUri == null) {
                    Text(
                        "Select an image to begin editing.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ForgeActionCard(
                        title = "Start with an image",
                        description = "Choose an image from device storage. ImageForge keeps processing on-device.",
                        actionLabel = "Import",
                        onAction = onImport,
                    )
                } else {
                    Text(
                        "Image imported",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        imageUri,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Image processing tools will use the core processing contract.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
