package world.hachimi.app.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.icon_3d_512x
import hachimiworld.composeapp.generated.resources.logo_text
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.vectorResource
import world.hachimi.app.ui.theme.LocalDarkMode
import world.hachimi.app.ui.theme.PreviewTheme

/** Figma sidebar / compact header logo: 124×32 row with 32dp-tall icon + wordmark. */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    showText: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val runningJob = remember { mutableStateOf<Job?>(null) }

    Row(
        modifier = modifier
            .width(124.dp)
            .height(32.dp)
            .clickable(interactionSource = interactionSource, indication = null) {
                val job = runningJob.value
                runningJob.value = scope.launch {
                    job?.cancel()
                    launch {
                        scale.snapTo(1f)
                        scale.animateTo(1.2f, tween(100))
                        scale.animateTo(1f, tween(100))
                    }
                    launch {
                        rotation.snapTo(0f)
                        rotation.animateTo(360f, tween(300))
                    }
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                },
            bitmap = imageResource(Res.drawable.icon_3d_512x),
            contentDescription = "基米天堂 Icon",
            filterQuality = FilterQuality.High,
        )
        if (showText) {
            Image(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .height(20.dp),
                imageVector = vectorResource(Res.drawable.logo_text),
                contentDescription = "基米天堂",
                colorFilter = ColorFilter.tint(
                    if (LocalDarkMode.current) Color(0xFFE8E0D4)
                    else Color(0xFF4F432F)
                ),
            )
        }
    }
}

@Composable
fun Logo(
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val runningJob = remember { mutableStateOf<Job?>(null) }
    
    Row(
        modifier = modifier.clickable(interactionSource = interactionSource, indication = null) {
            val job = runningJob.value
            runningJob.value = scope.launch {
                job?.cancel()
                launch {
                    scale.snapTo(1f)
                    scale.animateTo(1.2f, tween(100))
                    scale.animateTo(1f, tween(100))
                }
                launch {
                    rotation.snapTo(0f)
                    rotation.animateTo(360f, tween(300))
                }
            }
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(56.dp).graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                rotationZ = rotation.value
            },
            bitmap = imageResource(Res.drawable.icon_3d_512x),
            contentDescription = "基米天堂 Icon",
            filterQuality = FilterQuality.High,
        )
        AnimatedVisibility(visible = showText) {
            Image(
                imageVector = vectorResource(Res.drawable.logo_text),
                contentDescription = "基米天堂",
                colorFilter = ColorFilter.tint(
                    if (LocalDarkMode.current) Color(0xFFE8E0D4)
                    else Color(0xFF4F432F)
                )
            )
        }
    }
}

@Preview(name = "BrandLogo")
@Composable
private fun PreviewBrandLogo() {
    PreviewTheme(background = true) {
        BrandLogo()
    }
}

@Preview(name = "Logo")
@Composable
private fun PreviewLogo() {
    PreviewTheme(background = true) {
        Logo()
    }
}