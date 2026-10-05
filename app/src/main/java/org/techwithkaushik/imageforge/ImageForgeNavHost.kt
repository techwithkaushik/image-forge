package org.techwithkaushik.imageforge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.techwithkaushik.imageforge.feature.dashboard.DashboardScreen
import org.techwithkaushik.imageforge.feature.documents.DocumentsScreen
import org.techwithkaushik.imageforge.feature.editor.EditorScreen

@Serializable
private data object DashboardRoute

@Serializable
private data class EditorRoute(val imageUri: String? = null, val tool: String? = null)

@Serializable
private data object DocumentsRoute

@Composable
internal fun ImageForgeNavHost(
    onPickImage: () -> Unit,
    importedImageUri: String?,
    onImportedImageConsumed: () -> Unit,
) {
    val navController = rememberNavController()

    LaunchedEffect(importedImageUri) {
        if (importedImageUri != null) {
            navController.navigate(EditorRoute(importedImageUri)) {
                launchSingleTop = true
            }
            onImportedImageConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
    ) {
        composable<DashboardRoute> {
            DashboardScreen(
                onOpenEditor = { tool -> navController.navigate(EditorRoute(tool = tool)) },
                onImportImage = onPickImage,
                onOpenDocuments = { navController.navigate(DocumentsRoute) },
            )
        }
        composable<EditorRoute> { entry ->
            val route = entry.toRoute<EditorRoute>()
            EditorScreen(
                imageUri = route.imageUri,
                tool = route.tool,
                onImport = onPickImage,
                onBack = { navController.popBackStack() },
            )
        }
        composable<DocumentsRoute> {
            DocumentsScreen(onBack = { navController.popBackStack() })
        }
    }
}
