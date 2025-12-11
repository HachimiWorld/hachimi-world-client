package world.hachimi.app.ui.root.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Surface
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
    BasicTextField(
        modifier = modifier.defaultMinSize(300.dp),
        value = searchText,
        onValueChange = { onSearchTextChange(it.singleLined()) },
        cursorBrush = SolidColor(LocalContentColor.current),
        textStyle = searchTextStyle.copy(color = LocalContentColor.current),
        decorationBox = { innerTextField ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = HachimiTheme.colorScheme.background
            ) {
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

private val searchTextStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

@Preview
@Composable
private fun PreviewSearchBox() {
    PreviewTheme(background = true) {
        SearchBox("Search", {}, onSearch = {})
    }
}