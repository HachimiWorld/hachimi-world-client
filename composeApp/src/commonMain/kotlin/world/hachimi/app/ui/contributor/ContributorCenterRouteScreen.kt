package world.hachimi.app.ui.contributor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.ContributorEntryViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.NeedLoginScreen
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.creation.publish.PublishScreen
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.util.AdaptiveScreenMargin
import world.hachimi.app.util.fillMaxWidthIn

@Composable
fun ContributorCenterScreen(child: Route.Root.ContributorCenter) {
    Box(Modifier.fillMaxSize()) {
        when (child) {
            Route.Root.ContributorCenter.Entry -> ContributorEntryScreen()
            Route.Root.ContributorCenter.ReviewList -> ReviewListScreen()
            is Route.Root.ContributorCenter.ReviewDetail -> ReviewDetailScreen(child.reviewId, source = ReviewScreenSource.CONTRIBUTOR)
            is Route.Root.ContributorCenter.ReviewModify -> PublishScreen(songId = null, reviewId = child.reviewId)
            is Route.Root.ContributorCenter.ReviewHistory -> ReviewHistoryScreen(child.reviewId)
            Route.Root.ContributorCenter.CreatePost -> CreatePostScreen()
            is Route.Root.ContributorCenter.EditPost -> TODO()
            Route.Root.ContributorCenter.PostCenter -> TODO()
        }
    }
}

@Composable
fun ContributorEntryScreen(
    global: GlobalStore = koinInject(),
    vm: ContributorEntryViewModel = koinViewModel()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }

    AnimatedContent(
        vm.initStat,
        modifier = Modifier.fillMaxSize()
    ) { status ->
        when (status) {
            InitializeStatus.INIT -> LoadingPage()
            InitializeStatus.FAILED -> {
                // If user not logged in, show the login prompt
                if (!global.isLoggedIn) {
                    NeedLoginScreen()
                } else {
                    ReloadPage(onReloadClick = { vm.retry() })
                }
            }

            InitializeStatus.LOADED -> Content(global, vm)
        }
    }
}

@Composable
private fun Content(
    global: GlobalStore,
    vm: ContributorEntryViewModel
) {
    if (!global.isLoggedIn) {
        NeedLoginScreen()
        return
    }

    if (!vm.isContributor) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("你还不是贡献者，为社区作出贡献，解锁更多功能吧")
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().fillMaxWidthIn().padding(AdaptiveScreenMargin)
    ) {
        Text(
            text = "贡献者中心",
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = { global.nav.push(Route.Root.ContributorCenter.ReviewList) },
        ) {
            Text("审核列表")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { global.nav.push(Route.Root.ContributorCenter.CreatePost) },
        ) {
            Text("创建帖子")
        }
    }
}