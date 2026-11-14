package world.hachimi.app.ui.moment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_close
import hachimiworld.composeapp.generated.resources.image_save
import hachimiworld.composeapp.generated.resources.image_save_failed
import hachimiworld.composeapp.generated.resources.image_saved
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.api.ApiClient
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.downloadImage
import world.hachimi.app.api.module.MomentModule.MomentImage
import world.hachimi.app.logging.Logger
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.multiplatformSafeDrawingPadding
import world.hachimi.app.ui.util.HideSystemBars
import world.hachimi.app.ui.util.SaveImageResult
import world.hachimi.app.ui.util.fullScreenDialogProperties
import world.hachimi.app.ui.util.saveImage
import kotlin.math.pow

/**
 * A moment's images over everything, the system bars included, one at a time, swiped or stepped
 * through. Pinch, double-tap or the mouse wheel zooms in, and a zoomed image is dragged around.
 * Shows the thumbnail while the full image loads.
 *
 * @since 261106
 */
@Composable
fun ImageViewer(images: List<MomentImage>, initialIndex: Int, onDismiss: () -> Unit) {
    if (images.isEmpty()) return
    val pager = rememberPagerState(initialPage = initialIndex.coerceIn(images.indices)) { images.size }
    val scope = rememberCoroutineScope()
    val context = LocalPlatformContext.current
    val api = koinInject<ApiClient>()
    val zooms = remember(images) { List(images.size) { ZoomState() } }
    var saving by remember { mutableStateOf(false) }
    // The nonce shows the same notice again after a second save
    var notice by remember { mutableStateOf<Pair<StringResource, Int>?>(null) }
    LaunchedEffect(notice) {
        if (notice != null) {
            delay(2000)
            notice = null
        }
    }
    // An image swiped away comes back unzoomed
    LaunchedEffect(pager.settledPage) {
        zooms.forEachIndexed { index, zoom -> if (index != pager.settledPage) zoom.reset() }
    }

    Dialog(onDismissRequest = onDismiss, properties = fullScreenDialogProperties()) {
        HideSystemBars()
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(
                pager,
                modifier = Modifier.fillMaxSize(),
                // A zoomed image takes the drags for itself
                userScrollEnabled = !zooms[pager.currentPage].zoomed,
            ) { page ->
                val image = images[page]
                ZoomableBox(zooms[page], onTap = onDismiss) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .httpHeaders(CoilHeaders)
                            .data(image.thumbUrl)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .httpHeaders(CoilHeaders)
                            .data(image.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            Box(Modifier.fillMaxSize().multiplatformSafeDrawingPadding()) {
                Row(
                    Modifier.align(Alignment.TopEnd).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    HachimiIconButton(
                        onClick = {
                            val url = images[pager.currentPage].url
                            saving = true
                            scope.launch {
                                val result = try {
                                    saveImage(api.downloadImage(url), fileNameOf(url))
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Throwable) {
                                    Logger.e("image_viewer", "Failed to save an image", e)
                                    SaveImageResult.FAILED
                                }
                                saving = false
                                when (result) {
                                    SaveImageResult.SAVED -> notice = Res.string.image_saved to (notice?.second ?: 0) + 1
                                    SaveImageResult.FAILED -> notice = Res.string.image_save_failed to (notice?.second ?: 0) + 1
                                    SaveImageResult.DOWNLOADING, SaveImageResult.CANCELLED -> {}
                                }
                            }
                        },
                        enabled = !saving,
                    ) {
                        Icon(
                            Icons.Default.Download,
                            stringResource(Res.string.image_save),
                            Modifier.alpha(if (saving) 0.4f else 1f),
                            tint = Color.White,
                        )
                    }
                    HachimiIconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, stringResource(Res.string.common_close), tint = Color.White)
                    }
                }
                Column(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(notice != null, enter = fadeIn(), exit = fadeOut()) {
                        val shown = remember { mutableStateOf(notice?.first) }
                        notice?.first?.let { shown.value = it }
                        shown.value?.let {
                            Text(
                                stringResource(it),
                                modifier = Modifier
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                color = Color.White,
                                fontSize = 14.sp,
                            )
                        }
                    }
                    if (images.size > 1) {
                        Text("${pager.currentPage + 1} / ${images.size}", color = Color.White, fontSize = 14.sp)
                    }
                }
                if (images.size > 1) {
                    // For a mouse, which can't swipe
                    if (pager.currentPage > 0) {
                        StepButton(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            Modifier.align(Alignment.CenterStart).padding(12.dp),
                        ) { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }
                    }
                    if (pager.currentPage < images.size - 1) {
                        StepButton(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            Modifier.align(Alignment.CenterEnd).padding(12.dp),
                        ) { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }
                    }
                }
            }
        }
    }
}

/** The last part of the URL's path, or a made-up name when it has none. */
private fun fileNameOf(url: String): String {
    val name = url.substringBefore('?').substringBefore('#').substringAfterLast('/')
    return when {
        name.isEmpty() -> "hachimi-image.jpg"
        '.' !in name -> "$name.jpg"
        else -> name
    }
}

private const val MaxScale = 5f
private const val DoubleTapScale = 2.5f

/** How far one image is zoomed in and moved, from the middle of its box. */
@Stable
private class ZoomState {
    var scale by mutableFloatStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set
    var size = IntSize.Zero

    val zoomed get() = scale > 1.01f

    /** Zooms by [zoom] about [centroid], keeping the point under it in place, and moves by [pan]. */
    fun transform(centroid: Offset, zoom: Float, pan: Offset) {
        val newScale = (scale * zoom).coerceIn(1f, MaxScale)
        val fromCenter = centroid - Offset(size.width / 2f, size.height / 2f)
        val z = newScale / scale
        set(newScale, fromCenter - (fromCenter - offset) * z + pan)
    }

    /** In to [DoubleTapScale] about [at], or back out. */
    suspend fun toggle(at: Offset) {
        val fromScale = scale
        val fromOffset = offset
        val toScale = if (zoomed) 1f else DoubleTapScale
        val fromCenter = at - Offset(size.width / 2f, size.height / 2f)
        val toOffset = if (toScale == 1f) Offset.Zero else clamp(fromCenter - fromCenter * toScale, toScale)
        animate(0f, 1f, animationSpec = tween(250)) { t, _ ->
            scale = fromScale + (toScale - fromScale) * t
            offset = fromOffset + (toOffset - fromOffset) * t
        }
    }

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    private fun set(newScale: Float, newOffset: Offset) {
        scale = newScale
        offset = if (newScale <= 1f) Offset.Zero else clamp(newOffset, newScale)
    }

    // The edges of the zoomed box can't come in past the edges of the screen
    private fun clamp(offset: Offset, scale: Float): Offset {
        val maxX = size.width * (scale - 1) / 2
        val maxY = size.height * (scale - 1) / 2
        return Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
    }
}

@Composable
private fun ZoomableBox(state: ZoomState, onTap: () -> Unit, content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { state.size = it }
            .pointerInput(state) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val fingers = event.changes.count { it.pressed }
                        // One finger on an unzoomed image is left to the pager
                        if (fingers > 1 || state.zoomed) {
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            if (zoom != 1f || pan != Offset.Zero) {
                                state.transform(event.calculateCentroid(useCurrent = true), zoom, pan)
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(state) {
                detectTapGestures(
                    onDoubleTap = { scope.launch { state.toggle(it) } },
                    onTap = { onTap() },
                )
            }
            .pointerInput(state) {
                // The mouse wheel, about the pointer
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type != PointerEventType.Scroll) continue
                        val change = event.changes.firstOrNull() ?: continue
                        val delta = change.scrollDelta.y
                        if (delta != 0f) {
                            state.transform(change.position, 0.85f.pow(delta), Offset.Zero)
                            change.consume()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                scaleX = state.scale
                scaleY = state.scale
                translationX = state.offset.x
                translationY = state.offset.y
            },
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, Modifier.size(28.dp), tint = Color.White)
    }
}
