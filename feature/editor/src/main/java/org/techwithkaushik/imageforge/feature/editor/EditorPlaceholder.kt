package org.techwithkaushik.imageforge.feature.editor

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.techwithkaushik.imageforge.common.ForgeResult
import org.techwithkaushik.imageforge.imageprocessor.AndroidImageRepository
import org.techwithkaushik.imageforge.imageprocessor.ImageInput
import org.techwithkaushik.imageforge.imageprocessor.ImageOperation
import org.techwithkaushik.imageforge.imageprocessor.ImageProcessingRequest
import org.techwithkaushik.imageforge.imageprocessor.ProcessImageUseCase
import org.techwithkaushik.imageforge.storage.AndroidStorageGateway
import org.techwithkaushik.imageforge.storage.StorageDestination

internal data class EditorUiState(
    val tool: String? = null,
    val sourceUri: Uri? = null,
    val width: String = "",
    val height: String = "",
    val keepAspectRatio: Boolean = true,
    val originalWidth: Int = 0,
    val originalHeight: Int = 0,
    val outputUri: Uri? = null,
    val progress: org.techwithkaushik.imageforge.common.ProcessingProgress? = null,
    val busy: Boolean = false,
    val message: String? = null,
)

internal class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val processor = ProcessImageUseCase(AndroidImageRepository(application))
    private val storage = AndroidStorageGateway(application)
    private val resolver = application.contentResolver
    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    fun initialize(uri: Uri?, tool: String?) {
        if (_state.value.sourceUri == uri && _state.value.tool == tool) return
        _state.value = EditorUiState(tool = tool, sourceUri = uri)
        if (uri != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val bounds = resolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.Options().also {
                        it.inJustDecodeBounds = true
                        BitmapFactory.decodeStream(input, null, it)
                    }
                }
                if (bounds != null && bounds.outWidth > 0 && bounds.outHeight > 0) {
                    _state.value = _state.value.copy(
                        width = bounds.outWidth.toString(),
                        height = bounds.outHeight.toString(),
                        originalWidth = bounds.outWidth,
                        originalHeight = bounds.outHeight,
                    )
                }
            }
        }
    }

    fun updateWidth(value: String) {
        if (value.all(Char::isDigit) && value.length <= 5) {
            val current = _state.value
            if (current.keepAspectRatio) {
                val width = value.toIntOrNull()
                val ow = current.originalWidth
                val oh = current.originalHeight
                if (width != null && width > 0 && ow > 0 && oh > 0) {
                    val height = (width.toLong() * oh / ow).coerceAtLeast(1L).coerceAtMost(99_999L)
                    _state.value = current.copy(width = value, height = height.toString())
                    return
                }
            }
            _state.value = current.copy(width = value)
        }
    }

    fun updateHeight(value: String) {
        if (value.all(Char::isDigit) && value.length <= 5) {
            val current = _state.value
            if (current.keepAspectRatio) {
                val height = value.toIntOrNull()
                if (height != null && height > 0 && current.originalWidth > 0 && current.originalHeight > 0) {
                    val width = (height.toLong() * current.originalWidth / current.originalHeight)
                        .coerceAtLeast(1L).coerceAtMost(99_999L)
                    _state.value = current.copy(height = value, width = width.toString())
                    return
                }
            }
            _state.value = current.copy(height = value)
        }
    }

    fun setKeepAspectRatio(enabled: Boolean) {
        val current = _state.value
        _state.value = current.copy(keepAspectRatio = enabled)
        if (enabled && current.originalWidth > 0 && current.originalHeight > 0) {
            val width = current.width.toIntOrNull() ?: return
            val height = (width.toLong() * current.originalHeight / current.originalWidth)
                .coerceAtLeast(1L).coerceAtMost(99_999L)
            _state.value = _state.value.copy(height = height.toString())
        }
    }

    fun resize() {
        val current = _state.value
        val uri = current.sourceUri ?: return message("Import an image first.")
        val width = current.width.toIntOrNull()
        val height = current.height.toIntOrNull()
        if (width == null || height == null || width <= 0 || height <= 0) {
            return message("Enter valid width and height.")
        }
        viewModelScope.launch {
            _state.value = current.copy(busy = true, message = null, outputUri = null)
            val mime = resolver.getType(uri) ?: "image/jpeg"
            when (val result = processor(
                ImageProcessingRequest(
                    input = ImageInput(uri = uri, mimeType = mime),
                    operation = ImageOperation.Resize(width, height),
                ),
            ) { progress ->
                _state.value = _state.value.copy(progress = progress)
            }) {
                is ForgeResult.Success -> _state.value = _state.value.copy(
                    busy = false,
                    outputUri = result.value.uri,
                    message = "Resize complete.",
                )
                is ForgeResult.Failure -> _state.value = _state.value.copy(
                    busy = false,
                    message = "Resize failed: ${result.error}",
                )
            }
        }
    }

    fun save() {
        val output = _state.value.outputUri ?: return message("Resize an image before saving.")
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true)
            val result = storage.saveImage(
                source = output,
                destination = StorageDestination.Pictures,
                displayName = "ImageForge-${System.currentTimeMillis()}.jpg",
                mimeType = "image/jpeg",
            )
            _state.value = _state.value.copy(
                busy = false,
                message = when (result) {
                    is ForgeResult.Success -> "Saved to Pictures/ImageForge."
                    is ForgeResult.Failure -> "Save failed: ${result.error}"
                },
            )
        }
    }

    private fun message(value: String) {
        _state.value = _state.value.copy(message = value)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun EditorScreen(
    imageUri: String? = null,
    tool: String? = null,
    onBack: () -> Unit = {},
    onImport: () -> Unit = {},
) {
    val viewModel: EditorViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(imageUri, tool) {
        viewModel.initialize(imageUri?.let(Uri::parse), tool)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tool ?: "Editor") },
                navigationIcon = {
                    androidx.compose.material3.TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (state.sourceUri == null) {
                Text("Select an image to begin.", style = MaterialTheme.typography.titleLarge)
                Button(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                    Text("Import Image")
                }
            } else if (tool?.contains("Resize Image", ignoreCase = true) == true) {
                Text("Resize Image by Pixel", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Original: ${state.originalWidth} × ${state.originalHeight} px",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = state.width,
                        onValueChange = viewModel::updateWidth,
                        label = { Text("Width (px)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = state.height,
                        onValueChange = viewModel::updateHeight,
                        label = { Text("Height (px)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.keepAspectRatio,
                        onCheckedChange = viewModel::setKeepAspectRatio,
                    )
                    Text("Keep aspect ratio")
                }
                Button(
                    onClick = viewModel::resize,
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.busy) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                    Text(if (state.busy) "Resizing…" else "Resize Image")
                }
                if (state.outputUri != null) {
                    Button(
                        onClick = viewModel::save,
                        enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Save to Pictures")
                    }
                }
            } else {
                Text("This tool is being connected one-by-one.", style = MaterialTheme.typography.titleLarge)
                Text("Selected tool: ${tool ?: "Image Editor"}")
            }
            state.progress?.let {
                Text("${it.completedSteps}/${it.totalSteps} — ${it.message ?: "Processing"}")
                if (state.busy) CircularProgressIndicator()
            }
            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
