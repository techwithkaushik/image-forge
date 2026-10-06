package org.techwithkaushik.imageforge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.techwithkaushik.imageforge.common.ToolCatalog
import org.techwithkaushik.imageforge.common.ToolDefinition
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
            navController.navigate(EditorRoute(
                imageUri = importedImageUri,
                toolId = tool?.id?.value,
                toolTitle = tool?.title,
            )) {
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
                onOpenEditor = { tool ->
                    pendingTool = tool
                    navController.navigate(EditorRoute(toolId = tool.id.value, toolTitle = tool.title))
                },
                onImportImage = onPickImage,
                onOpenDocuments = { navController.navigate(DocumentsRoute) },
            )
        }
        composable<EditorRoute> { entry ->
            val route = entry.toRoute<EditorRoute>()
            pendingTool = route.toolTitle?.let(ToolCatalog::definition)
            EditorScreen(
                imageUri = route.imageUri,
                toolId = route.toolId,
                onImport = onPickImage,
                onBack = { navController.popBackStack() },
            )
        }
        composable<DocumentsRoute> {
            DocumentsScreen(onBack = { navController.popBackStack() })
        }
    }
}
