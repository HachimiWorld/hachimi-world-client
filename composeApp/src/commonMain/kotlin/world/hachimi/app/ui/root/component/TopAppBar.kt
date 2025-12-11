package world.hachimi.app.ui.root.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.Logo
import world.hachimi.app.ui.design.components.*
import world.hachimi.app.ui.insets.currentSafeAreaInsets

@Composable
fun CompactTopAppBar(
    global: GlobalStore,
    onExpandNavClick: () -> Unit
) {
    Surface(Modifier.fillMaxWidth().dropShadow(RectangleShape, CardShadow)) {
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
fun ExpandedTopAppBar(global: GlobalStore) {
    Surface(Modifier.zIndex(2f).fillMaxWidth().dropShadow(RectangleShape, CardShadow)) {
        Row(
            modifier = Modifier
                .padding(top = currentSafeAreaInsets().top)
                .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
                .consumeWindowInsets(WindowInsets.statusBars),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (getPlatform().name == "JVM") IconButton(onClick = {
                global.nav.back()
            }, enabled = global.nav.backStack.size > 1) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Logo()

            Row(Modifier.weight(1f).wrapContentWidth()) {
                var searchText by remember { mutableStateOf("") }
                SearchBox(
                    searchText, { searchText = it }, modifier = Modifier.widthIn(max = 400.dp),
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
                Button(onClick = {
                    global.nav.push(Route.Auth())
                }) {
                    Text("登录")
                }
                Button(onClick = {
                    global.nav.push(Route.Auth(false))
                }) {
                    Text("注册")
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
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(text = name, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(LocalContentColor.current.copy(0.12f))
        ) {
            AsyncImage(
                model = avatarUrl,
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
            model = avatarUrl,
            contentDescription = "User Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
