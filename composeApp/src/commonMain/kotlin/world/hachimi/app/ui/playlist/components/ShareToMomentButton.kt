package world.hachimi.app.ui.playlist.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.moment_share
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon

/** Writes a moment sharing the playlist. @since 261114 */
@Composable
fun ShareToMomentButton(onClick: () -> Unit) {
    Button(onClick = onClick, contentPadding = PaddingValues(8.dp)) {
        Icon(Icons.Default.Share, contentDescription = stringResource(Res.string.moment_share))
    }
}
