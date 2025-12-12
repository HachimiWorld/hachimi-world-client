package world.hachimi.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.LocalTextStyle

private val chipTextStyle = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 16.sp
)

@Composable
fun Chip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    content: @Composable RowScope.() -> Unit
) {
    CompositionLocalProvider(LocalTextStyle provides chipTextStyle, LocalContentColor provides HachimiTheme.colorScheme.onSurfaceReverse) {
        Row(
            modifier = modifier
                .height(24.dp).defaultMinSize(minWidth = 24.dp)
                .background(HachimiTheme.colorScheme.onSurface, RoundedCornerShape(50))
                .dropShadow(RoundedCornerShape(50), Shadow(radius = 16.dp, offset = DpOffset(x = 0.dp, y = 2.dp), color = Color(0f, 0f, 0f, 0.15f)))
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onClick)
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun AmbientChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    content: @Composable RowScope.() -> Unit
) {
    CompositionLocalProvider(LocalTextStyle provides chipTextStyle) {
        Row(
            modifier = modifier
                .height(24.dp).defaultMinSize(minWidth = 24.dp)
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onClick)
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * Text is selectable
 */
@Composable
fun HintChip(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    content: @Composable RowScope.() -> Unit
) {
    CompositionLocalProvider(LocalTextStyle provides chipTextStyle) {
        SelectionContainer {
            Row(
                modifier = modifier
                    .height(24.dp).defaultMinSize(minWidth = 24.dp)
                    .background(HachimiTheme.colorScheme.onSurfaceReverse.copy(0.15f), RoundedCornerShape(50))
                    .padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}