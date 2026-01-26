package world.hachimi.app.ui.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties()
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        Card(
            modifier = modifier.defaultMinSize(minWidth = 280.dp).widthIn(min = 280.dp, max = 560.dp),
            color = HachimiTheme.colorScheme.surface.compositeOver(HachimiTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Column(Modifier.weight(1f, false)) {
                    icon?.let {
                        Box(Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)) {
                            it()
                        }
                    }

                    Column {
                        title?.let {
                            Row(
                                Modifier.padding(bottom = 16.dp).align(if (icon != null) Alignment.CenterHorizontally else Alignment.Start),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CompositionLocalProvider(LocalTextStyle provides titleStyle) {
                                    it.invoke()
                                }
                            }
                        }
                        CompositionLocalProvider(LocalTextStyle provides textStyle) {
                            text?.let {
                                it()
                            }
                        }
                    }
                }


                Row(
                    modifier = Modifier.align(Alignment.End).padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}


private val titleStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 32.sp
)
private val textStyle = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

@Composable
@Preview(widthDp = 1280)
private fun Preview() {
    PreviewTheme(background = false) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = {}) {
                    Text("CONFIRM")
                }
            },
            dismissButton = {
                TextButton(onClick = {}) {
                    Text("DISMISS")
                }
            },
            icon = {
                Icon(Icons.Default.Info, null)
            },
            title = {
                Text("Lorem ipsum")
            },
            text = {
                Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent est leo, pellentesque nec elementum ut, lobortis quis metus. Sed convallis et dui at bibendum. Morbi nisi quam, placerat eget ligula in, elementum pretium sem. Sed non massa nec libero placerat imperdiet.")
            }
        )
    }
}