package world.hachimi.app.ui.committee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.committee_appoint
import hachimiworld.composeapp.generated.resources.committee_appoint_hint
import hachimiworld.composeapp.generated.resources.committee_appointed_by
import hachimiworld.composeapp.generated.resources.committee_members
import hachimiworld.composeapp.generated.resources.committee_members_empty
import hachimiworld.composeapp.generated.resources.committee_revoke
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import world.hachimi.app.model.CommitteeMembersViewModel
import world.hachimi.app.nav.LocalNavigator
import world.hachimi.app.nav.Route
import world.hachimi.app.ui.component.ScreenScaffold
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton
import world.hachimi.app.ui.design.components.TextField
import world.hachimi.app.ui.design.components.Card
import world.hachimi.app.ui.message.components.cardItems
import world.hachimi.app.ui.util.AdaptiveScreenMargin
import world.hachimi.app.ui.util.InitStatusScaffold
import world.hachimi.app.ui.util.listTailSpacerItem

@Composable
fun CommitteeMembersScreen(vm: CommitteeMembersViewModel = koinViewModel()) {
    val navigator = LocalNavigator.current
    LaunchedEffect(vm) { vm.mounted() }

    ScreenScaffold(
        title = { Text(stringResource(Res.string.committee_members), maxLines = 1) },
        showBack = true,
        onBack = navigator::back,
    ) {
        InitStatusScaffold(
            initializeStatus = vm.initializeStatus,
            isLoading = false,
            onRetryClick = vm::retry,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
                    contentPadding = PaddingValues(AdaptiveScreenMargin),
                ) {
                    if (vm.canManage) {
                        item(key = "appoint") {
                            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    TextField(
                                        value = vm.uidInput,
                                        onValueChange = { value -> vm.uidInput = value.filter { it.isDigit() } },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text(stringResource(Res.string.committee_appoint_hint)) },
                                        singleLine = true,
                                        enabled = !vm.working,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    )
                                    AccentButton(
                                        onClick = vm::appoint,
                                        enabled = vm.uidInput.isNotEmpty() && !vm.working,
                                        modifier = Modifier.padding(start = 12.dp),
                                    ) {
                                        Text(stringResource(Res.string.committee_appoint))
                                    }
                                }
                            }
                        }
                    }
                    item(key = "title") {
                        SectionTitle(stringResource(Res.string.committee_members), trailing = vm.members.size.takeIf { it > 0 }?.toString())
                    }
                    if (vm.members.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                stringResource(Res.string.committee_members_empty),
                                modifier = Modifier.padding(vertical = 24.dp),
                                color = HachimiTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    cardItems(vm.members.toList(), key = { it.user.uid }) { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navigator.push(Route.Root.PublicUserSpace(member.user.uid)) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(member.user.avatarUrl, 40.dp)
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(member.user.username, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                member.appointedBy?.let {
                                    Text(
                                        stringResource(Res.string.committee_appointed_by, it.username),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HachimiTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            if (vm.canManage) {
                                TextButton(onClick = { vm.revoke(member.user.uid) }, enabled = !vm.working) {
                                    Text(stringResource(Res.string.committee_revoke))
                                }
                            }
                        }
                    }
                    listTailSpacerItem()
                }
            }
        }
    }
}
