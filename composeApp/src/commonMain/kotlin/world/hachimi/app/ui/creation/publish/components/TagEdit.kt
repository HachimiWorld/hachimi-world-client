package world.hachimi.app.ui.creation.publish.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import world.hachimi.app.model.PublishViewModel
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
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = vm.tagInput,
            onValueChange = { vm.updateTagInput(it.singleLined()) },
            interactionSource = interactionSource,
            singleLine = true,
            keyboardActions = KeyboardActions(onDone = { vm.addTag() }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            trailingIcon = {
                if (vm.tagCreating) { CircularProgressIndicator(Modifier.size(24.dp)) }
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
            ElevatedCard(Modifier.width(240.dp)) {
                if (vm.tagSearching) LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                if (vm.tagCandidates.isEmpty()) Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "无结果", style = MaterialTheme.typography.bodyMedium
                ) else LazyColumn(Modifier.height(120.dp)) {
                    items(vm.tagCandidates, key = { item -> item.id }) { item ->
                        Box(
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                                .clickable { vm.selectTag(item) }
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}