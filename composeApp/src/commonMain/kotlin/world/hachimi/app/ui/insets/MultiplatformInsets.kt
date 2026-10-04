package world.hachimi.app.ui.insets

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Desktop-only caption band (Windows custom title bar / mac traffic lights).
 * Mobile implementations return empty insets.
 *
 * Combined types below participate in the normal [windowInsetsPadding] /
 * [consumeWindowInsets] tree — prefer them over hand-rolled Dp paddings.
 */
expect val WindowInsets.Companion.multiplatformCaptionBar: WindowInsets

/** Optional override for caption height (e.g. Windows [LocalMultiplatformCaptionBarHeight]). */
val LocalMultiplatformCaptionBarHeight = compositionLocalOf { 0.dp }

val WindowInsets.Companion.multiplatformStatusBars: WindowInsets
    @Composable
    get() = WindowInsets.multiplatformCaptionBar.add(WindowInsets.statusBars)

val WindowInsets.Companion.multiplatformNavigationBars: WindowInsets
    @Composable
    get() = WindowInsets.multiplatformCaptionBar.add(WindowInsets.navigationBars)

val WindowInsets.Companion.multiplatformSystemBars: WindowInsets
    @Composable
    get() = WindowInsets.multiplatformCaptionBar.add(WindowInsets.systemBars)

val WindowInsets.Companion.multiplatformSafeDrawing: WindowInsets
    @Composable
    get() = WindowInsets.multiplatformCaptionBar.add(WindowInsets.safeDrawing)
