package world.hachimi.app.ui.root.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.singleLined

@Composable
fun SearchBox(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    TextField(
        modifier = modifier,
        value = searchText,
        onValueChange = { onSearchTextChange(it.singleLined()) },
        shape = RoundedCornerShape(24.dp),
        textStyle = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        contentPadding = PaddingValues(start = 8.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        prefix = {
            HachimiIconButton(
                modifier = Modifier.padding(end = 8.dp),
                touchMode = true,
                onClick = {
                    focusManager.clearFocus()
                    onSearch()
                },
                enabled = searchText.isNotBlank()
            ) {
                Icon(Icons.Default.Search, "Search")
            }
        },
        placeholder = { Text("搜索...") },
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

@Preview
@Composable
private fun PreviewSearchBox() {
    PreviewTheme(background = true) {
        SearchBox("Search", {}, onSearch = {})
    }
}