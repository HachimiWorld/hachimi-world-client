package world.hachimi.app.ui.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.theme.PreviewTheme

val buttonLabelTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = HachimiTheme.colorScheme.surface,
    contentColor: Color = HachimiTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            Modifier.clickable(onClick = onClick, enabled = enabled).padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                LocalTextStyle provides buttonLabelTextStyle
            ) {
                content()
            }
        }
    }
}

@Composable
fun AccentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = contentPadding,
        shape = shape,
        color = HachimiTheme.colorScheme.primary,
        contentColor = HachimiTheme.colorScheme.onSurfaceReverse,
        content = content
    )
}

@Composable
fun SubtleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = contentPadding,
        shape = shape,
        color = HachimiTheme.colorScheme.primaryContainer,
        contentColor = HachimiTheme.colorScheme.primary,
        content = content
    )
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    shape: Shape = RoundedCornerShape(12.dp),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = contentPadding,
        shape = shape,
        color = Color.Transparent,
        contentColor = HachimiTheme.colorScheme.primary,
        content = content
    )
}

@Preview
@Composable
private fun PreviewButton() {
    PreviewTheme(background = true) {
        Button(onClick = {}) {
            Text("Button")
        }
    }
}

@Preview
@Composable
private fun PreviewAccentButton() {
    PreviewTheme(background = true) {
        Button(onClick = {}) {
            Text("Button")
        }
    }
}

@Preview
@Composable
private fun PreviewSubtleButton() {
    PreviewTheme(background = true) {
        SubtleButton(onClick = {}) {
            Text("Button")
        }
    }
}

@Preview
@Composable
private fun PreviewTextButton() {
    PreviewTheme(background = true) {
        TextButton(onClick = {}) {
            Text("Button")
        }
    }
}