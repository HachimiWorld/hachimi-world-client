package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import world.hachimi.app.ui.theme.PreviewTheme

/** The old name of [ErrorPage]: the whole area retries, there is no button any more. */
@Composable
fun ReloadPage(
    onReloadClick: () -> Unit,
    message: String? = null,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    ErrorPage(onRetry = onReloadClick, modifier = modifier, message = message)
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = true) {
        ReloadPage(onReloadClick = {})
    }
}