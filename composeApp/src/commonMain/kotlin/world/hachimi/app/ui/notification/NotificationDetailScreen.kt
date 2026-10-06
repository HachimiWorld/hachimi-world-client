package world.hachimi.app.ui.notification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.notification_back_to_inbox
import hachimiworld.composeapp.generated.resources.notification_detail_title
import hachimiworld.composeapp.generated.resources.notification_load_error
import hachimiworld.composeapp.generated.resources.notification_mark_read_failed
import hachimiworld.composeapp.generated.resources.notification_retry
import hachimiworld.composeapp.generated.resources.notification_unavailable
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.NotificationDetailViewModel
import world.hachimi.app.model.toTarget
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.LocalWindowSize
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.SubtleButton
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.notification.components.NotificationMessageState
import world.hachimi.app.ui.util.WindowSize
import world.hachimi.app.ui.util.listTailPadding
import world.hachimi.app.util.YMDHM
import world.hachimi.app.util.formatTime

/** Detail as its own page, for narrow windows and links. */
@Composable
fun NotificationDetailScreen(notificationId: String) {
    val navigator = LocalNavigator.current
    ScreenScaffold(
        title = { Text(stringResource(Res.string.notification_detail_title), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
    ) {
        NotificationDetailContent(
            notificationId = notificationId,
            onBackToInbox = {
                if (navigator.canGoBack) navigator.back() else navigator.push(Route.Root.Notifications)
            },
        )
    }
}

/** Shared by the detail page and the right pane of the two-pane inbox. */
@Composable
fun NotificationDetailContent(
    notificationId: String,
    onBackToInbox: (() -> Unit)?,
    modifier: Modifier = Modifier,
    vm: NotificationDetailViewModel = koinViewModel(key = "notification_$notificationId") {
        parametersOf(notificationId)
    },
) {
    val navigator = LocalNavigator.current
    LaunchedEffect(vm) { vm.mounted() }

    when (vm.initializeStatus) {
        InitializeStatus.INIT -> Unit
        InitializeStatus.FAILED -> NotificationMessageState(
            title = stringResource(
                if (vm.unavailable) Res.string.notification_unavailable else Res.string.notification_load_error
            ),
            modifier = modifier.listTailPadding(),
            action = {
                if (vm.unavailable) {
                    if (onBackToInbox != null) SubtleButton(onClick = onBackToInbox) {
                        Text(stringResource(Res.string.notification_back_to_inbox))
                    }
                } else {
                    AccentButton(onClick = { vm.retry() }) {
                        Text(stringResource(Res.string.notification_retry))
                    }
                }
            },
        )
        InitializeStatus.LOADED -> {
            val item = vm.item ?: return
            val isCompact = LocalWindowSize.current.width < WindowSize.COMPACT
            Column(
                modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .listTailPadding()
                    .padding(horizontal = if (isCompact) 16.dp else 32.dp, vertical = 20.dp)
            ) {
                Column(Modifier.widthIn(max = 640.dp)) {
                    SelectionContainer {
                        Text(
                            text = item.title,
                            fontSize = if (isCompact) 22.sp else 26.sp,
                            lineHeight = if (isCompact) 30.sp else 36.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = formatTime(item.createTime, fullFormat = LocalDateTime.Formats.YMDHM),
                        modifier = Modifier.padding(top = 8.dp),
                        fontSize = 13.sp,
                        color = HachimiTheme.colorScheme.onSurfaceVariant,
                    )
                    if (vm.markReadFailed) {
                        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(Res.string.notification_mark_read_failed),
                                fontSize = 13.sp,
                                color = HachimiTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = { vm.markReadIfUnread() }) {
                                Text(stringResource(Res.string.notification_retry))
                            }
                        }
                    }
                    SelectionContainer(Modifier.padding(top = 20.dp)) {
                        Text(text = item.body, fontSize = 15.sp, lineHeight = 24.sp)
                    }
                    val target = item.contentIntent?.toTarget()
                    if (target != null) {
                        AccentButton(
                            onClick = { navigator.push(target.route) },
                            modifier = Modifier
                                .padding(top = 28.dp)
                                .then(if (isCompact) Modifier.fillMaxWidth() else Modifier),
                            contentPadding = PaddingValues(
                                horizontal = 20.dp,
                                vertical = if (isCompact) 12.dp else 9.dp,
                            ),
                        ) {
                            Text(stringResource(target.label))
                        }
                    }
                }
            }
        }
    }
}
