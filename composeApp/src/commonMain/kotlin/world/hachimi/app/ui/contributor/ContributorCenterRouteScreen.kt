package world.hachimi.app.ui.contributor

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.contributor_create_post
import hachimiworld.composeapp.generated.resources.contributor_not_contributor
import hachimiworld.composeapp.generated.resources.contributor_review_list
import hachimiworld.composeapp.generated.resources.nav_contributor_center
import org.jetbrains.compose.resources.stringResource
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
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.LoadingPage
import world.hachimi.app.ui.component.NeedLoginScreen
import world.hachimi.app.ui.component.ReloadPage
import world.hachimi.app.ui.creation.publish.PublishScreen
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.multiplatformStatusBarsPadding
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.fillMaxWidthIn

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
    val navigator = LocalNavigator.current

    if (!global.isLoggedIn) {
        NeedLoginScreen()
        return
    }

    if (!vm.isContributor) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(stringResource(Res.string.contributor_not_contributor))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidthIn()
            .multiplatformStatusBarsPadding()
            .padding(AdaptiveScreenMargin)
    ) {
        Text(
            text = stringResource(Res.string.nav_contributor_center),
            modifier = Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        Button(
            onClick = { navigator.push(Route.Root.ContributorCenter.ReviewList) },
        ) {
            Text(stringResource(Res.string.contributor_review_list))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navigator.push(Route.Root.ContributorCenter.CreatePost) },
        ) {
            Text(stringResource(Res.string.contributor_create_post))
        }
    }
}