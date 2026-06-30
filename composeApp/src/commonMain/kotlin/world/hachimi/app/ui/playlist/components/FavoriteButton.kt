package world.hachimi.app.ui.playlist.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.playlist_favorite
import hachimiworld.composeapp.generated.resources.playlist_unfavorite
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Icon

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onFavoriteClick: (Boolean) -> Unit,
    operating: Boolean
) {
    Button(
        onClick = { onFavoriteClick(!isFavorite) },
        enabled = !operating,
        contentPadding = PaddingValues(8.dp)
    ) {
        if (isFavorite) Icon(
            Icons.Default.Favorite,
            contentDescription = stringResource(Res.string.playlist_unfavorite)
        )
        else Icon(
            Icons.Default.FavoriteBorder,
            contentDescription = stringResource(Res.string.playlist_favorite)
        )
    }
}
