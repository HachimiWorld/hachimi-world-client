package world.hachimi.app.ui.util

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.RefreshingIndicator
import world.hachimi.app.ui.component.ReloadPage

@Composable
fun InitStatusScaffold(
    initializeStatus: InitializeStatus,
    isLoading: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    initPage: @Composable () -> Unit = { LoadingPage() },
    errorPage: @Composable () -> Unit = { ReloadPage(onReloadClick = onRetryClick) },
    content: @Composable () -> Unit
) {
    Box(modifier) {
        AnimatedContent(
            targetState = initializeStatus,
            transitionSpec = { fadeInFadeOut() }
        ) { initStatus ->
            when (initStatus) {
                InitializeStatus.INIT -> initPage()
                InitializeStatus.LOADED -> content()
                InitializeStatus.FAILED -> errorPage()
            }
        }
        RefreshingIndicator(isRefreshing = isLoading && initializeStatus == InitializeStatus.LOADED)
    }
}