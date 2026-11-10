package world.hachimi.app.ui.moment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hachimiworld.composeapp.generated.resources.Res
import hachimiworld.composeapp.generated.resources.moment_empty
import hachimiworld.composeapp.generated.resources.moment_load_error
import hachimiworld.composeapp.generated.resources.moment_load_more_failed
import hachimiworld.composeapp.generated.resources.notification_retry
import org.jetbrains.compose.resources.stringResource
import world.hachimi.app.model.InitializeStatus
import world.hachimi.app.model.MomentPager
import world.hachimi.app.ui.component.LoadMoreItem
import world.hachimi.app.ui.design.HachimiTheme
import world.hachimi.app.ui.design.components.Text
import world.hachimi.app.ui.design.components.TextButton

/**
 * The moments of [pager] as list items, each a [MomentCard] with a line under it, and what to show
 * while there are none. The list gives no side padding: each moment runs its ripple and its line
 * from edge to edge.
 *
 * @since 261106
 */
fun LazyListScope.momentItems(pager: MomentPager, actions: MomentActions, empty: String? = null) {
    when {
        pager.status == InitializeStatus.INIT -> items(MomentSkeletonCount, key = { "moment_skeleton_$it" }) {
            MomentRow { MomentSkeleton(it) }
        }
        pager.status == InitializeStatus.FAILED -> item(key = "moments_state") {
            MomentListState(pager)
        }
        pager.items.isEmpty() -> item(key = "moments_empty") {
            EmptyLine(empty)
        }
        else -> {
            items(pager.items, key = { "moment_${it.id}" }) { item ->
                MomentRow { MomentCard(item, actions) }
            }
            item(key = "moments_more") { MoreLine(pager) }
        }
    }
}

/**
 * [momentItems] for a grid, each over its whole row.
 *
 * @param bleed How far each moment reaches into the grid's side padding, so its text lines up with
 * the rest of the page while its ripple and its line run past. @since 261110
 */
fun LazyGridScope.momentItems(pager: MomentPager, actions: MomentActions, empty: String? = null, bleed: Dp = 0.dp) {
    val full: (androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope) -> GridItemSpan = { GridItemSpan(it.maxLineSpan) }
    when {
        pager.status == InitializeStatus.INIT -> items(MomentSkeletonCount, key = { "moment_skeleton_$it" }, span = { full(this) }) {
            MomentRow(Modifier.bleed(bleed)) {
                MomentSkeleton(it, contentPadding = PaddingValues(start = bleed, end = bleed, top = 12.dp, bottom = 4.dp))
            }
        }
        pager.status == InitializeStatus.FAILED -> item(key = "moments_state", span = full) {
            MomentListState(pager)
        }
        pager.items.isEmpty() -> item(key = "moments_empty", span = full) {
            EmptyLine(empty)
        }
        else -> {
            pager.items.forEach { item ->
                item(key = "moment_${item.id}", span = full) {
                    MomentRow(Modifier.bleed(bleed)) {
                        MomentCard(
                            item, actions,
                            contentPadding = PaddingValues(start = bleed, end = bleed, top = 12.dp, bottom = 4.dp),
                        )
                    }
                }
            }
            item(key = "moments_more", span = full) { MoreLine(pager) }
        }
    }
}

/** Wider than its place by [horizontal] on each side. */
private fun Modifier.bleed(horizontal: Dp): Modifier = if (horizontal == 0.dp) this else layout { measurable, constraints ->
    val extra = horizontal.roundToPx() * 2
    val placeable = measurable.measure(
        constraints.copy(minWidth = constraints.minWidth + extra, maxWidth = constraints.maxWidth + extra)
    )
    layout(placeable.width - extra, placeable.height) { placeable.place(-extra / 2, 0) }
}

/** Loads the next page when the end of the list comes into view. */
@Composable
fun LoadMoreMomentsEffect(state: LazyListState, pager: MomentPager) {
    val nearEnd by remember(state) {
        derivedStateOf {
            val last = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            last >= state.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(nearEnd, pager.items.size) {
        if (nearEnd) pager.loadMore()
    }
}

/** [LoadMoreMomentsEffect] for a grid. */
@Composable
fun LoadMoreMomentsEffect(state: LazyGridState, pager: MomentPager, enabled: Boolean = true) {
    val nearEnd by remember(state) {
        derivedStateOf {
            val last = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            last >= state.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(nearEnd, pager.items.size, enabled) {
        if (nearEnd && enabled) pager.loadMore()
    }
}

@Composable
private fun MomentRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier.fillMaxWidth()) {
        content()
        Box(Modifier.fillMaxWidth().height(1.dp).background(HachimiTheme.colorScheme.outline))
    }
}

@Composable
private fun MomentListState(pager: MomentPager) {
    Box(Modifier.fillMaxWidth().padding(vertical = 64.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.moment_load_error), color = HachimiTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = pager::retry) { Text(stringResource(Res.string.notification_retry)) }
        }
    }
}

@Composable
private fun EmptyLine(text: String?) {
    Box(Modifier.fillMaxWidth().padding(vertical = 64.dp), contentAlignment = Alignment.Center) {
        Text(text ?: stringResource(Res.string.moment_empty), color = HachimiTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MoreLine(pager: MomentPager) {
    if (pager.loadMoreFailed) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(Res.string.moment_load_more_failed), fontSize = 13.sp, color = HachimiTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = pager::loadMore) { Text(stringResource(Res.string.notification_retry)) }
        }
    } else {
        LoadMoreItem(hasMore = pager.hasMore, isLoading = pager.loadingMore)
    }
}
