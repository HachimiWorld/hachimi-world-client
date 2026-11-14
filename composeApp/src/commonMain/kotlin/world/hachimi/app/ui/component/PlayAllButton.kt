package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.play_all
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Icon

/**
 * Plays everything on the page: the accent play button the mini player wears, for the page's
 * [title][PageTitleAction].
 *
 * @since 261114
 */
@Composable
fun PlayAllButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    AccentButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        contentPadding = PaddingValues.Zero,
        enabled = enabled,
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = stringResource(Res.string.play_all))
    }
}
