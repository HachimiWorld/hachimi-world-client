package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.multiplatformStatusBars
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.util.PlatformBackHandler

@Composable
fun ScreenScaffold(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    showBack: Boolean = false,
    showMenu: Boolean = false,
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    if (showBack && onBack != null) {
        PlatformBackHandler(enabled = true, onBack = onBack)
    }

    Column(modifier.fillMaxSize()) {
        ScreenToolbar(
            title = title,
            subtitle = subtitle,
            showBack = showBack,
            showMenu = showMenu,
            onBack = onBack,
            onMenuClick = onMenuClick,
            actions = actions,
        )
        // Toolbar applied multiplatform top insets; body must not re-apply them.
        Column(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .consumeWindowInsets(WindowInsets.multiplatformStatusBars)
        ) {
            content()
        }
    }
}

@Preview(widthDp = 360, name = "Compact · Secondary")
@Composable
private fun PreviewScreenScaffoldCompact() {
    PreviewTheme(background = true) {
        CompositionLocalProvider(
            LocalWindowSize provides DpSize(WindowSize.COMPACT - 1.dp, 800.dp),
        ) {
            ScreenScaffold(
                title = { Text("最近发布") },
                showBack = true,
                onBack = {},
                showMenu = false,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Page content")
                }
            }
        }
    }
}

@Preview(widthDp = 1280, name = "Expanded · Secondary")
@Composable
private fun PreviewScreenScaffoldExpanded() {
    PreviewTheme(background = true) {
        CompositionLocalProvider(
            LocalWindowSize provides DpSize(1280.dp, 800.dp),
        ) {
            ScreenScaffold(title = { Text("设置") }) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Page content")
                }
            }
        }
    }
}
