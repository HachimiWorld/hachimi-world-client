package world.hachimi.app.ui.root.component

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_login
import hachimiworld.composeapp.generated.resources.auth_register
import hachimiworld.composeapp.generated.resources.common_back_cd
import hachimiworld.composeapp.generated.resources.common_menu_cd
import hachimiworld.composeapp.generated.resources.message_entry_cd
import hachimiworld.composeapp.generated.resources.message_entry_unread_cd
import hachimiworld.composeapp.generated.resources.user_space_user_avatar_cd
import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Velocity
import hachimiworld.composeapp.generated.resources.common_search_cd
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.util.PlatformBackHandler
import kotlin.math.roundToInt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.model.MessageCenterStore
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.TestTags
import world.hachimi.app.ui.component.BrandLogo
import world.hachimi.app.ui.component.Logo
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.CardShadow
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.SubtleButton
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.multiplatformSafeDrawing
import world.hachimi.app.ui.notification.components.NotificationBell
import world.hachimi.app.ui.notification.components.formatBadgeCount

@Composable
fun ExpandedTopAppBar(
    global: GlobalStore,
    modifier: Modifier = Modifier
) {
    val navigator = LocalNavigator.current

    Surface(modifier.dropShadow(RectangleShape, CardShadow)) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.multiplatformSafeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (getPlatform().name == "JVM") IconButton(onClick = {
                navigator.back()
            }, enabled = navigator.canGoBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back_cd))
            }
            Logo()

            Row(Modifier.weight(1f).wrapContentWidth()) {
                var searchText by remember { mutableStateOf("") }
                SearchBox(
                    searchText, { searchText = it }, modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                    onSearch = {
                        navigator.push(Route.Root.Search(searchText))
                    }
                )
            }

            if (global.isLoggedIn) {
                val userInfo = global.userInfo!!
                NameAvatar(
                    name = userInfo.name,
                    avatarUrl = userInfo.avatarUrl,
                    onClick = { navigator.push(Route.Root.UserSpace) }
                )
            } else {
                AccentButton(onClick = {
                    navigator.push(Route.Auth())
                }) {
                    Text(stringResource(Res.string.auth_login))
                }
                SubtleButton(onClick = {
                    navigator.push(Route.Auth(false))
                }) {
                    Text(stringResource(Res.string.auth_register))
                }
            }
        }
    }
}

@Composable
private fun NameAvatar(
    name: String,
    avatarUrl: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clip(CircleShape)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(LocalContentColor.current.copy(0.12f))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .httpHeaders(CoilHeaders)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(Res.string.user_space_user_avatar_cd),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}
