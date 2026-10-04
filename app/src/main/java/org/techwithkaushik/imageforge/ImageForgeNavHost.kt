package org.techwithkaushik.imageforge

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.techwithkaushik.imageforge.feature.dashboard.DashboardScreen
import org.techwithkaushik.imageforge.feature.documents.DocumentsScreen
import org.techwithkaushik.imageforge.feature.editor.EditorScreen

@Serializable
private data object DashboardRoute

@Serializable
private data object EditorRoute

@Serializable
private data object DocumentsRoute

@Composable
internal fun ImageForgeNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = DashboardRoute,
    ) {
        composable<DashboardRoute> {
            DashboardScreen(
                onOpenEditor = { navController.navigate(EditorRoute) },
                onImportImage = { navController.navigate(EditorRoute) },
            )
        }
        composable<EditorRoute> {
            EditorScreen()
        }
        composable<DocumentsRoute> {
            DocumentsScreen()
        }
    }
}
