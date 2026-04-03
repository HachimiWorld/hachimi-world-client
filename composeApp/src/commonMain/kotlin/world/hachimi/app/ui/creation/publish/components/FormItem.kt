package world.hachimi.app.ui.creation.publish.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FormItem(
    header: @Composable () -> Unit,
    subtitle: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier) {
        CompositionLocalProvider(
            value = LocalTextStyle provides MaterialTheme.typography.labelLarge,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                header()
            }
        }
        subtitle?.let {
            CompositionLocalProvider(
                value = LocalTextStyle provides MaterialTheme.typography.bodySmall,
            ) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    it()
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        content()
    }
}
