package world.hachimi.app.ui.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_captcha_confirm
import hachimiworld.composeapp.generated.resources.auth_captcha_help_label
import hachimiworld.composeapp.generated.resources.auth_captcha_help_message
import hachimiworld.composeapp.generated.resources.auth_captcha_message
import hachimiworld.composeapp.generated.resources.auth_captcha_title
import hachimiworld.composeapp.generated.resources.common_info
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.theme.PreviewTheme

@Composable
fun CaptchaDialog(
    processing: Boolean,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(stringResource(Res.string.auth_captcha_title))
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                var showHelp by remember { mutableStateOf(false) }

                Text(stringResource(Res.string.auth_captcha_message))

                Row(
                    modifier = Modifier.padding(top = 8.dp).clickable(
                        onClick = { showHelp = true },
                        interactionSource = null,
                        indication = null
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = stringResource(Res.string.common_info))
                    Text(
                        modifier = Modifier.padding(start = 4.dp),
                        style = TextStyle(fontSize = 12.sp),
                        text = stringResource(Res.string.auth_captcha_help_label)
                    )
                }

                AnimatedVisibility(showHelp) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = stringResource(Res.string.auth_captcha_help_message),
                        style = TextStyle(fontSize = 12.sp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !processing) {
                Text(stringResource(Res.string.auth_captcha_confirm))
            }
        }
    )
}


@Preview(widthDp = 1280)
@Composable
private fun Preview() {
    PreviewTheme(background = false) {
        CaptchaDialog(
            processing = false,
            onConfirm = {}
        )
    }
}