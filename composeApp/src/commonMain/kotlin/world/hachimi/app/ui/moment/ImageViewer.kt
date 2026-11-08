package world.hachimi.app.ui.moment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_close
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.MomentModule.MomentImage
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text

/**
 * A moment's images over everything, one at a time, swiped or stepped through. Shows the
 * thumbnail while the full image loads.
 *
 * @since 261106
 */
@Composable
fun ImageViewer(images: List<MomentImage>, initialIndex: Int, onDismiss: () -> Unit) {
    if (images.isEmpty()) return
    val pager = rememberPagerState(initialPage = initialIndex.coerceIn(images.indices)) { images.size }
    val scope = rememberCoroutineScope()
    val context = LocalPlatformContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.92f))) {
            HorizontalPager(pager, modifier = Modifier.fillMaxSize()) { page ->
                val image = images[page]
                Box(
                    Modifier
                        .fillMaxSize()
                        .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
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
            HachimiIconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            ) {
                Icon(Icons.Default.Close, stringResource(Res.string.common_close), tint = Color.White)
            }
            if (images.size > 1) {
                Text(
                    "${pager.currentPage + 1} / ${images.size}",
                    modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
                    color = Color.White,
                    fontSize = 14.sp,
                )
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
