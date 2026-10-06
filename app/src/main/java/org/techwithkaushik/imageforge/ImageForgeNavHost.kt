package org.techwithkaushik.imageforge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.techwithkaushik.imageforge.common.ProcessingFamily
import org.techwithkaushik.imageforge.common.ToolDefinition
import org.techwithkaushik.imageforge.common.ToolId
import org.techwithkaushik.imageforge.feature.dashboard.DashboardScreen
import org.techwithkaushik.imageforge.feature.documents.DocumentsScreen
import org.techwithkaushik.imageforge.feature.editor.EditorScreen

@Serializable
private data object DashboardRoute

@Serializable
private data class EditorRoute(
    val imageUri: String? = null,
    val toolId: String? = null,
    val toolTitle: String? = null,
    val family: String? = null,
    val preset: String? = null,
)

@Serializable
private data object DocumentsRoute

@Composable
internal fun ImageForgeNavHost(
    onPickImage: () -> Unit,
    importedImageUri: String?,
    onImportedImageConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    var pendingTool: ToolDefinition? = null

    LaunchedEffect(importedImageUri) {
        if (importedImageUri != null) {
            val tool = pendingTool
            navController.navigate(
                EditorRoute(
                    imageUri = importedImageUri,
                    toolId = tool?.id?.value,
                    toolTitle = tool?.title,
                    family = tool?.family?.name,
                    preset = tool?.preset,
                ),
            ) {
                launchSingleTop = true
            }
            pendingTool = null
            onImportedImageConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
    ) {
        composable<DashboardRoute> {
            DashboardScreen(
                onOpenTool = { tool ->
                    if (tool.destination.name == "DOCUMENTS") {
                        navController.navigate(DocumentsRoute)
                    } else {
                        pendingTool = tool
                        navController.navigate(
                            EditorRoute(
                                toolId = tool.id.value,
                                toolTitle = tool.title,
                                family = tool.family.name,
                                preset = tool.preset,
                            ),
                        )
                    }
                },
                onImportImage = onPickImage,
            )
        }

        composable<EditorRoute> { entry ->
            val route = entry.toRoute<EditorRoute>()
            val tool = route.toolId?.let { id ->
                ToolDefinition(
                    id = ToolId(id),
                    title = route.toolTitle ?: id,
                    family = route.family?.let { ProcessingFamily.valueOf(it) }
                        ?: ProcessingFamily.EDITING,
                    capabilities = emptySet(),
                    preset = route.preset,
                )
            }
            pendingTool = tool
            EditorScreen(
                imageUri = route.imageUri,
                tool = tool,
                onImport = onPickImage,
                onBack = { navController.popBackStack() },
            )
        }

        composable<DocumentsRoute> {
            DocumentsScreen(onBack = { navController.popBackStack() })
        }
    }
}
