package world.hachimi.app.ui.moment

import androidx.compose.foundation.layout.Column
import world.hachimi.app.ui.util.loadStateTransition
import world.hachimi.app.ui.component.ErrorPage
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.moment_not_found
import hachimiworld.composeapp.generated.resources.moment_page_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.CommentModule
import world.hachimi.app.model.CommentTarget
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.MomentDetailViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.comment.CommentsPage
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.component.TitleStyle
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Text

/**
 * A moment above its comments.
 *
 * @param atComments Opens scrolled to the comments.
 * @since 261106
 */
@Composable
fun MomentDetailScreen(
    momentId: Long,
    atComments: Boolean = false,
    vm: MomentDetailViewModel = koinViewModel(),
) {
    val navigator = LocalNavigator.current
    LaunchedEffect(momentId) { vm.mounted(momentId) }
    val actions = rememberMomentActions(onDeleted = navigator::back)

    val moment = vm.moment
    // The comments don't wait for the moment: both load at once, and only a moment that's gone or
    // didn't load takes the page from them
    val state = when {
        vm.notFound -> DetailState.NOT_FOUND
        vm.status == InitializeStatus.FAILED -> DetailState.FAILED
        else -> DetailState.SHOWN
    }
    AnimatedContent(state, modifier = Modifier.fillMaxSize(), transitionSpec = { loadStateTransition() }) { st ->
        when (st) {
            DetailState.FAILED -> Placeholder(navigator::back) { ErrorPage(onRetry = vm::retry) }
            DetailState.NOT_FOUND -> Placeholder(navigator::back) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.moment_not_found), color = HachimiTheme.colorScheme.onSurfaceVariant)
                }
            }
            DetailState.SHOWN -> CommentsPage(
                target = CommentTarget(CommentModule.TARGET_MOMENT, momentId, moment?.author?.uid),
                title = { stringResource(Res.string.moment_page_title) },
                onBack = navigator::back,
                header = {
                    AnimatedContent(vm.moment, transitionSpec = { loadStateTransition() }, contentKey = { it != null }) { shown ->
                        Column {
                            // Where the moment will open, until it does
                            if (shown == null) MomentSkeleton(index = 0) else MomentCard(shown, actions, full = true)
                            Box(Modifier.fillMaxWidth().height(1.dp).background(HachimiTheme.colorScheme.outline))
                        }
                    }
                },
                startAtComments = atComments,
                // Comments that come back first wait for the moment; a moment that comes first shows at once
                holdComments = moment == null,
            )
        }
    }
}

private enum class DetailState { SHOWN, FAILED, NOT_FOUND }

@Composable
private fun Placeholder(onBack: () -> Unit, content: @Composable () -> Unit) {
    ScreenScaffold(
        titleStyle = TitleStyle.Compact,
        title = { Text(stringResource(Res.string.moment_page_title), maxLines = 1) },
        showBack = true,
        onBack = onBack,
    ) {
        content()
    }
}
