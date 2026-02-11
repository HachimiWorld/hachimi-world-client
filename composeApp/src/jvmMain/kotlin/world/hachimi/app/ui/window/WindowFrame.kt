package world.hachimi.app.ui.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinUser
import io.github.composefluent.gallery.jna.windows.ComposeWindowProcedure
import io.github.composefluent.gallery.jna.windows.structure.WinUserConst
import world.hachimi.app.LocalWindow
import world.hachimi.app.ui.insets.LocalSafeAreaInsets
import world.hachimi.app.ui.insets.SafeAreaInsets

val LocalWindowFrameState: ProvidableCompositionLocal<WindowFrameState?> =
    staticCompositionLocalOf { null }

interface WindowFrameState {
    var darkMode: Boolean
}

private class WindowFrameStateImpl(darkMode: Boolean) : WindowFrameState {
    private val _darkMode = mutableStateOf(darkMode)
    override var darkMode: Boolean
        get() = _darkMode.value
        set(value) {
            _darkMode.value = value
        }
}

@Composable
fun WindowFrame(
    state: WindowState,
    initialDarkMode: Boolean,
    backgroundColor: Color,
    onCloseRequest: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val window = LocalWindow.current
    var maxButtonRect by remember { mutableStateOf(Rect.Zero) }
    var minButtonRect by remember { mutableStateOf(Rect.Zero) }
    var closeButtonRect by remember { mutableStateOf(Rect.Zero) }
    var captionBarRect by remember { mutableStateOf(Rect.Zero) }
    val maximized = state.placement == WindowPlacement.Maximized
    var windowInsets by remember { mutableStateOf(WindowInsets()) }
    val procedure = remember(window) {
        ComposeWindowProcedure(
            window,
            hitTest = { x, y ->
                when {
                    maxButtonRect.contains(x, y) -> WinUserConst.HTMAXBUTTON
                    minButtonRect.contains(x, y) -> WinUserConst.HTMINBUTTON
                    closeButtonRect.contains(x, y) -> WinUserConst.HTCLOSE
                    captionBarRect.contains(x, y) -> WinUserConst.HTCAPTION

                    else -> WinUserConst.HTCLIENT
                }
            },
            onWindowInsetUpdate = {
                windowInsets = it
            }
        )
    }
    val state = remember(window, initialDarkMode) { WindowFrameStateImpl(initialDarkMode) }
    val windowInsetsPaddings = windowInsets.asPaddingValues()
    val safeAreaInsets = windowInsetsPaddings.let {
        SafeAreaInsets(
            it.calculateTopPadding() + CaptionBarHeight,
            it.calculateBottomPadding(),
            it.calculateStartPadding(LocalLayoutDirection.current),
            it.calculateEndPadding(LocalLayoutDirection.current)
        )
    }
    CompositionLocalProvider(
        LocalWindowFrameState provides state,
        LocalSafeAreaInsets provides safeAreaInsets
    ) {
        Box(Modifier.fillMaxSize().background(backgroundColor)) {
            content()
            CaptionBar(
                modifier = Modifier.padding(
                    top = windowInsetsPaddings.calculateTopPadding(),
                    start = windowInsetsPaddings.calculateStartPadding(LocalLayoutDirection.current),
                    end = windowInsetsPaddings.calculateEndPadding(LocalLayoutDirection.current)
                )
                    .fillMaxWidth()
                    .onGloballyPositioned { captionBarRect = it.boundsInWindow() },
                darkMode = state.darkMode,
                maximized = maximized,
                onMinClick = {
                    User32.INSTANCE.ShowWindow(
                        procedure.windowHandle,
                        WinUser.SW_MINIMIZE
                    )
                },
                onMinBounds = { minButtonRect = it },
                onMaximizeClick = {
                    if (maximized) User32.INSTANCE.ShowWindow(
                        procedure.windowHandle,
                        WinUser.SW_RESTORE
                    ) else User32.INSTANCE.ShowWindow(
                        procedure.windowHandle,
                        WinUser.SW_MAXIMIZE
                    )
                },
                onMaxBounds = { maxButtonRect = it },
                onCloseClick = onCloseRequest,
                onCloseBounds = { closeButtonRect = it },
            )
        }
    }
}

fun Rect.contains(x: Float, y: Float): Boolean {
    return x >= left && x < right && y >= top && y < bottom
}
