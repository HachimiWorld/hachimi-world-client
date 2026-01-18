package world.hachimi.app.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_login
import hachimiworld.composeapp.generated.resources.auth_register
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import soup.compose.material.motion.animation.materialSharedAxisX
import soup.compose.material.motion.animation.rememberSlideDistance
import world.hachimi.app.model.AuthViewModel
import world.hachimi.app.model.GlobalStore
import world.hachimi.app.ui.auth.components.CaptchaDialog
import world.hachimi.app.ui.auth.components.FormContent
import world.hachimi.app.ui.auth.components.PasswordToggleButton
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.insets.currentSafeAreaInsets
import world.hachimi.app.util.singleLined
import world.hachimi.app.util.validateEmailPattern
import world.hachimi.app.util.validatePasswordPattern

@Composable
fun AuthScreen(
    displayLoginAsInitial: Boolean,
    vm: AuthViewModel = koinViewModel()
) {
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.unmount() }
    }
    val global = koinInject<GlobalStore>()
    var isLogin by remember(displayLoginAsInitial) { mutableStateOf(displayLoginAsInitial) }

    Box(Modifier.fillMaxSize().padding(top = currentSafeAreaInsets().top)) {
        HachimiIconButton(
            modifier = Modifier.padding(24.dp).align(Alignment.TopStart),
            onClick = { global.nav.back() },
            touchMode = true
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Card(
            Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = 512.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            val form = when {
                isLogin -> Form.Login
                vm.regStep == 0 -> Form.Register
                vm.regStep == 1 -> Form.RegisterVerify
                vm.regStep == 2 -> Form.RegisterProfile
                else -> error("unreachable")
            }

            val slideDistance = rememberSlideDistance()

            AnimatedContent(
                modifier = Modifier,
                targetState = form,
                transitionSpec = {
                    materialSharedAxisX(targetState > initialState, slideDistance)
                }
            ) { page ->
                Box(Modifier.padding(horizontal = 32.dp, vertical = 24.dp)) {
                    when (page) {
                        Form.Login -> LoginForm(
                            vm = vm,
                            toRegister = { isLogin = false }
                        )

                        Form.Register -> RegisterForm(
                            vm = vm,
                            toLogin = { isLogin = true }
                        )

                        Form.RegisterVerify -> RegisterVerifyForm(vm)
                        Form.RegisterProfile -> RegisterProfileForm(vm)
                    }
                }
            }
        }
    }

    if (vm.showCaptchaDialog) CaptchaDialog(
        processing = vm.isOperating,
        onConfirm = { vm.finishCaptcha() }
    )
}

private enum class Form {
    Login, Register, RegisterVerify, RegisterProfile
}

@Composable
private fun LoginForm(vm: AuthViewModel, toRegister: () -> Unit) {
    FormContent(
        title = { Text("欢迎回家") },
        subtitle = {}
    ) {
        Column(Modifier) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = vm.email,
                onValueChange = { vm.email = it.singleLined() },
                placeholder = { Text("邮箱") },
                leadingIcon = { Icon(Icons.Outlined.Mail, null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
            )
            Spacer(Modifier.height(24.dp))
            var showPassword by remember { mutableStateOf(false) }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = vm.password,
                onValueChange = { vm.password = it.singleLined() },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                placeholder = { Text("密码") },
                trailingIcon = {
                    PasswordToggleButton(
                        showPassword,
                        onValueChange = { showPassword = it })
                },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (!vm.isOperating && vm.email.isNotBlank() && vm.password.isNotBlank()) {
                        vm.startLogin()
                    }
                })
            )
            Spacer(Modifier.height(8.dp))
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { vm.forgetPassword() }
            ) {
                Text("忘记密码？")
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth()) {
                TextButton(modifier = Modifier.height(48.dp), onClick = toRegister) {
                    Text(stringResource(Res.string.auth_register))
                }

                Spacer(Modifier.weight(1f))

                AccentButton(
                    modifier = Modifier.size(width = 112.dp, height = 48.dp),
                    onClick = { vm.startLogin() },
                    enabled = !vm.isOperating && vm.email.isNotBlank() && vm.password.isNotBlank(),
                ) {
                    Text(stringResource(Res.string.auth_login))
                }
            }
        }
    }
}

@Composable
private fun RegisterForm(vm: AuthViewModel, toLogin: () -> Unit) {
    FormContent(
        title = { Text("成为神人") },
        subtitle = {}
    ) {
        Column(Modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            var emailHelp by remember { mutableStateOf("") }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = vm.regEmail,
                onValueChange = {
                    vm.regEmail = it.singleLined()
                    emailHelp =
                        if (validateEmailPattern(vm.regEmail)) ""
                        else "邮箱格式不正确"
                },
                leadingIcon = { Icon(Icons.Outlined.Mail, null) },
                placeholder = { Text("邮箱") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                supportingText = {
                    Text(emailHelp)
                }
            )
            var showPassword by remember { mutableStateOf(false) }
            var passwordHelp by remember { mutableStateOf("") }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = vm.regPassword,
                onValueChange = {
                    vm.regPassword = it.singleLined()
                    passwordHelp =
                        if (validatePasswordPattern(vm.regPassword)) ""
                        else "密码长度至少为 6 位"
                },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                placeholder = { Text("密码") },
                trailingIcon = {
                    PasswordToggleButton(
                        showPassword,
                        onValueChange = { showPassword = it }
                    )
                },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                supportingText = {
                    Text(passwordHelp)
                }
            )
            var showRepeatPassword by remember { mutableStateOf(false) }
            var repeatPasswordHelp by remember { mutableStateOf("") }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = vm.regPasswordRepeat,
                onValueChange = {
                    vm.regPasswordRepeat = it.singleLined()
                    repeatPasswordHelp =
                        if (vm.regPasswordRepeat != vm.regPassword) "两次输入不一致"
                        else ""
                },
                leadingIcon = { Icon(Icons.Outlined.SettingsBackupRestore, null) },
                placeholder = { Text("确认密码") },
                trailingIcon = {
                    PasswordToggleButton(
                        showRepeatPassword,
                        onValueChange = { showRepeatPassword = it }
                    )
                },
                singleLine = true,
                visualTransformation = if (showRepeatPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                supportingText = {
                    Text(repeatPasswordHelp)
                }
            )

            val enabled by remember {
                derivedStateOf {
                    vm.regEmail.isNotBlank() && vm.regPassword.isNotBlank()
                            && vm.regPassword == vm.regPasswordRepeat
                }
            }

            Row(Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier.size(width = 112.dp, height = 48.dp),
                    onClick = toLogin
                ) {
                    Text(stringResource(Res.string.auth_login))
                }

                Spacer(Modifier.weight(1f))

                AccentButton(
                    modifier = Modifier.size(width = 112.dp, height = 48.dp),
                    onClick = { vm.regNextStep() },
                    enabled = enabled && !vm.isOperating,
                ) {
                    Text("下一步")
                }
            }
        }
    }
}

@Composable
private fun RegisterVerifyForm(vm: AuthViewModel) {
    FormContent(
        title = { Text("离神人很近了") },
        subtitle = { Text("已将验证码发送至邮箱，如未收到请检查垃圾箱") }
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TextField(
                    modifier = Modifier.weight(1f),
                    value = vm.regCode,
                    onValueChange = { vm.regCode = it.singleLined() },
                    leadingIcon = { Icon(Icons.Outlined.Security, null) },
                    placeholder = { Text("验证码") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (!vm.isOperating && vm.regCode.isNotBlank()) {
                            vm.regNextStep()
                        }
                    })
                )
            }
            Spacer(Modifier.height(8.dp))
            val sendCodeEnabled = vm.regCodeRemainSecs < 0
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { vm.regSendEmailCode() },
                enabled = sendCodeEnabled && !vm.isOperating
            ) {
                Text(
                    if (sendCodeEnabled) "重新发送"
                    else "重新发送 (${vm.regCodeRemainSecs} 秒)"
                )
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier.size(width = 112.dp, height = 48.dp),
                    onClick = { vm.regStep = 0 }
                ) {
                    Text("上一步")
                }

                Spacer(Modifier.weight(1f))

                AccentButton(
                    modifier = Modifier.size(width = 112.dp, height = 48.dp),
                    onClick = { vm.regNextStep() },
                    enabled = vm.regCode.isNotBlank() && !vm.isOperating
                ) {
                    Text("下一步")
                }
            }
        }
    }
}

@Composable
private fun RegisterProfileForm(vm: AuthViewModel) {
    FormContent(
        title = { Text("只差一步") },
        subtitle = { Text("完善你的资料") }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = vm.name,
                onValueChange = { vm.name = it.singleLined() },
                placeholder = { Text("昵称") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = vm.intro,
                onValueChange = { vm.intro = it },
                placeholder = { Text("介绍一下") },
                maxLines = 4,
                minLines = 4,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = vm.gender == 0, onClick = { vm.gender = 0 })
                Text("男")

                RadioButton(selected = vm.gender == 1, onClick = { vm.gender = 1 })
                Text("女")

                RadioButton(selected = vm.gender == 2, onClick = { vm.gender = 2 })
                Text("神没有性别")
            }

            AccentButton(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                onClick = { vm.finishRegister() },
                enabled = vm.name.isNotBlank() && vm.intro.isNotBlank() && vm.gender != null && !vm.isOperating
            ) {
                Text("完成")
            }

            TextButton(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                onClick = { vm.skipProfile() }
            ) {
                Text("跳过")
            }
        }
    }
}