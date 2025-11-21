package world.hachimi.app.ui.root.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import world.hachimi.app.getPlatform
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.Logo
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.singleLined

@Composable
fun TopAppBar(
    global: GlobalStore,
    onExpandNavClick: () -> Unit
) {
    BoxWithConstraints {
        if (maxWidth < 600.dp) {
            CompactTopAppBar(global, onExpandNavClick)
        } else {
            ExpandedTopAppBar(global)
        }
    }
}

@Composable
fun CompactTopAppBar(
    global: GlobalStore,
    onExpandNavClick: () -> Unit
) {
    Surface(Modifier.fillMaxWidth(), shadowElevation = 2.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).padding(top = currentSafeAreaInsets().top),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedTopAppBar(global: GlobalStore) {
    Surface(Modifier.fillMaxWidth(), shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .padding(top = currentSafeAreaInsets().top)
                .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
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
private fun SearchBox(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    BasicTextField(
        modifier = modifier.defaultMinSize(300.dp),
        value = searchText,
        onValueChange = { onSearchTextChange(it.singleLined()) },
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        decorationBox = { innerTextField ->
            Card(shape = CircleShape) {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                        innerTextField()
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        onClick = {
                            focusManager.clearFocus()
                            onSearch()
                        },
                        enabled = searchText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Search, "Search")
                    }
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            if (searchText.isNotBlank()) {
                focusManager.clearFocus()
                onSearch()
            }
        })
    )
}

@Composable
private fun NameAvatar(
    name: String,
    avatarUrl: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Column {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
            /*Text(
                text = "Lv.4",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )*/
        }
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(0.12f))
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
            .background(MaterialTheme.colorScheme.onSurface.copy(0.12f))
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

@Preview
@Composable
private fun PreviewSearchBox() {
    PreviewTheme(background = true) {
        SearchBox("Search", {}, onSearch = {})
    }
}