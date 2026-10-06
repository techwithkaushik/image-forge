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
import org.techwithkaushik.imageforge.common.ProcessingFamily
import org.techwithkaushik.imageforge.common.ToolDefinition
import org.techwithkaushik.imageforge.imageprocessor.AndroidImageRepository
import org.techwithkaushik.imageforge.imageprocessor.ImageInput
import org.techwithkaushik.imageforge.imageprocessor.ImageOperation
import org.techwithkaushik.imageforge.imageprocessor.ImageProcessingRequest
import org.techwithkaushik.imageforge.imageprocessor.ProcessImageUseCase
import org.techwithkaushik.imageforge.storage.AndroidStorageGateway
import org.techwithkaushik.imageforge.storage.StorageDestination

internal data class EditorUiState(
    val tool: ToolDefinition? = null,
    val sourceUri: Uri? = null,
    val width: String = "",
    val height: String = "",
    val targetKb: String = "100",
    val rotateDegrees: Int = 90,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
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

    fun initialize(uri: Uri?, tool: ToolDefinition?) {
        if (_state.value.sourceUri == uri && _state.value.tool == tool) return
        val preset = resizePreset(tool?.title)
        val targetKb = targetKbPreset(tool?.title)
        _state.value = EditorUiState(
            tool = tool,
            sourceUri = uri,
            width = preset?.first?.toString() ?: "",
            height = preset?.second?.toString() ?: "",
            targetKb = targetKb?.toString() ?: "100",
        )
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

    private fun resizePreset(title: String?): Pair<Int, Int>? {
        val value = title?.lowercase() ?: return null
        return when {
            "a4" in value -> 2480 to 3508
            "instagram grid" in value -> 1080 to 1080
            "instagram" in value -> 1080 to 1080
            "whatsapp dp" in value -> 500 to 500
            "youtube banner" in value -> 2560 to 1440
            "600x600" in value || "2 x 2 inch" in value -> 600 to 600
            "3 x 4 inch" in value -> 900 to 1200
            "4 x 6 inch" in value -> 1200 to 1800
            "35mm x 45mm" in value || "3.5cm x 4.5cm" in value || "35mm" in value -> 413 to 531
            "signature 50mm x 20mm" in value -> 591 to 236
            "6cm x 2cm" in value -> 709 to 236
            else -> null
        }
    }

    private fun targetKbPreset(title: String?): Long? =
        Regex("""(\\d+)\\s*(kb|mb)""").find(title?.lowercase().orEmpty())?.let { match ->
            val value = match.groupValues[1].toLong()
            if (match.groupValues[2] == "mb") value * 1024L else value
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

    fun updateTargetKb(value: String) {
        if (value.all(Char::isDigit) && value.length <= 7) _state.value = _state.value.copy(targetKb = value)
    }
    fun rotate() = process(ImageOperation.Rotate(_state.value.rotateDegrees), "Rotate complete.")
    fun setRotateDegrees(value: Int) { _state.value = _state.value.copy(rotateDegrees = value) }
    fun setFlipHorizontal(value: Boolean) { _state.value = _state.value.copy(flipHorizontal = value) }
    fun setFlipVertical(value: Boolean) { _state.value = _state.value.copy(flipVertical = value) }
    fun flip() {
        val current = _state.value
        if (!current.flipHorizontal && !current.flipVertical) return message("Select horizontal or vertical flip.")
        process(ImageOperation.Flip(current.flipHorizontal, current.flipVertical), "Flip complete.")
    }
    fun convert(mimeType: String) = process(ImageOperation.Convert(mimeType), "Conversion complete.")
    fun compress() {
        val kb = _state.value.targetKb.toLongOrNull() ?: return message("Enter a valid target size.")
        if (kb <= 0) return message("Target size must be greater than zero.")
        process(ImageOperation.Compress(kb * 1024L), "Compression complete.")
    }
    fun cropCenter() {
        val current = _state.value
        val width = current.width.toIntOrNull()
        val height = current.height.toIntOrNull()
        if (width == null || height == null || width <= 0 || height <= 0) return message("Enter valid crop width and height.")
        val left = ((current.originalWidth - width) / 2).coerceAtLeast(0)
        val top = ((current.originalHeight - height) / 2).coerceAtLeast(0)
        if (left + width > current.originalWidth || top + height > current.originalHeight) return message("Crop size exceeds the original image.")
        process(ImageOperation.Crop(left, top, width, height), "Crop complete.")
    }
    fun passport() = process(ImageOperation.PassportPhoto(), "Passport photo created.")
    fun signature() = process(ImageOperation.ExtractSignature(), "Signature extracted.")
    fun adjustColors() = process(ImageOperation.ColorAdjust(_state.value.brightness, _state.value.contrast, _state.value.saturation), "Image adjustment complete.")
    fun setBrightness(value: Float) { _state.value = _state.value.copy(brightness = value) }
    fun setContrast(value: Float) { _state.value = _state.value.copy(contrast = value) }
    fun setSaturation(value: Float) { _state.value = _state.value.copy(saturation = value) }
    fun resize() {
        val current = _state.value
        val width = current.width.toIntOrNull()
        val height = current.height.toIntOrNull()
        if (width == null || height == null || width <= 0 || height <= 0) return message("Enter valid width and height.")
        process(ImageOperation.Resize(width, height), "Resize complete.")
    }
    private fun process(operation: ImageOperation, successMessage: String) {
        val current = _state.value
        val uri = current.sourceUri ?: return message("Import an image first.")
        viewModelScope.launch {
            _state.value = current.copy(busy = true, message = null, outputUri = null)
            val mime = resolver.getType(uri) ?: "image/jpeg"
            when (val result = processor(ImageProcessingRequest(ImageInput(uri, mime), operation)) { progress ->
                _state.value = _state.value.copy(progress = progress)
            }) {
                is ForgeResult.Success -> _state.value = _state.value.copy(busy = false, outputUri = result.value.uri, message = successMessage)
                is ForgeResult.Failure -> _state.value = _state.value.copy(busy = false, message = "Processing failed: ${result.error}")
            }
        }
    }
    fun save() {
        val output = _state.value.outputUri ?: return message("Process an image before saving.")
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
    tool: ToolDefinition? = null,
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
                title = { Text(tool?.title ?: "Editor") },
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
            } else {
                when (state.tool?.family) {
                    ProcessingFamily.RESIZE -> ResizeFamilyContent(state, viewModel)
                    ProcessingFamily.COMPRESSION -> CompressionFamilyContent(state, viewModel)
                    ProcessingFamily.CONVERSION -> ConversionFamilyContent(state, viewModel)
                    ProcessingFamily.TRANSFORM -> TransformFamilyContent(state, viewModel)
                    ProcessingFamily.CROP -> CropFamilyContent(state, viewModel)
                    ProcessingFamily.PASSPORT_ID -> PassportFamilyContent(state, viewModel)
                    ProcessingFamily.SIGNATURE -> SignatureFamilyContent(viewModel)
                    ProcessingFamily.EFFECTS -> EffectsFamilyContent(state, viewModel)
                    else -> {
                        Text(
                            "This processing family is ready for connection.",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text("Family: ${state.tool?.family?.name ?: "UNKNOWN"}")
                        Text("Tool: ${state.tool?.title ?: "Image Editor"}")
                    }
                }
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

@Composable
private fun CompressionFamilyContent(state: EditorUiState, viewModel: EditorViewModel) {
    Text(state.tool?.title ?: "Compression", style = MaterialTheme.typography.headlineSmall)
    OutlinedTextField(value = state.targetKb, onValueChange = viewModel::updateTargetKb, label = { Text("Target size (KB)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
    Button(onClick = viewModel::compress, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Compress Image") }
}

@Composable
private fun ConversionFamilyContent(state: EditorUiState, viewModel: EditorViewModel) {
    Text(state.tool?.title ?: "Conversion", style = MaterialTheme.typography.headlineSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { viewModel.convert("image/jpeg") }, modifier = Modifier.weight(1f)) { Text("JPG") }
        Button(onClick = { viewModel.convert("image/png") }, modifier = Modifier.weight(1f)) { Text("PNG") }
        Button(onClick = { viewModel.convert("image/webp") }, modifier = Modifier.weight(1f)) { Text("WEBP") }
    }
}

@Composable
private fun TransformFamilyContent(state: EditorUiState, viewModel: EditorViewModel) {
    Text(state.tool?.title ?: "Transform", style = MaterialTheme.typography.headlineSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { viewModel.setRotateDegrees(90); viewModel.rotate() }, modifier = Modifier.weight(1f)) { Text("Rotate 90°") }
        Button(onClick = { viewModel.setRotateDegrees(180); viewModel.rotate() }, modifier = Modifier.weight(1f)) { Text("Rotate 180°") }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { viewModel.setFlipHorizontal(true); viewModel.setFlipVertical(false); viewModel.flip() }, modifier = Modifier.weight(1f)) { Text("Flip H") }
        Button(onClick = { viewModel.setFlipHorizontal(false); viewModel.setFlipVertical(true); viewModel.flip() }, modifier = Modifier.weight(1f)) { Text("Flip V") }
    }
}

@Composable
private fun CropFamilyContent(state: EditorUiState, viewModel: EditorViewModel) {
    Text(state.tool?.title ?: "Crop", style = MaterialTheme.typography.headlineSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = state.width, onValueChange = viewModel::updateWidth, label = { Text("Crop width") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
        OutlinedTextField(value = state.height, onValueChange = viewModel::updateHeight, label = { Text("Crop height") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
    }
    Button(onClick = viewModel::cropCenter, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Crop Center") }
}

@Composable
private fun PassportFamilyContent(state: EditorUiState, viewModel: EditorViewModel) {
    Text(state.tool?.title ?: "Passport / ID Photo", style = MaterialTheme.typography.headlineSmall)
    Text("Default preset: 35 × 45 mm at 300 DPI (413 × 531 px)")
    Button(onClick = viewModel::passport, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Create Passport Photo") }
}

@Composable
private fun SignatureFamilyContent(viewModel: EditorViewModel) {
    Text("Signature", style = MaterialTheme.typography.headlineSmall)
    Text("Detect dark ink, crop to the ink bounds and clean border noise.")
    Button(onClick = viewModel::signature, enabled = true, modifier = Modifier.fillMaxWidth()) { Text("Extract Signature") }
}

@Composable
private fun EffectsFamilyContent(state: EditorUiState, viewModel: EditorViewModel) {
    Text(state.tool?.title ?: "Effects", style = MaterialTheme.typography.headlineSmall)
    OutlinedTextField(value = state.brightness.toString(), onValueChange = { it.toFloatOrNull()?.let(viewModel::setBrightness) }, label = { Text("Brightness (-1 to 1)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = state.contrast.toString(), onValueChange = { it.toFloatOrNull()?.let(viewModel::setContrast) }, label = { Text("Contrast (0 to 2)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = state.saturation.toString(), onValueChange = { it.toFloatOrNull()?.let(viewModel::setSaturation) }, label = { Text("Saturation (0 to 2)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
    Button(onClick = viewModel::adjustColors, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Apply Effect") }
}
@Composable
private fun ResizeFamilyContent(
    state: EditorUiState,
    viewModel: EditorViewModel,
) {
    Text(
        state.tool?.title ?: "Resize Image",
        style = MaterialTheme.typography.headlineSmall,
    )
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
        if (state.busy) {
            CircularProgressIndicator(
                modifier = Modifier.padding(end = 8.dp),
                strokeWidth = 2.dp,
            )
        }
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
}
