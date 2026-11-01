package world.hachimi.app.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.star_prompt_later
import hachimiworld.composeapp.generated.resources.star_prompt_message
import hachimiworld.composeapp.generated.resources.star_prompt_never
import hachimiworld.composeapp.generated.resources.star_prompt_star
import hachimiworld.composeapp.generated.resources.star_prompt_title
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.model.StarPrompt
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.ElevatedCard
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

/**
 * Three actions don't fit side by side on a phone, so the main action gets a full-width button
 * and the two ways out are stacked under it. Text sizes follow the Material 3 dialog spec
 * (headlineSmall title, bodyMedium supporting text).
 */
@Composable
private fun StarPromptDialogContent(
    onStar: () -> Unit,
    onLater: () -> Unit,
    onNever: () -> Unit,
) {
    Dialog(onDismissRequest = onLater) {
        ElevatedCard(
            modifier = Modifier.widthIn(min = 280.dp, max = 400.dp),
            color = HachimiTheme.colorScheme.surface.compositeOver(HachimiTheme.colorScheme.background),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = HachimiTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(Res.string.star_prompt_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(Res.string.star_prompt_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                AccentButton(onClick = onStar, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.star_prompt_star))
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onLater, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.star_prompt_later))
                }
                TextButton(onClick = onNever, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.star_prompt_never))
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        StarPromptDialogContent(onStar = {}, onLater = {}, onNever = {})
    }
}
