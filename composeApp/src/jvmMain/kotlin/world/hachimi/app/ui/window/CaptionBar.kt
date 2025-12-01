package world.hachimi.app.ui.window

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.LocalDarkMode
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.theme.onSurfaceDark
import world.hachimi.app.ui.theme.onSurfaceLight

@Composable
fun CaptionBar(
    modifier: Modifier,
    maximized: Boolean,
    onMinClick: () -> Unit,
    onMinBounds: (Rect) -> Unit,
    onMaximizeClick: () -> Unit,
    onMaxBounds: (Rect) -> Unit,
    onCloseClick: () -> Unit,
    onCloseBounds: (Rect) -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides if (LocalDarkMode.current) onSurfaceDark else onSurfaceLight) {
        Row(modifier.height(32.dp), horizontalArrangement = Arrangement.End) {
            CaptionButton(
                modifier = Modifier.onGloballyPositioned { onMinBounds(it.boundsInWindow()) },
                onClick = onMinClick
            ) {
                Icon(Icons.Default.Minimize, contentDescription = "Minimize")
            }
            CaptionButton(
                modifier = Modifier.onGloballyPositioned { onMaxBounds(it.boundsInWindow()) },
                onClick = onMaximizeClick
            ) {
                if (maximized) Icon(Icons.Default.CloseFullscreen, contentDescription = "Restore")
                else Icon(Icons.Default.Maximize, contentDescription = "Maximize")
            }
            CaptionButton(
                modifier = Modifier.onGloballyPositioned { onCloseBounds(it.boundsInWindow()) },
                onClick = onCloseClick
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}

@Composable
private fun CaptionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Box(modifier.size(32.dp).clickable(onClick = onClick), Alignment.Center) {
        icon()
    }
}