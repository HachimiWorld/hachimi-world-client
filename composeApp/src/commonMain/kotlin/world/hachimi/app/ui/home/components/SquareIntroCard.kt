package world.hachimi.app.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.square_intro_desc
import hachimiworld.composeapp.generated.resources.square_intro_dismiss_cd
import hachimiworld.composeapp.generated.resources.square_intro_go
import hachimiworld.composeapp.generated.resources.square_intro_title
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.model.SquareIntro
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme

/**
 * Points new and old users to the square, on top of the home page. Tapping it opens the square.
 *
 * @since 261106
 */
@Composable
fun SquareIntroCard(
    intro: SquareIntro,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = intro.visible,
        modifier = modifier,
        exit = fadeOut() + shrinkVertically(),
    ) {
        SquareIntroCardContent(onOpen = onOpen, onDismiss = intro::markSeen)
    }
}

@Composable
private fun SquareIntroCardContent(
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier.fillMaxWidth().clip(shape).clickable(onClick = onOpen),
        shape = shape,
        color = HachimiTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Forum,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = HachimiTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.square_intro_title),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(Res.string.square_intro_desc),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = HachimiTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.square_intro_go),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = HachimiTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = HachimiTheme.colorScheme.primary,
                    )
                }
            }
            HachimiIconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.Top)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(Res.string.square_intro_dismiss_cd),
                    modifier = Modifier.size(16.dp),
                    tint = HachimiTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(widthDp = 390)
@Composable
private fun PreviewSquareIntroCard() {
    PreviewTheme(background = true) {
        SquareIntroCardContent(onOpen = {}, onDismiss = {}, modifier = Modifier.padding(16.dp))
    }
}
