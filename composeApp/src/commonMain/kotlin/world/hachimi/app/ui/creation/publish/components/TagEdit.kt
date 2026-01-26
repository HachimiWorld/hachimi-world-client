package world.hachimi.app.ui.creation.publish.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import world.hachimi.app.model.PublishViewModel
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.LinearProgressIndicator
import world.hachimi.app.ui.design.components.LocalTextStyle
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.util.singleLined

@Composable
fun TagEdit(
    vm: PublishViewModel
) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            vm.tags.fastForEachIndexed { index, item ->
                AssistChip(
                    label = { Text(item.name) },
                    trailingIcon = {
//                        IconButton(onClick = { onRemoveClick(index) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove")
//                        }
                    },
                    onClick = {
                        vm.removeTag(index)
                    }
                )
            }
        }

        val interactionSource = remember { MutableInteractionSource() }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = vm.tagInput,
            onValueChange = { vm.updateTagInput(it.singleLined()) },
            interactionSource = interactionSource,
            singleLine = true,
            keyboardActions = KeyboardActions(onDone = { vm.addTag() }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            trailingIcon = {
                if (vm.tagCreating) {
                    CircularProgressIndicator(Modifier.size(20.dp))
                }
            },
            enabled = !vm.tagCreating
        )

        val focused by interactionSource.collectIsFocusedAsState()
        if (focused) Popup(
            onDismissRequest = {},
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                clippingEnabled = true
            ),
            offset = IntOffset(0, with(LocalDensity.current) {
                if (vm.tags.isNotEmpty()) 128.dp.roundToPx() else 64.dp.roundToPx()
            })
        ) {
            Card(
                Modifier.width(240.dp),
                color = HachimiTheme.colorScheme.surface.compositeOver(HachimiTheme.colorScheme.background)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    if (vm.tagSearching) LinearProgressIndicator(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    if (vm.tagCandidates.isEmpty()) Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = "无结果", style = MaterialTheme.typography.bodyMedium
                    ) else LazyColumn(Modifier.height(120.dp)) {
                        items(vm.tagCandidates, key = { item -> item.id }) { item ->
                            Item(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { vm.selectTag(item) },
                            ) {
                                Text(item.name)
                            }
                        }
                        item {
                            Item(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { vm.addTag() },
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("创建")
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun Item(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
            content()
        }
    }
}