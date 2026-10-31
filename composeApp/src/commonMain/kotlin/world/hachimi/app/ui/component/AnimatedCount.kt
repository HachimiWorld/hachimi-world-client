package world.hachimi.app.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A number that rolls to its next value: up as it grows, down as it shrinks.
 *
 * @since 261031
 */
@Composable
fun AnimatedCount(
    count: Long,
    modifier: Modifier = Modifier,
    content: @Composable (Long) -> Unit,
) {
    AnimatedContent(
        targetState = count,
        modifier = modifier,
        transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            (slideInVertically(tween(RollMs)) { it * direction } + fadeIn(tween(RollMs)))
                .togetherWith(slideOutVertically(tween(RollMs)) { -it * direction } + fadeOut(tween(RollMs)))
                .using(SizeTransform(clip = false))
        },
    ) { content(it) }
}

private const val RollMs = 200
