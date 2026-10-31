package world.hachimi.app.ui.insets

import androidx.compose.runtime.Composable

/**
 * While this is in the composition, the window leaves room for the keyboard to the screen, through
 * `imePadding`, instead of panning. Call it on screens that place content above the keyboard; the
 * rest of the app relies on the panning.
 *
 * @since 261031
 */
@Composable
expect fun AdjustResizeForIme()
