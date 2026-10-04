package world.hachimi.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.LocalTextStyle
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.multiplatformSafeDrawing
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.ui.util.WindowSize

val LocalOpenNavigationDrawer = compositionLocalOf<(() -> Unit)?> { null }

// M3 Small Top App Bar tokens
// https://m3.material.io/components/app-bars/specs
private val TopAppBarContainerHeight = 64.dp
private val TopAppBarHorizontalPadding = 4.dp
/** Title start inset when there is no leading icon (16dp edge − 4dp bar padding). */
private val TopAppBarTitleInset = 12.dp
private val TopAppBarIconButtonSize = 48.dp

/**
 * Screen-level top app bar aligned with Material 3 **small** top app bar metrics:
 * 64dp container height, [titleLarge] title, 48dp icon buttons / 24dp icons.
 */
@Composable
fun ScreenToolbar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    showBack: Boolean = false,
    showMenu: Boolean = false,
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val menuHandler = onMenuClick ?: LocalOpenNavigationDrawer.current
    val hasLeading = (showBack && onBack != null) || (showMenu && menuHandler != null)

    val titleStyle = MaterialTheme.typography.titleLarge.copy(
        color = LocalContentColor.current,
    )
    val subtitleStyle = MaterialTheme.typography.bodyMedium.copy(
        color = HachimiTheme.colorScheme.onSurfaceVariant,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(HachimiTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.multiplatformSafeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
                .defaultMinSize(minHeight = TopAppBarContainerHeight)
                .height(TopAppBarContainerHeight)
                .padding(horizontal = TopAppBarHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            when {
                showBack && onBack != null -> {
                    ToolbarIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(Modifier.width(8.dp))
                }

                showMenu && menuHandler != null -> {
                    ToolbarIconButton(onClick = menuHandler) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (hasLeading) 0.dp else TopAppBarTitleInset),
                verticalArrangement = Arrangement.Center,
            ) {
                CompositionLocalProvider(LocalTextStyle provides titleStyle) {
                    title()
                }
                subtitle?.let { content ->
                    CompositionLocalProvider(LocalTextStyle provides subtitleStyle) {
                        content()
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(HachimiTheme.colorScheme.outline)
        )
    }
}

@Composable
private fun ToolbarIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    HachimiIconButton(
        modifier = Modifier.defaultMinSize(
            minWidth = TopAppBarIconButtonSize,
            minHeight = TopAppBarIconButtonSize,
        ),
        onClick = onClick,
        touchMode = true,
        content = content,
    )
}

@Preview(widthDp = 360, name = "Compact · Back")
@Composable
private fun PreviewScreenToolbarCompactBack() {
    PreviewTheme(background = true) {
        CompositionLocalProvider(LocalWindowSize provides DpSize(WindowSize.COMPACT - 1.dp, 800.dp)) {
            ScreenToolbar(
                title = { Text("最近发布", maxLines = 1) },
                showBack = true,
                onBack = {},
            )
        }
    }
}

@Preview(widthDp = 1280, name = "Expanded · Back + Subtitle")
@Composable
private fun PreviewScreenToolbarExpandedBack() {
    PreviewTheme(background = true) {
        CompositionLocalProvider(LocalWindowSize provides DpSize(1280.dp, 800.dp)) {
            ScreenToolbar(
                title = { Text("作品详情", maxLines = 1) },
                subtitle = { Text("12 首歌曲", maxLines = 1) },
                showBack = true,
                onBack = {},
            )
        }
    }
}

@Preview(widthDp = 1280, name = "Expanded · Title only")
@Composable
private fun PreviewScreenToolbarTitleOnly() {
    PreviewTheme(background = true) {
        CompositionLocalProvider(LocalWindowSize provides DpSize(1280.dp, 800.dp)) {
            ScreenToolbar(title = { Text("设置", maxLines = 1) })
        }
    }
}
