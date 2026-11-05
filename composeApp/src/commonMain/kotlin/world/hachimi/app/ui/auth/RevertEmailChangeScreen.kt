package world.hachimi.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.autofill.contentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_confirm_password_placeholder
import hachimiworld.composeapp.generated.resources.auth_new_password_placeholder
import hachimiworld.composeapp.generated.resources.auth_passwords_not_match
import hachimiworld.composeapp.generated.resources.auth_revert_email_password_too_short
import hachimiworld.composeapp.generated.resources.auth_revert_email_subtitle
import hachimiworld.composeapp.generated.resources.auth_revert_email_success_text
import hachimiworld.composeapp.generated.resources.auth_revert_email_success_title
import hachimiworld.composeapp.generated.resources.auth_revert_email_title
import hachimiworld.composeapp.generated.resources.common_ok
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.RevertEmailChangeViewModel
import world.hachimi.app.nav.HandleNavigationRequests
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.auth.components.FormCard
import world.hachimi.app.ui.auth.components.FormContent
import world.hachimi.app.ui.auth.components.PasswordToggleButton
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.insets.multiplatformSafeDrawingPadding
import world.hachimi.app.util.singleLined

/** The server asks for at least 8 characters here. */
private const val MIN_PASSWORD_LENGTH = 8

/**
 * Opened from the link in the email sent to the old address when the account email changes.
 * @since 261105
 */
@Composable
fun RevertEmailChangeScreen(token: String, vm: RevertEmailChangeViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    HandleNavigationRequests(vm.navigationRequests, navigator)

    Box(Modifier.fillMaxSize().multiplatformSafeDrawingPadding()) {
        FormCard(
            Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = 512.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            FormContent(
                title = { Text(stringResource(Res.string.auth_revert_email_title)) },
                subtitle = { Text(stringResource(Res.string.auth_revert_email_subtitle)) },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var showPassword by remember { mutableStateOf(false) }
                    val tooShort = vm.password.isNotEmpty() && vm.password.length < MIN_PASSWORD_LENGTH
                    TextField(
                        modifier = Modifier.fillMaxWidth().contentType(ContentType.NewPassword),
                        value = vm.password,
                        onValueChange = { vm.password = it.singleLined() },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                        placeholder = { Text(stringResource(Res.string.auth_new_password_placeholder)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        trailingIcon = { PasswordToggleButton(showPassword, onValueChange = { showPassword = it }) },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        supportingText = { Text(if (tooShort) stringResource(Res.string.auth_revert_email_password_too_short) else "") }
                    )

                    var showRepeat by remember { mutableStateOf(false) }
                    val notMatch = vm.passwordRepeat.isNotEmpty() && vm.password != vm.passwordRepeat
                    TextField(
                        modifier = Modifier.fillMaxWidth().contentType(ContentType.NewPassword),
                        value = vm.passwordRepeat,
                        onValueChange = { vm.passwordRepeat = it.singleLined() },
                        leadingIcon = { Icon(Icons.Outlined.SettingsBackupRestore, null) },
                        placeholder = { Text(stringResource(Res.string.auth_confirm_password_placeholder)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        trailingIcon = { PasswordToggleButton(showRepeat, onValueChange = { showRepeat = it }) },
                        visualTransformation = if (showRepeat) VisualTransformation.None else PasswordVisualTransformation(),
                        supportingText = { Text(if (notMatch) stringResource(Res.string.auth_passwords_not_match) else "") }
                    )

                    Row(Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(1f))
                        AccentButton(
                            modifier = Modifier.size(width = 112.dp, height = 48.dp),
                            onClick = { vm.submit(token) },
                            enabled = vm.password.length >= MIN_PASSWORD_LENGTH && vm.password == vm.passwordRepeat && !vm.operating
                        ) {
                            Text(stringResource(Res.string.common_ok))
                        }
                    }
                }

                if (vm.showSuccessDialog) AlertDialog(
                    onDismissRequest = { vm.closeDialog() },
                    confirmButton = {
                        TextButton(onClick = { vm.closeDialog() }) { Text(stringResource(Res.string.common_ok)) }
                    },
                    title = { Text(stringResource(Res.string.auth_revert_email_success_title)) },
                    text = { Text(stringResource(Res.string.auth_revert_email_success_text)) }
                )
            }
        }
    }
}
