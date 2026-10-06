package org.techwithkaushik.imageforge.feature.editor

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.techwithkaushik.imageforge.common.CropConfiguration
import org.techwithkaushik.imageforge.common.CropMode

internal data class CropSelection(val left: Int, val top: Int, val width: Int, val height: Int)

@Composable
internal fun CropEditorDialog(
    resolver: ContentResolver,
    imageUri: Uri,
    configuration: CropConfiguration,
    originalWidth: Int,
    originalHeight: Int,
    onDismiss: () -> Unit,
    onConfirm: (CropSelection) -> Unit,
) {
    var bitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember(imageUri) { mutableFloatStateOf(1f) }
    var pan by remember(imageUri) { mutableStateOf(Offset.Zero) }
    var ratio by remember(configuration.aspectRatio) {
        mutableStateOf(configuration.aspectRatio ?: (originalWidth.coerceAtLeast(1) to originalHeight.coerceAtLeast(1)))
    }
    LaunchedEffect(imageUri) {
        bitmap = withContext(Dispatchers.IO) {
            resolver.openInputStream(imageUri)?.use { BitmapFactory.decodeStream(it) }
        }
    }
    val image = bitmap
    val ratios = if (configuration.mode == CropMode.FIXED) {
        listOfNotNull(configuration.aspectRatio)
    } else {
        listOf(1 to 1, 4 to 3, 3 to 4, 16 to 9, 9 to 16,
            originalWidth.coerceAtLeast(1) to originalHeight.coerceAtLeast(1)).distinct()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.96f),
        title = { Text("Crop image") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (image == null) {
                    Box(Modifier.fillMaxWidth().height(340.dp), contentAlignment = Alignment.Center) {
                        Text("Loading image…")
                    }
                } else {
                    Box(
                        Modifier.fillMaxWidth().height(340.dp).background(MaterialTheme.colorScheme.scrim)
                            .onSizeChanged { viewport = it }
                            .pointerInput(image) {
                                detectTransformGestures { _, gesturePan, gestureZoom, _ ->
                                    zoom = (zoom * gestureZoom).coerceIn(1f, 5f)
                                    pan += gesturePan
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            bitmap = image.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().graphicsLayer {
                                scaleX = zoom; scaleY = zoom
                                translationX = pan.x; translationY = pan.y
                            },
                        )
                        CropFrame(ratio, Modifier.fillMaxSize())
                    }
                }
                if (configuration.mode != CropMode.FIXED) {
                    Text("Crop ratio", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        ratios.forEach { item ->
                            Button(onClick = { ratio = item }, modifier = Modifier.weight(1f)) {
                                Text("${item.first}:${item.second}")
                            }
                        }
                    }
                } else {
                    Text("Fixed frame: ${configuration.width} × ${configuration.height} ${configuration.unit.name.lowercase()}")
                }
                Text("Pinch to zoom • drag to position", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(enabled = image != null && viewport.width > 0, onClick = {
                val selectedImage = image ?: return@Button
                onConfirm(calculateSelection(viewport, selectedImage, zoom, pan, ratio))
            }) { Text("Crop & Apply") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun CropFrame(ratio: Pair<Int, Int>, modifier: Modifier) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val r = ratio.first.toFloat() / ratio.second.toFloat()
        val frameWidth = if (maxWidth / maxHeight > r) maxHeight * 0.82f * r else maxWidth * 0.82f
        val frameHeight = frameWidth / r
        Box(Modifier.size(frameWidth, frameHeight).border(2.dp, MaterialTheme.colorScheme.primary))
    }
}

private fun calculateSelection(viewport: IntSize, image: Bitmap, zoom: Float, pan: Offset, ratio: Pair<Int, Int>): CropSelection {
    val vw = viewport.width.toFloat()
    val vh = viewport.height.toFloat()
    val scale = minOf(vw / image.width, vh / image.height)
    val r = ratio.first.toFloat() / ratio.second.toFloat()
    val fw = if (vw / vh > r) vh * 0.82f * r else vw * 0.82f
    val fh = fw / r
    val left = (vw - fw) / 2f
    val top = (vh - fh) / 2f
    fun toImage(p: Offset): Offset {
        val relative = p - Offset(vw / 2f, vh / 2f) - pan
        return Offset(image.width / 2f + relative.x / (scale * zoom), image.height / 2f + relative.y / (scale * zoom))
    }
    val a = toImage(Offset(left, top))
    val b = toImage(Offset(left + fw, top + fh))
    val l = a.x.coerceIn(0f, image.width - 1f)
    val t = a.y.coerceIn(0f, image.height - 1f)
    val rgt = b.x.coerceIn(l + 1f, image.width.toFloat())
    val bot = b.y.coerceIn(t + 1f, image.height.toFloat())
    return CropSelection(l.toInt(), t.toInt(), (rgt - l).toInt().coerceAtLeast(1), (bot - t).toInt().coerceAtLeast(1))
}