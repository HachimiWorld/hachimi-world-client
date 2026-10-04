package world.hachimi.app.ui.root.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.client_promo_banner
import hachimiworld.composeapp.generated.resources.client_promo_desc
import hachimiworld.composeapp.generated.resources.client_promo_dismiss_cd
import hachimiworld.composeapp.generated.resources.client_promo_download
import hachimiworld.composeapp.generated.resources.client_promo_title
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import world.hachimi.app.getPlatform
import world.hachimi.app.storage.MyDataStore
import world.hachimi.app.storage.PreferenceKey
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.AccentButton
import world.hachimi.app.ui.design.components.HachimiIconButton
import world.hachimi.app.ui.design.components.Icon
import world.hachimi.app.ui.design.components.Surface
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.theme.PreviewTheme
import world.hachimi.app.update.OFFICIAL_DOWNLOAD_PAGE
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/**
 * Web-only prompt suggesting the native client.
 *
 * Hidden on native platforms. Dismissing (or tapping download) snoozes it for [SNOOZE].
 */
@Stable
class ClientPromoState(
    private val dataStore: MyDataStore,
    private val scope: CoroutineScope,
) {
    var visible by mutableStateOf(false)
        private set

    suspend fun load() {
        if (!getPlatform().name.startsWith("Web")) return
        val dismissedAt = dataStore.get(KEY_DISMISSED_AT) ?: 0L
        visible = Clock.System.now().toEpochMilliseconds() - dismissedAt > SNOOZE.inWholeMilliseconds
    }

    fun dismiss() {
        visible = false
        scope.launch {
            dataStore.set(KEY_DISMISSED_AT, Clock.System.now().toEpochMilliseconds())
        }
    }

    fun download() {
        getPlatform().openUrl(OFFICIAL_DOWNLOAD_PAGE)
        dismiss()
    }

    private companion object {
        val KEY_DISMISSED_AT = PreferenceKey("client_promo_dismissed_at", Long::class)
        val SNOOZE = 14.days
    }
}

@Composable
fun rememberClientPromoState(): ClientPromoState {
    val dataStore = koinInject<MyDataStore>()
    val scope = rememberCoroutineScope()
    val state = remember(dataStore) { ClientPromoState(dataStore, scope) }
    LaunchedEffect(state) { state.load() }
    return state
}

/** Expanded (desktop browser): card at the bottom of the side rail. */
@Composable
fun ExpandedClientPromoCard(
    state: ClientPromoState,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = state.visible,
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        ExpandedClientPromoCardContent(
            onDownload = state::download,
            onDismiss = state::dismiss,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun ExpandedClientPromoCardContent(
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HachimiTheme.colorScheme.primaryContainer,
    ) {
        Column(Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = HachimiTheme.colorScheme.primary,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(Res.string.client_promo_title),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                DismissButton(onDismiss)
            }
            Text(
                text = stringResource(Res.string.client_promo_desc),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = HachimiTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 6.dp),
            )
            Spacer(Modifier.height(10.dp))
            AccentButton(
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(stringResource(Res.string.client_promo_download))
            }
        }
    }
}

/** Compact (mobile browser): slim banner under the top app bar. */
@Composable
fun CompactClientPromoBanner(
    state: ClientPromoState,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = state.visible,
        modifier = modifier,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        CompactClientPromoBannerContent(
            onDownload = state::download,
            onDismiss = state::dismiss,
        )
    }
}

@Composable
private fun CompactClientPromoBannerContent(
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HachimiTheme.colorScheme.primaryContainer)
            .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = HachimiTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(Res.string.client_promo_banner),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        AccentButton(
            onClick = onDownload,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
            shape = RoundedCornerShape(50),
        ) {
            Text(stringResource(Res.string.client_promo_download))
        }
        DismissButton(onDismiss, touchMode = true)
    }
}

@Composable
private fun DismissButton(onDismiss: () -> Unit, touchMode: Boolean = false) {
    HachimiIconButton(onClick = onDismiss, touchMode = touchMode) {
        Icon(
            Icons.Default.Close,
            contentDescription = stringResource(Res.string.client_promo_dismiss_cd),
            modifier = Modifier.size(16.dp),
            tint = HachimiTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(widthDp = 180, name = "Expanded card")
@Composable
private fun PreviewExpandedClientPromoCard() {
    PreviewTheme(background = true) {
        ExpandedClientPromoCardContent(onDownload = {}, onDismiss = {})
    }
}

@Preview(widthDp = 390, name = "Compact banner")
@Composable
private fun PreviewCompactClientPromoBanner() {
    PreviewTheme(background = true) {
        CompactClientPromoBannerContent(onDownload = {}, onDismiss = {})
    }
}
