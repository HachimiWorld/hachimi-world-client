package world.hachimi.app.ui.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun SearchUserItem(
    name: String,
    avatarUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CardDefaults.shape,
        color = CardDefaults.cardColors().containerColor,
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Surface(Modifier.size(120.dp), shape = CircleShape) {
                AsyncImage(avatarUrl, "Avatar")
            }
            Text(name, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        SearchUserItem(name = "测试神人", avatarUrl = null, onClick = {})
    }
}