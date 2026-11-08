package world.hachimi.app.ui.moment

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
import hachimiworld.composeapp.generated.resources.comment_header
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
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.component.ScreenScaffold
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
    val state = when {
        vm.notFound -> DetailState.NOT_FOUND
        vm.status == InitializeStatus.FAILED -> DetailState.FAILED
        moment == null -> DetailState.LOADING
        else -> DetailState.LOADED
    }
    AnimatedContent(state, modifier = Modifier.fillMaxSize()) { st ->
        when (st) {
            DetailState.LOADING -> Placeholder(navigator::back) { LoadingPage() }
            DetailState.FAILED -> Placeholder(navigator::back) { ReloadPage(onReloadClick = vm::retry) }
            DetailState.NOT_FOUND -> Placeholder(navigator::back) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.moment_not_found), color = HachimiTheme.colorScheme.onSurfaceVariant)
                }
            }
            DetailState.LOADED -> {
                val item = vm.moment ?: return@AnimatedContent
                CommentsPage(
                    target = CommentTarget(CommentModule.TARGET_MOMENT, item.id, item.author.uid),
                    title = { total ->
                        if (total > 0) stringResource(Res.string.comment_header, total.toString())
                        else stringResource(Res.string.moment_page_title)
                    },
                    onBack = navigator::back,
                    header = {
                        MomentCard(vm.moment ?: item, actions, full = true)
                        Box(Modifier.fillMaxWidth().height(1.dp).background(HachimiTheme.colorScheme.outline))
                    },
                    startAtComments = atComments,
                )
            }
        }
    }
}

private enum class DetailState { LOADING, FAILED, NOT_FOUND, LOADED }

@Composable
private fun Placeholder(onBack: () -> Unit, content: @Composable () -> Unit) {
    ScreenScaffold(
        title = { Text(stringResource(Res.string.moment_page_title), maxLines = 1) },
        showBack = true,
        onBack = onBack,
    ) {
        content()
    }
}
