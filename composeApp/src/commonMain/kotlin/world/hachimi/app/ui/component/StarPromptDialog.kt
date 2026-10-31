package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.star_prompt_later
import hachimiworld.composeapp.generated.resources.star_prompt_message
import hachimiworld.composeapp.generated.resources.star_prompt_never
import hachimiworld.composeapp.generated.resources.star_prompt_star
import hachimiworld.composeapp.generated.resources.star_prompt_title
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.model.StarPrompt
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun StarPromptDialog(prompt: StarPrompt) {
    if (!prompt.visible) return
    StarPromptDialogContent(
        onStar = prompt::star,
        onLater = prompt::later,
        onNever = prompt::never,
    )
}

@Composable
private fun StarPromptDialogContent(
    onStar: () -> Unit,
    onLater: () -> Unit,
    onNever: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onLater,
        icon = {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = HachimiTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(Res.string.star_prompt_title)) },
        text = { Text(stringResource(Res.string.star_prompt_message)) },
        confirmButton = {
            TextButton(onClick = onStar) {
                Text(stringResource(Res.string.star_prompt_star))
            }
        },
        dismissButton = {
            TextButton(onClick = onNever) {
                Text(stringResource(Res.string.star_prompt_never))
            }
            TextButton(onClick = onLater) {
                Text(stringResource(Res.string.star_prompt_later))
            }
        },
    )
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        StarPromptDialogContent(onStar = {}, onLater = {}, onNever = {})
    }
}
