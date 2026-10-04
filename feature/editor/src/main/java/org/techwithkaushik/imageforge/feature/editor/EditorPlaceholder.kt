package org.techwithkaushik.imageforge.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.techwithkaushik.imageforge.designsystem.ForgeActionCard
import org.techwithkaushik.imageforge.designsystem.ForgeScreenSurface

@Composable
public fun EditorScreen(
    onImport: () -> Unit = {},
) {
    ForgeScreenSurface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Editor", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Production image tools will run through the core processing contract.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ForgeActionCard(
                title = "Start with an image",
                description = "Import an image before selecting resize, crop, compression or conversion tools.",
                actionLabel = "Import",
                onAction = onImport,
            )
        }
    }
}
