package world.hachimi.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.settings_device_current_label
import hachimiworld.composeapp.generated.resources.settings_device_empty
import hachimiworld.composeapp.generated.resources.settings_device_fisrt_login
import hachimiworld.composeapp.generated.resources.settings_device_last_active
import hachimiworld.composeapp.generated.resources.settings_device_load_error
import hachimiworld.composeapp.generated.resources.settings_device_logout
import hachimiworld.composeapp.generated.resources.settings_device_logout_cancel
import hachimiworld.composeapp.generated.resources.settings_device_logout_confirm
import hachimiworld.composeapp.generated.resources.settings_device_logout_confirm_message
import hachimiworld.composeapp.generated.resources.settings_device_logout_confirm_title
import hachimiworld.composeapp.generated.resources.settings_device_management
import hachimiworld.composeapp.generated.resources.settings_device_other_label
import hachimiworld.composeapp.generated.resources.settings_device_retry
import hachimiworld.composeapp.generated.resources.settings_device_unknown
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.api.module.AuthModule
import world.hachimi.app.model.DeviceManagementViewModel
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.Button
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.util.listTailSpacerItem
import world.hachimi.app.util.AdaptiveScreenMargin
import world.hachimi.app.util.YMD
import world.hachimi.app.util.formatDistance

@Composable
fun DeviceManagementScreen(
    vm: DeviceManagementViewModel = koinViewModel()
) {
    DisposableEffect(Unit) {
        vm.mounted()
        onDispose { }
    }

    Column(Modifier.fillMaxSize()) {
        // Title
        Text(
            modifier = Modifier.padding(AdaptiveScreenMargin),
            text = stringResource(Res.string.settings_device_management),
            style = MaterialTheme.typography.titleLarge
        )

        // Content
        when {
            vm.initializeStatus == InitializeStatus.INIT && vm.loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            vm.initializeStatus == InitializeStatus.FAILED && vm.devices.isEmpty() -> {
                ErrorState(
                    message = vm.error ?: stringResource(Res.string.settings_device_load_error),
                    onRetry = { vm.loadDevices() }
                )
            }

            vm.devices.isEmpty() && vm.initializeStatus != InitializeStatus.INIT -> {
                EmptyState()
            }

            else -> {
                DeviceList(vm)
            }
        }
    }

    vm.logoutTarget?.let { device ->
        LogoutConfirmDialog(device, vm)
    }
}

@Composable
private fun DeviceList(vm: DeviceManagementViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = AdaptiveScreenMargin, vertical = 8.dp)
    ) {
        vm.currentDevice?.let { current ->
            item(key = "current_device") {
                Column {
                    Text(stringResource(Res.string.settings_device_current_label), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    DeviceCard(current, vm, isCurrent = true)
                }
            }
        }
        item {
            Text(
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                text = stringResource(Res.string.settings_device_other_label), style = MaterialTheme.typography.titleSmall
            )
        }
        items(vm.otherDevices.toList(), key = { it.id }) { device ->
            DeviceCard(device, vm, isCurrent = false)
        }
        listTailSpacerItem()
    }
}

@Composable
private fun DeviceCard(
    device: AuthModule.DeviceItem,
    vm: DeviceManagementViewModel,
    isCurrent: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowRow(itemVerticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = device.deviceInfo
                            ?: stringResource(Res.string.settings_device_unknown),
                        style = MaterialTheme.typography.titleSmall,
                    )

                    // IP address
                    device.ipAddress?.let { ip ->
                        Text(
                            text = ip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }


                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_device_fisrt_login,
                            formatDistance(device.createTime, precise = false, fullFormat = LocalDateTime.Formats.YMD)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Last active time
                    device.lastUsedTime?.let { lastUsed ->
                        Text(
                            text = stringResource(
                                Res.string.settings_device_last_active,
                                formatDistance(lastUsed, precise = false, thresholdDay = Int.MAX_VALUE)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HachimiIconButton(
                onClick = {
                    vm.showLogoutConfirm(device)
                },
                enabled = device.id !in vm.logoutLoading
            ) {
                if (device.id in vm.logoutLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.settings_device_logout))
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.settings_device_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(stringResource(Res.string.settings_device_retry))
            }
        }
    }
}

@Composable
private fun LogoutConfirmDialog(
    device: AuthModule.DeviceItem,
    vm: DeviceManagementViewModel
) {
    AlertDialog(
        onDismissRequest = { vm.dismissLogoutConfirm() },
        title = {
            Text(stringResource(Res.string.settings_device_logout_confirm_title))
        },
        text = {
            Text(
                stringResource(
                    Res.string.settings_device_logout_confirm_message,
                    device.deviceInfo ?: stringResource(Res.string.settings_device_unknown)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmLogout() },
                enabled = device.id !in vm.logoutLoading
            ) {
                Text(
                    if (device.id in vm.logoutLoading) "..."
                    else stringResource(Res.string.settings_device_logout_confirm)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.dismissLogoutConfirm() }) {
                Text(stringResource(Res.string.settings_device_logout_cancel))
            }
        },
    )
}

