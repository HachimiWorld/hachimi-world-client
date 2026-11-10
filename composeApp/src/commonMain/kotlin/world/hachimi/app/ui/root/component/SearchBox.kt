package world.hachimi.app.ui.root.component

import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_clear
import hachimiworld.composeapp.generated.resources.common_search_cd
import hachimiworld.composeapp.generated.resources.search_placeholder
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import world.hachimi.app.ui.design.HachimiTheme
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LocalContentColor
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.singleLined

@Composable
fun SearchBox(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    /** The field's fill; see [TextField]. @since 261110 */
    containerColor: Color = HachimiTheme.colorScheme.background,
    shape: Shape = RoundedCornerShape(16.dp),
    /** Shows a clear button at the end, while [clearVisible]. @since 261111 */
    onClear: (() -> Unit)? = null,
    /** @since 261111 */
    clearVisible: Boolean = searchText.isNotEmpty(),
) {
    val focusManager = LocalFocusManager.current
    TextField(
        modifier = modifier,
        value = searchText,
        onValueChange = { onSearchTextChange(it.singleLined()) },
        shape = shape,
        containerColor = containerColor,
        textStyle = TextStyle(
            color = LocalContentColor.current,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        contentPadding = PaddingValues(start = 8.dp, end = if (onClear != null && clearVisible) 6.dp else 12.dp, top = 0.dp, bottom = 0.dp),
        prefix = {
            HachimiIconButton(
                modifier = Modifier.padding(end = 4.dp).size(28.dp),
                touchMode = true,
                onClick = {
                    focusManager.clearFocus()
                    if (searchText.isNotBlank()) {
                        onSearch()
                    }
                },
//                enabled = searchText.isNotBlank()
            ) {
                Icon(Icons.Default.Search, stringResource(Res.string.common_search_cd))
            }
        },
        postfix = if (onClear != null && clearVisible) {
            {
                HachimiIconButton(
                    modifier = Modifier.padding(start = 4.dp).size(28.dp),
                    touchMode = true,
                    onClick = onClear,
                ) {
                    Icon(Icons.Default.Cancel, stringResource(Res.string.common_clear), Modifier.size(18.dp))
                }
            }
        } else null,
        placeholder = { Text(stringResource(Res.string.search_placeholder)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            focusManager.clearFocus()
            if (searchText.isNotBlank()) {
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