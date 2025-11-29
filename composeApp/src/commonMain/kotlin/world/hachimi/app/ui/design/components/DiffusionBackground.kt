package world.hachimi.app.ui.design.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun DiffusionBackground(modifier: Modifier = Modifier, painter: Painter) {
    // TODO: Optimize the performance
    BoxWithConstraints(modifier) {
        val maxEdge = maxOf(maxHeight, maxWidth)
        val scale = maxEdge.value / 256.dp.value

        Box(modifier = Modifier
            .graphicsLayer {
                transformOrigin = TransformOrigin(0f, 0f)
                scaleX = scale
                scaleY = scale
            }
            .size(256.dp).clipToBounds().blur(48.dp)) {
            RotateCombinedPicture(Modifier.fillMaxSize().scale(1.42f), painter)
        }
    }
}

@Composable
fun RotateCombinedPicture(modifier: Modifier = Modifier, painter: Painter){
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )
    CombinedRotatePicture(modifier.rotate(rotation), painter)
}

@Composable
fun CombinedRotatePicture(modifier: Modifier = Modifier, painter: Painter) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 15f,
        targetValue = 375f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )
    val rotation3 by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = 390f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )
    val rotation4 by infiniteTransition.animateFloat(
        initialValue = 45f,
        targetValue = 405f,
        animationSpec = infiniteRepeatable(
            animation = tween(35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = ""
    )

    Box(modifier = modifier.size(256.dp)) {
        Picture(
            Modifier.align(Alignment.Center).fillMaxSize(),
            painter
        )
        DividedPictureRT(Modifier.align(Alignment.TopStart).rotate(rotation1), painter)
        DividedPictureLT(Modifier.align(Alignment.TopEnd).rotate(rotation3), painter)
        DividedPictureRB(Modifier.align(Alignment.BottomStart).rotate(rotation2), painter)
        DividedPictureLB(Modifier.align(Alignment.BottomEnd).rotate(rotation4), painter)
    }
}

@Composable
private fun DividedPictureRT(modifier: Modifier = Modifier, painter: Painter) {
    Box(modifier = modifier.size(128.dp).clipToBounds()) {
        Picture(
            Modifier.scale(2f).offset(128.dp/4, 128.dp/4),
            painter
        )
    }
}

@Composable
private fun DividedPictureLT(modifier: Modifier = Modifier, painter: Painter) {
    Box(modifier = modifier.size(128.dp).clipToBounds()) {
        Picture(
            Modifier.scale(2f).offset((-128).dp /4, 128.dp/4),
            painter
        )
    }
}

@Composable
private fun DividedPictureRB(modifier: Modifier = Modifier, painter: Painter) {
    Box(modifier = modifier.size(128.dp).clipToBounds()) {
        Picture(
            Modifier.scale(2f).offset(128.dp/4, (-128).dp /4),
            painter
        )
    }
}

@Composable
private fun DividedPictureLB(modifier: Modifier = Modifier, painter: Painter) {
    Box(modifier = modifier.size(128.dp).clipToBounds()) {
        Picture(
            Modifier.scale(2f).offset((-128).dp /4, (-128).dp /4),
            painter
        )
    }
}

@Composable
private fun Picture(modifier: Modifier = Modifier, painter: Painter) {
    Surface(
        modifier.size(256.dp)
    ) {
        Image(
            painter = painter,
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(2f) }),
        )
    }
}