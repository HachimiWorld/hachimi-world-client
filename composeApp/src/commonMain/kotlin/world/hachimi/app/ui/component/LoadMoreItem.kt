package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_no_more
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.Text

@Composable
fun LoadMoreItem(
    modifier: Modifier = Modifier,
    hasMore: Boolean,
    isLoading: Boolean = false,
) {
    Box(
        modifier.fillMaxWidth().height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (!hasMore) {
            Text(
                text = stringResource(Res.string.common_no_more),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}