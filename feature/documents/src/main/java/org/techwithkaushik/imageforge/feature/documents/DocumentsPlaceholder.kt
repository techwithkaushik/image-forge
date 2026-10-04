package org.techwithkaushik.imageforge.feature.documents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
public fun DocumentsScreen(
    onBack: () -> Unit = {},
    onOpenDocumentTools: () -> Unit = {},
) {
    ForgeScreenSurface {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Documents") },
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
            Text("Documents", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Document-oriented tools stay offline and use the same processing pipeline.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ForgeActionCard(
                title = "Document tools",
                description = "Passport, signature, PDF and OCR workflows will be added in their dedicated phases.",
                actionLabel = "Explore",
                onAction = onOpenDocumentTools,
            )
        }
        }
    }
}
