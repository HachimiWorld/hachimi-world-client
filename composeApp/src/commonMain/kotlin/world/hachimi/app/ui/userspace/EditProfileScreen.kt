package world.hachimi.app.ui.userspace

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import coil3.request.crossfade
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.common_cancel
import hachimiworld.composeapp.generated.resources.common_ok
import hachimiworld.composeapp.generated.resources.user_change_avatar
import hachimiworld.composeapp.generated.resources.user_change_bio
import hachimiworld.composeapp.generated.resources.user_change_nickname
import hachimiworld.composeapp.generated.resources.user_connections_bilibili_uid_label
import hachimiworld.composeapp.generated.resources.user_connections_bind_title
import hachimiworld.composeapp.generated.resources.user_connections_challenge_hint
import hachimiworld.composeapp.generated.resources.user_connections_generate
import hachimiworld.composeapp.generated.resources.user_connections_link
import hachimiworld.composeapp.generated.resources.user_connections_platform_bilibili
import hachimiworld.composeapp.generated.resources.user_connections_title
import hachimiworld.composeapp.generated.resources.user_connections_unlink
import hachimiworld.composeapp.generated.resources.user_connections_unlink_confirm
import hachimiworld.composeapp.generated.resources.user_connections_verify
import hachimiworld.composeapp.generated.resources.user_edit_profile
import hachimiworld.composeapp.generated.resources.user_gender
import hachimiworld.composeapp.generated.resources.user_gender_female
import hachimiworld.composeapp.generated.resources.user_gender_male
import hachimiworld.composeapp.generated.resources.user_gender_unspecified
import hachimiworld.composeapp.generated.resources.user_profile_save
import hachimiworld.composeapp.generated.resources.user_space_user_avatar_cd
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import soup.compose.material.motion.animation.materialFadeThrough
import world.hachimi.app.api.CoilHeaders
import world.hachimi.app.api.module.UserModule
import world.hachimi.app.model.EditProfileViewModel
import world.hachimi.app.ui.LocalContentInsets
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.AlertDialog
import world.hachimi.app.ui.design.components.CircularProgressIndicator
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.design.components.ToggleButton
import world.hachimi.app.ui.util.PlatformIcons
import world.hachimi.app.util.AdaptiveScreenMargin
import world.hachimi.app.util.fillMaxWidthIn

@Composable
fun EditProfileScreen(vm: EditProfileViewModel = koinViewModel()) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AdaptiveScreenMargin)
            .navigationBarsPadding().padding(LocalContentInsets.current.asPaddingValues()).fillMaxWidthIn(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(Res.string.user_edit_profile),
            style = MaterialTheme.typography.titleLarge
        )

        AnimatedContent(
            targetState = vm.loadingProfile,
            transitionSpec = { materialFadeThrough() }
        ) { loading ->
            if (loading) {
                Box(
                    Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                vm.profile?.let { profile ->
                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(Res.string.user_change_avatar),
                            style = MaterialTheme.typography.labelLarge
                        )
                        EditableAvatar(
                            modifier = Modifier.padding(top = 8.dp),
                            avatarUrl = profile.avatarUrl,
                            isUploading = vm.avatarUploading,
                            uploadProgress = vm.avatarUploadProgress,
                            onEditClick = { vm.editAvatar() }
                        )

                        // Edit fields
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Nickname
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = stringResource(Res.string.user_change_nickname),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                TextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = vm.editUsernameValue,
                                    onValueChange = { vm.editUsernameValue = it },
                                    singleLine = true
                                )
                            }

                            // Bio
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = stringResource(Res.string.user_change_bio),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                TextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = vm.editBioValue,
                                    onValueChange = { vm.editBioValue = it },
                                    minLines = 3
                                )
                            }

                            // Gender
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = stringResource(Res.string.user_gender),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                GenderSelector(
                                    value = vm.editGenderValue,
                                    onSelect = { vm.editGenderValue = it }
                                )
                            }

                            // Save button
                            AccentButton(
                                onClick = { vm.saveProfile() },
                                enabled = !vm.operating && vm.editUsernameValue.isNotBlank()
                            ) {
                                if (vm.operating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(stringResource(Res.string.user_profile_save))
                            }
                        }

                        ConnectionsManagementSection(
                            vm = vm,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }

    EditBindBilibiliDialog(vm)
    EditUnlinkConfirmDialog(vm)
}

@Composable
private fun EditableAvatar(
    avatarUrl: String?,
    isUploading: Boolean,
    uploadProgress: Float,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(modifier = modifier.size(88.dp), shape = CircleShape) {
        Box(
            modifier = Modifier.clickable(
                onClick = onEditClick,
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.Black)
            ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .httpHeaders(CoilHeaders)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(Res.string.user_space_user_avatar_cd),
                modifier = Modifier.fillMaxSize(),
                filterQuality = FilterQuality.High,
                contentScale = ContentScale.Crop
            )
            if (interactionSource.collectIsHoveredAsState().value || interactionSource.collectIsPressedAsState().value) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0f, 0f, 0f, 0.35f)))
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
            }
            if (isUploading) {
                if (uploadProgress > 0f && uploadProgress < 1f)
                    CircularProgressIndicator(progress = { uploadProgress })
                else CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun GenderSelector(
    value: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            0 to stringResource(Res.string.user_gender_male),
            1 to stringResource(Res.string.user_gender_female),
            null to stringResource(Res.string.user_gender_unspecified),
        ).forEach { (optValue, label) ->
            ToggleButton(
                selected = value == optValue,
                onClick = { onSelect(optValue) }
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ConnectionsManagementSection(
    vm: EditProfileViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(Res.string.user_connections_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (vm.loadingConnections) {
            Box(
                Modifier.fillMaxWidth().height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        } else {
            // Show one row per supported platform type
            ConnectionPlatformRow(
                type = UserModule.CONNECTION_TYPE_BILIBILI,
                platformName = stringResource(Res.string.user_connections_platform_bilibili),
                connection = vm.connections.find { it.type == UserModule.CONNECTION_TYPE_BILIBILI },
                onLink = { vm.startBind() },
                onUnlink = { vm.requestUnlink(UserModule.CONNECTION_TYPE_BILIBILI) }
            )
        }
    }
}

@Composable
private fun ConnectionPlatformRow(
    type: String,
    platformName: String,
    connection: UserModule.ConnectionItem?,
    onLink: () -> Unit,
    onUnlink: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Platform icon
        when (type) {
            UserModule.CONNECTION_TYPE_BILIBILI -> Icon(
                painter = painterResource(PlatformIcons.bilibili),
                contentDescription = type,
                tint = Color(0xFFFF6699),
                modifier = Modifier.size(24.dp)
            )
            else -> Icon(Icons.Default.Link, contentDescription = type, modifier = Modifier.size(24.dp))
        }

        // Platform name + linked account
        Column(Modifier.weight(1f)) {
            Text(
                text = platformName,
                style = MaterialTheme.typography.bodyMedium
            )
            if (connection != null) Row {
                Text(
                    text = connection.name,
                    style = MaterialTheme.typography.bodySmall,
                )

                Text(
                    modifier = Modifier.padding(start = 4.dp),
                    text = connection.id,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        // Action button
        if (connection != null) {
            TextButton(onClick = onUnlink) {
                Text(stringResource(Res.string.user_connections_unlink))
            }
        } else {
            TextButton(onClick = onLink) {
                Text(stringResource(Res.string.user_connections_link))
            }
        }
    }
}

@Composable
private fun EditBindBilibiliDialog(vm: EditProfileViewModel) {
    if (!vm.showBindDialog) return

    AlertDialog(
        onDismissRequest = { vm.dismissBind() },
        title = { Text(stringResource(Res.string.user_connections_bind_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (vm.bindChallenge == null) {
                    Text(
                        text = stringResource(Res.string.user_connections_bilibili_uid_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    TextField(
                        modifier = Modifier.requiredWidth(280.dp),
                        value = vm.bindBilibiliUid,
                        onValueChange = { vm.bindBilibiliUid = it },
                        singleLine = true,
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.user_connections_challenge_hint),
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = vm.bindChallenge ?: "",
                        onValueChange = {},
                        singleLine = true,
                        enabled = true
                    )
                }
            }
        },
        confirmButton = {
            if (vm.bindChallenge == null) {
                TextButton(
                    onClick = { vm.generateChallenge() },
                    enabled = !vm.bindGenerating && vm.bindBilibiliUid.isNotBlank()
                ) {
                    if (vm.bindGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(Res.string.user_connections_generate))
                }
            } else {
                TextButton(
                    onClick = { vm.verifyChallenge() },
                    enabled = !vm.bindVerifying
                ) {
                    if (vm.bindVerifying) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(Res.string.user_connections_verify))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.dismissBind() }) {
                Text(stringResource(Res.string.common_cancel))
            }
        }
    )
}

@Composable
private fun EditUnlinkConfirmDialog(vm: EditProfileViewModel) {
    if (!vm.showUnlinkConfirm) return
    AlertDialog(
        onDismissRequest = { vm.dismissUnlink() },
        title = { Text(stringResource(Res.string.user_connections_unlink)) },
        text = { Text(stringResource(Res.string.user_connections_unlink_confirm)) },
        confirmButton = {
            TextButton(
                onClick = { vm.confirmUnlink() },
                enabled = !vm.operating
            ) {
                Text(stringResource(Res.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.dismissUnlink() }) {
                Text(stringResource(Res.string.common_cancel))
            }
        }
    )
}


