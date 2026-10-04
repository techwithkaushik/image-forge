package org.techwithkaushik.imageforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.techwithkaushik.imageforge.designsystem.ImageForgeTheme
import org.techwithkaushik.imageforge.feature.dashboard.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageForgeTheme {
                DashboardScreen()
            }
        }
    }
}
