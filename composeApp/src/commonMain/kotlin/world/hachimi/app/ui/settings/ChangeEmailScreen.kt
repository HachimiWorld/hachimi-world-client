package world.hachimi.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.autofill.contentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.auth_resend_seconds
import hachimiworld.composeapp.generated.resources.auth_send
import hachimiworld.composeapp.generated.resources.change_email_cancel
import hachimiworld.composeapp.generated.resources.change_email_cancel_confirm_text
import hachimiworld.composeapp.generated.resources.change_email_cancel_confirm_title
import hachimiworld.composeapp.generated.resources.change_email_cancel_keep
import hachimiworld.composeapp.generated.resources.change_email_current
import hachimiworld.composeapp.generated.resources.change_email_new_code
import hachimiworld.composeapp.generated.resources.change_email_new_email
import hachimiworld.composeapp.generated.resources.change_email_next_request
import hachimiworld.composeapp.generated.resources.change_email_old_code
import hachimiworld.composeapp.generated.resources.change_email_password
import hachimiworld.composeapp.generated.resources.change_email_pending_text
import hachimiworld.composeapp.generated.resources.change_email_pending_title
import hachimiworld.composeapp.generated.resources.change_email_request_submit
import hachimiworld.composeapp.generated.resources.change_email_submit
import hachimiworld.composeapp.generated.resources.change_email_success_text
import hachimiworld.composeapp.generated.resources.change_email_success_title
import hachimiworld.composeapp.generated.resources.change_email_choose_hint
import hachimiworld.composeapp.generated.resources.change_email_method_old_email
import hachimiworld.composeapp.generated.resources.change_email_method_old_email_desc
import hachimiworld.composeapp.generated.resources.change_email_method_password
import hachimiworld.composeapp.generated.resources.change_email_method_password_desc
import hachimiworld.composeapp.generated.resources.change_email_with_old_hint
import hachimiworld.composeapp.generated.resources.change_email_without_old_hint
import hachimiworld.composeapp.generated.resources.common_ok
import hachimiworld.composeapp.generated.resources.settings_change_email
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.AuthModule
import world.hachimi.app.model.ChangeEmailViewModel
import world.hachimi.app.model.EmailChangeMethod
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.ui.auth.components.PasswordToggleButton
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.InitStatusScaffold
import world.hachimi.app.ui.util.fillMaxWidthIn
import world.hachimi.app.ui.util.listTailPadding
import world.hachimi.app.util.YMDHM
import world.hachimi.app.util.formatTime
import world.hachimi.app.util.singleLined

/** @since 261105 */
@Composable
fun ChangeEmailScreen(vm: ChangeEmailViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    DisposableEffect(vm) {
        vm.mounted()
        onDispose { vm.dispose() }
    }

    ScreenScaffold(
        title = { Text(stringResource(Res.string.settings_change_email), maxLines = 1) },
        showBack = true,
        onBack = { if (vm.method != null) vm.chooseMethod(null) else navigator.back() },
    ) {
        InitStatusScaffold(
            initializeStatus = vm.initializeStatus,
            isLoading = vm.loading,
            onRetryClick = { vm.retry() },
            modifier = Modifier.fillMaxSize()
        ) {
            val info = vm.info
            if (info != null) Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AdaptiveScreenMargin)
                    .listTailPadding()
                    .fillMaxWidthIn(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    stringResource(Res.string.change_email_current, info.email),
                    style = MaterialTheme.typography.titleMedium
                )
                val pending = info.pending
                if (pending != null) {
                    PendingCard(pending, vm)
                } else if (vm.method == null) {
                    MethodChooser(info, vm)
                } else {
                    Form(info, vm)
                }
            }
        }
    }

    vm.changedTo?.let { email ->
        AlertDialog(
            onDismissRequest = { vm.closeSuccessDialog() },
            confirmButton = {
                TextButton(onClick = { vm.closeSuccessDialog() }) { Text(stringResource(Res.string.common_ok)) }
            },
            title = { Text(stringResource(Res.string.change_email_success_title)) },
            text = { Text(stringResource(Res.string.change_email_success_text, email)) }
        )
    }

    if (vm.showCancelConfirm) AlertDialog(
        onDismissRequest = { vm.showCancelConfirm = false },
        confirmButton = {
            TextButton(onClick = { vm.cancelPending() }) { Text(stringResource(Res.string.change_email_cancel)) }
        },
        dismissButton = {
            TextButton(onClick = { vm.showCancelConfirm = false }) {
                Text(stringResource(Res.string.change_email_cancel_keep))
            }
        },
        title = { Text(stringResource(Res.string.change_email_cancel_confirm_title)) },
        text = { Text(stringResource(Res.string.change_email_cancel_confirm_text)) }
    )
}

@Composable
private fun PendingCard(pending: AuthModule.PendingEmailChange, vm: ChangeEmailViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(Res.string.change_email_pending_title), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(
                    Res.string.change_email_pending_text,
                    pending.newEmail,
                    formatTime(pending.effectiveTime, fullFormat = LocalDateTime.Formats.YMDHM)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { vm.showCancelConfirm = true },
                enabled = !vm.operating
            ) {
                Text(stringResource(Res.string.change_email_cancel))
            }
        }
    }
}

@Composable
private fun MethodChooser(info: AuthModule.EmailInfoResp, vm: ChangeEmailViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(Res.string.change_email_choose_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = HachimiTheme.colorScheme.onSurfaceVariant
        )
        MethodEntry(
            icon = Icons.Outlined.Mail,
            title = stringResource(Res.string.change_email_method_old_email),
            description = stringResource(Res.string.change_email_method_old_email_desc),
            onClick = { vm.chooseMethod(EmailChangeMethod.OLD_EMAIL) },
        )
        val nextRequestTime = info.nextRequestTime
        MethodEntry(
            icon = Icons.Outlined.Lock,
            title = stringResource(Res.string.change_email_method_password),
            description = stringResource(Res.string.change_email_method_password_desc),
            note = nextRequestTime?.let {
                stringResource(Res.string.change_email_next_request, formatTime(it, fullFormat = LocalDateTime.Formats.YMDHM))
            },
            onClick = if (nextRequestTime == null) {
                { vm.chooseMethod(EmailChangeMethod.PASSWORD) }
            } else null,
        )
    }
}

@Composable
private fun MethodEntry(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)?,
    note: String? = null,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val alpha = if (onClick != null) 1f else 0.5f
            Icon(icon, contentDescription = null, modifier = Modifier.alpha(alpha))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, modifier = Modifier.alpha(alpha), style = MaterialTheme.typography.titleSmall)
                Text(
                    description,
                    modifier = Modifier.alpha(alpha),
                    style = MaterialTheme.typography.bodySmall,
                    color = HachimiTheme.colorScheme.onSurfaceVariant
                )
                if (note != null) Text(
                    note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (onClick != null) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun Form(info: AuthModule.EmailInfoResp, vm: ChangeEmailViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(
                if (vm.withOldEmail) Res.string.change_email_method_old_email
                else Res.string.change_email_method_password
            ),
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            stringResource(
                if (vm.withOldEmail) Res.string.change_email_with_old_hint
                else Res.string.change_email_without_old_hint
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = HachimiTheme.colorScheme.onSurfaceVariant
        )

        val nextRequestTime = info.nextRequestTime
        val blocked = !vm.withOldEmail && nextRequestTime != null
        if (blocked && nextRequestTime != null) {
            Text(
                stringResource(
                    Res.string.change_email_next_request,
                    formatTime(nextRequestTime, fullFormat = LocalDateTime.Formats.YMDHM)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (vm.withOldEmail) {
            CodeField(
                value = vm.oldCode,
                onValueChange = { vm.oldCode = it },
                placeholder = stringResource(Res.string.change_email_old_code),
                remainSecs = vm.oldCodeRemainSecs,
                sendEnabled = !vm.operating,
                onSend = { vm.sendOldCode() },
            )
        }

        TextField(
            modifier = Modifier.fillMaxWidth().contentType(ContentType.EmailAddress),
            value = vm.newEmail,
            onValueChange = { vm.newEmail = it.singleLined() },
            leadingIcon = { Icon(Icons.Outlined.Mail, null) },
            placeholder = { Text(stringResource(Res.string.change_email_new_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        CodeField(
            value = vm.newCode,
            onValueChange = { vm.newCode = it },
            placeholder = stringResource(Res.string.change_email_new_code),
            remainSecs = vm.newCodeRemainSecs,
            sendEnabled = !vm.operating && vm.newEmail.isNotBlank() && !blocked,
            onSend = { vm.sendNewCode() },
        )

        if (!vm.withOldEmail) {
            var showPassword by remember { mutableStateOf(false) }
            TextField(
                modifier = Modifier.fillMaxWidth().contentType(ContentType.Password),
                value = vm.password,
                onValueChange = { vm.password = it.singleLined() },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                placeholder = { Text(stringResource(Res.string.change_email_password)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                trailingIcon = { PasswordToggleButton(showPassword, onValueChange = { showPassword = it }) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            )
        }

        val filled = vm.newEmail.isNotBlank() && vm.newCode.isNotBlank() &&
                if (vm.withOldEmail) vm.oldCode.isNotBlank() else vm.password.isNotBlank()
        AccentButton(
            modifier = Modifier.align(Alignment.End).widthIn(min = 112.dp),
            onClick = { vm.submit() },
            enabled = filled && !blocked && !vm.operating
        ) {
            Text(
                stringResource(
                    if (vm.withOldEmail) Res.string.change_email_submit else Res.string.change_email_request_submit
                )
            )
        }
    }
}

@Composable
private fun CodeField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    remainSecs: Int,
    sendEnabled: Boolean,
    onSend: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            modifier = Modifier.weight(1f).contentType(ContentType.SmsOtpCode),
            value = value,
            onValueChange = { onValueChange(it.singleLined()) },
            leadingIcon = { Icon(Icons.Outlined.Security, null) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.padding(start = 8.dp))
        val canSend = remainSecs < 0
        TextButton(onClick = onSend, enabled = canSend && sendEnabled) {
            Text(
                if (canSend) stringResource(Res.string.auth_send)
                else stringResource(Res.string.auth_resend_seconds, remainSecs)
            )
        }
    }
}
