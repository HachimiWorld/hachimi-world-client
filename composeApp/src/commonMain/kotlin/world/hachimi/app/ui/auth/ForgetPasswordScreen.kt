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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.ForgetPasswordViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.auth.components.CaptchaDialog
import world.hachimi.app.ui.auth.components.FormCard
import world.hachimi.app.ui.auth.components.FormContent
import world.hachimi.app.ui.auth.components.PasswordToggleButton
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.singleLined
import world.hachimi.app.util.validatePasswordPattern

@Composable
fun ForgetPasswordScreen(vm: ForgetPasswordViewModel = koinViewModel()) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }
    val global = koinInject<GlobalStore>()
    Box(Modifier.fillMaxSize().padding(top = currentSafeAreaInsets().top)) {
        HachimiIconButton(
            modifier = Modifier.padding(24.dp).align(Alignment.TopStart),
            onClick = { global.nav.back() },
            touchMode = true
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        FormCard(
            Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = 512.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            FormContent(
                title = { Text("重置密码") },
                subtitle = { Text("忘记密码是成为神人的第一步") },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.email,
                        onValueChange = { vm.email = it.singleLined() },
                        leadingIcon = { Icon(Icons.Outlined.Mail, null) },
                        placeholder = { Text("邮箱") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        supportingText = {
                            Text("")
                        }
                    )

                    var showPassword by remember { mutableStateOf(false) }
                    var passwordHelp by remember { mutableStateOf("") }
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.password,
                        onValueChange = {
                            vm.password = it.singleLined()
                            passwordHelp =
                                if (validatePasswordPattern(vm.password)) ""
                                else "密码长度至少为 6 位"
                        },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                        placeholder = { Text("新密码") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        trailingIcon = {
                            PasswordToggleButton(
                                showPassword,
                                onValueChange = { showPassword = it }
                            )
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        supportingText = { Text(passwordHelp) }
                    )

                    var showRepeatPassword by remember { mutableStateOf(false) }
                    var repeatPasswordHelp by remember { mutableStateOf("") }

                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.passwordRepeat,
                        onValueChange = {
                            vm.passwordRepeat = it.singleLined()
                            repeatPasswordHelp =
                                if (vm.password != vm.passwordRepeat) "两次输入不一致"
                                else ""
                        },
                        leadingIcon = { Icon(Icons.Outlined.SettingsBackupRestore, null) },
                        placeholder = { Text("确认密码") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        trailingIcon = {
                            PasswordToggleButton(
                                showRepeatPassword,
                                onValueChange = { showRepeatPassword = it }
                            )
                        },
                        visualTransformation = if (showRepeatPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        supportingText = { Text(repeatPasswordHelp) }
                    )

                    val enabled by remember {
                        derivedStateOf {
                            vm.email.isNotBlank() && vm.password.isNotBlank()
                                    && vm.password == vm.passwordRepeat
                                    && vm.verifyCode.isNotBlank()
                        }
                    }

                    Column {
                        val sendCodeEnabled by remember {
                            derivedStateOf { vm.codeRemainSecs < 0 }
                        }
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = vm.verifyCode,
                            onValueChange = { vm.verifyCode = it.singleLined() },
                            leadingIcon = { Icon(Icons.Outlined.Security, null) },
                            placeholder = { Text("验证码") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onDone = {
                                if (enabled && !vm.operating) {
                                    vm.submit()
                                }
                            })
                        )
                        TextButton(
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                            onClick = { vm.sendVerifyCode() },
                            enabled = sendCodeEnabled && !vm.operating && vm.email.isNotBlank()
                        ) {
                            Text(
                                if (sendCodeEnabled) "发送"
                                else "重新发送 (${vm.codeRemainSecs} 秒)"
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        TextButton(
                            modifier = Modifier.size(width = 112.dp, height = 48.dp),
                            onClick = { global.nav.back()
                        }) {
                            Text("返回")
                        }

                        Spacer(Modifier.weight(1f))

                        AccentButton(
                            modifier = Modifier.size(width = 112.dp, height = 48.dp),
                            onClick = { vm.submit() },
                            enabled = enabled && !vm.operating
                        ) {
                            Text("确定")
                        }
                    }
                }

                if (vm.showSuccessDialog) AlertDialog(
                    onDismissRequest = { vm.closeDialog() },
                    confirmButton = {
                        TextButton(onClick = { vm.closeDialog() }) {
                            Text("确定")
                        }
                    },
                    title = { Text("成功") },
                    text = { Text("已成功将您的密码重置") }
                )

                if (vm.showCaptchaDialog) CaptchaDialog(
                    processing = vm.operating,
                    onConfirm = { vm.captchaContinue() }
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview() {
    PreviewTheme(background = true) {
        ForgetPasswordScreen()
    }
}