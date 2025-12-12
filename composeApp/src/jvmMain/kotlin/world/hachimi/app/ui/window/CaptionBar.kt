package world.hachimi.app.ui.window

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.SystemFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.theme.onSurfaceDark
import world.hachimi.app.ui.theme.onSurfaceLight

@OptIn(ExperimentalTextApi::class)
private val SegoeIconFont = FontFamily(
    SystemFont("Segoe Fluent Icons"),
    SystemFont("Segoe MDL2 Assets"),
)

private const val MinimizeGlyph = '\uE921'
private const val MaximizeGlyph = '\uE922'
private const val RestoreGlyph = '\uE923'
private const val CloseGlyph = '\uE8BB'

@Composable
fun CaptionBar(
    modifier: Modifier,
    darkMode: Boolean,
    maximized: Boolean,
    onMinClick: () -> Unit,
    onMinBounds: (Rect) -> Unit,
    onMaximizeClick: () -> Unit,
    onMaxBounds: (Rect) -> Unit,
    onCloseClick: () -> Unit,
    onCloseBounds: (Rect) -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides if (darkMode) onSurfaceDark else onSurfaceLight) {
        Row(modifier.height(32.dp), horizontalArrangement = Arrangement.End) {
            CaptionButton(
                modifier = Modifier.onGloballyPositioned { onMinBounds(it.boundsInWindow()) },
                onClick = onMinClick,
                glyph = MinimizeGlyph,
                contentDescription = "Minimize"
            )
            CaptionButton(
                modifier = Modifier.onGloballyPositioned { onMaxBounds(it.boundsInWindow()) },
                onClick = onMaximizeClick,
                glyph = if (maximized) RestoreGlyph else MaximizeGlyph,
                contentDescription = if (maximized) "Restore" else "Maximize"
            )
            CaptionButton(
                modifier = Modifier.onGloballyPositioned { onCloseBounds(it.boundsInWindow()) },
                onClick = onCloseClick,
                glyph = CloseGlyph,
                contentDescription = "Close",
                hoverColor = Color(0xFFC42B1C),
                activeColor = Color(0xFFB32B1C)
            )
        }
    }
}

@Composable
private fun CaptionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    glyph: Char,
    activeColor: Color = LocalContentColor.current.copy(0.06f),
    hoverColor: Color = LocalContentColor.current.copy(0.12f),
    contentDescription: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()

    val backgroundColor by animateColorAsState(
        when {
            pressed -> activeColor
            hovered -> hoverColor
            else -> Color.Transparent
        }
    )
    Box(
        modifier.size(32.dp)
            .clickable(
                onClick = onClick,
                onClickLabel = contentDescription,
                indication = null,
                interactionSource = interactionSource
            )
            .background(backgroundColor),
        Alignment.Center,
    ) {
        BasicText(
            text = glyph.toString(),
            style = TextStyle(
                color = LocalContentColor.current,
                fontFamily = SegoeIconFont,
                fontSize = 10.sp
            )
        )
    }
}