package world.hachimi.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

/** A dialog over the whole window, the system bars' place included. @since 261113 */
expect fun fullScreenDialogProperties(): DialogProperties

/** Hides the system bars while it's in the composition, on platforms that have them to hide. @since 261113 */
@Composable
expect fun HideSystemBars()
