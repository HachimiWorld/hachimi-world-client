package world.hachimi.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.ForgetPasswordViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.auth.components.CaptchaDialog
import world.hachimi.app.ui.auth.components.FormCard
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.util.singleLined

@Composable
fun ForgetPasswordScreen(vm: ForgetPasswordViewModel = koinViewModel()) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }
    val global = koinInject<GlobalStore>()
    Box(Modifier.fillMaxSize().padding(top = currentSafeAreaInsets().top)) {
        IconButton(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp).align(Alignment.TopStart),
            onClick = { global.nav.back() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        FormCard(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = 512.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            title = { Text("重置密码") },
            subtitle = { Text("神也会忘记密码") },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.email,
                    onValueChange = { vm.email = it.singleLined() },
                    label = { Text("邮箱") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.password,
                    onValueChange = { vm.password = it.singleLined() },
                    label = { Text("新密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                )
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = vm.passwordRepeat,
                    onValueChange = { vm.passwordRepeat = it.singleLined() },
                    label = { Text("确认密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
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
                        label = { Text("验证码") },
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
                    TextButton(modifier = Modifier, onClick = { global.nav.back() }) {
                        Text("返回")
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        modifier = Modifier.width(110.dp),
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

            if (vm.showCaptchaDialog) CaptchaDialog(onConfirm = {
                vm.captchaContinue()
            })
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