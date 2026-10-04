package org.techwithkaushik.imageforge

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.techwithkaushik.imageforge.designsystem.ImageForgeTheme

class MainActivity : ComponentActivity() {
    private var pendingImageUri by mutableStateOf<String?>(null)

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            runCatching { contentResolver.takePersistableUriPermission(uri, flags) }
            pendingImageUri = uri.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageForgeTheme {
                ImageForgeNavHost(
                    onPickImage = {
                        imagePicker.launch(
                            arrayOf(
                                "image/jpeg",
                                "image/png",
                                "image/webp",
                                "image/heic",
                                "image/heif",
                            ),
                        )
                    },
                    importedImageUri = pendingImageUri,
                    onImportedImageConsumed = { pendingImageUri = null },
                )
            }
        }
    }
}
