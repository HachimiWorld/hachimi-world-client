package world.hachimi.app.ui.root.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_login
import hachimiworld.composeapp.generated.resources.auth_register
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.Logo
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.CardShadow
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.SubtleButton
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.insets.currentSafeAreaInsets

@Composable
fun CompactTopAppBar(
    global: GlobalStore,
    onExpandNavClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier.dropShadow(RectangleShape, CardShadow)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                .padding(top = currentSafeAreaInsets().top)
                .consumeWindowInsets(WindowInsets.statusBars),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onExpandNavClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
            var searchText by remember { mutableStateOf("") }
            SearchBox(
                modifier = Modifier.weight(1f),
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                onSearch = {
                    global.nav.push(Route.Root.Search(searchText))
                }
            )
            if (global.isLoggedIn) {
                val userInfo = global.userInfo!!
                AvatarOnly(
                    avatarUrl = userInfo.avatarUrl,
                    onClick = { global.nav.push(Route.Root.UserSpace) }
                )
            } else {
                AvatarOnly(
                    avatarUrl = null,
                    onClick = { global.nav.push(Route.Auth()) }
                )
            }
        }
    }
}

@Composable
fun ExpandedTopAppBar(
    global: GlobalStore,
    modifier: Modifier = Modifier
) {
    Surface(modifier.dropShadow(RectangleShape, CardShadow)) {
        Row(
            modifier = Modifier
                .padding(top = currentSafeAreaInsets().top)
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp)
                .consumeWindowInsets(WindowInsets.statusBars),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (getPlatform().name == "JVM") IconButton(onClick = {
                global.nav.back()
            }, enabled = global.nav.backStack.size > 1) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Logo()

            Row(Modifier.weight(1f).wrapContentWidth()) {
                var searchText by remember { mutableStateOf("") }
                SearchBox(
                    searchText, { searchText = it }, modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                    onSearch = {
                        global.nav.push(Route.Root.Search(searchText))
                    }
                )
            }

            if (global.isLoggedIn) {
                val userInfo = global.userInfo!!
                NameAvatar(
                    name = userInfo.name,
                    avatarUrl = userInfo.avatarUrl,
                    onClick = { global.nav.push(Route.Root.UserSpace) }
                )
            } else {
                AccentButton(onClick = {
                    global.nav.push(Route.Auth())
                }) {
                    Text(stringResource(Res.string.auth_login))
                }
                SubtleButton(onClick = {
                    global.nav.push(Route.Auth(false))
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
                contentDescription = "User Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun AvatarOnly(avatarUrl: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(LocalContentColor.current.copy(0.12f))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .httpHeaders(CoilHeaders)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            contentDescription = "User Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
