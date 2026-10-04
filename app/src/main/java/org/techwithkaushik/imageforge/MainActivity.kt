package org.techwithkaushik.imageforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.techwithkaushik.imageforge.designsystem.ImageForgeTheme
import org.techwithkaushik.imageforge.feature.dashboard.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageForgeApp()
        }
    }
}

@Composable
private fun ImageForgeApp() {
    ImageForgeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DashboardScreen()
        }
    }
}
