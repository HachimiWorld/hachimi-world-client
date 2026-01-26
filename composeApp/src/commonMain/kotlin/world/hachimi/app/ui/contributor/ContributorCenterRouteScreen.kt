package world.hachimi.app.ui.contributor

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Text

@Composable
fun ContributorCenterScreen(child: Route.Root.ContributorCenter) {
    Box(Modifier.fillMaxSize()) {
        when (child) {
            Route.Root.ContributorCenter.Entry -> ContributorEntryScreen()
            Route.Root.ContributorCenter.ReviewList -> ReviewListScreen()
            is Route.Root.ContributorCenter.ReviewDetail -> ReviewDetailScreen(child.reviewId)
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

    AnimatedContent(vm.initStat) { status ->
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
            InitializeStatus.LOADED -> {
                if (!global.isLoggedIn) {
                    NeedLoginScreen()
                    return@AnimatedContent
                }

                if (!vm.isContributor) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("你还不是贡献者，为社区作出贡献，解锁更多功能吧")
                    }
                    return@AnimatedContent
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "贡献者中心", modifier = Modifier.padding(bottom = 16.dp))

                    Button(
                        onClick = { global.nav.push(Route.Root.ContributorCenter.ReviewList) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    ) {
                        Text("审核列表")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { global.nav.push(Route.Root.ContributorCenter.CreatePost) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    ) {
                        Text("创建帖子")
                    }
                }
            }
        }
    }
}