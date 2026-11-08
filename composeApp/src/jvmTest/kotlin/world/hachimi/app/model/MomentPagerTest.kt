package world.hachimi.app.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import world.hachimi.app.api.module.MomentModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock

class MomentPagerTest {
    private fun moment(id: Long, uid: Long = 1) = MomentModule.MomentItem(
        id = id,
        author = MomentModule.MomentUser(uid, "u$uid"),
        content = "moment $id",
        images = emptyList(),
        attachments = emptyList(),
        likeCount = 0,
        liked = false,
        commentCount = 0,
        isHidden = false,
        createTime = Clock.System.now(),
    )

    /** Pages of two, newest first, from 5 down to 1. */
    private val pages = mapOf(
        null to MomentPage(listOf(moment(5), moment(4)), "a"),
        "a" to MomentPage(listOf(moment(4), moment(3), moment(2)), "b"),
        "b" to MomentPage(listOf(moment(1)), null),
    )

    private suspend fun until(condition: () -> Boolean) = withTimeout(5_000) {
        while (!condition()) delay(5)
    }

    @Test
    fun pagesUntilTheEndAndFollowsChanges() = runBlocking {
        val scope = CoroutineScope(Dispatchers.Default)
        val changes = MutableSharedFlow<MomentChange>(extraBufferCapacity = 8)
        val loaded = mutableListOf<String?>()
        val pager = MomentPager(scope, changes, alert = {}, acceptsPosted = { it.author.uid == 1L }) { cursor ->
            loaded += cursor
            pages.getValue(cursor)
        }
        try {
            pager.ensureLoaded()
            until { pager.status == InitializeStatus.LOADED }
            assertEquals(listOf(5L, 4L), pager.items.map { it.id })
            assertTrue(pager.hasMore)

            pager.loadMore()
            until { !pager.loadingMore && pager.items.size > 2 }
            // A moment seen on the page before isn't listed twice
            assertEquals(listOf(5L, 4L, 3L, 2L), pager.items.map { it.id })
            pager.loadMore()
            until { !pager.hasMore }
            assertEquals(listOf(5L, 4L, 3L, 2L, 1L), pager.items.map { it.id })
            pager.loadMore()
            assertEquals(listOf(null, "a", "b"), loaded)

            changes.emit(MomentChange.Updated(moment(3).copy(liked = true, likeCount = 1)))
            until { pager.items.first { it.id == 3L }.liked }
            changes.emit(MomentChange.Deleted(4))
            until { pager.items.none { it.id == 4L } }
            changes.emit(MomentChange.Posted(moment(6)))
            changes.emit(MomentChange.Posted(moment(7, uid = 2)))
            until { pager.items.first().id == 6L }
            delay(50)
            assertFalse(pager.items.any { it.id == 7L }, "only the moments this list is for")
            assertEquals(listOf(6L, 5L, 3L, 2L, 1L), pager.items.map { it.id })
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun failedFirstLoadCanBeRetried() = runBlocking {
        val scope = CoroutineScope(Dispatchers.Default)
        var fail = true
        val pager = MomentPager(scope, MutableSharedFlow(), alert = {}) {
            if (fail) throw MomentLoadException("down")
            MomentPage(listOf(moment(1)), null)
        }
        try {
            pager.ensureLoaded()
            until { pager.status == InitializeStatus.FAILED }
            fail = false
            pager.retry()
            until { pager.status == InitializeStatus.LOADED }
            assertEquals(listOf(1L), pager.items.map { it.id })
            assertFalse(pager.hasMore)
        } finally {
            scope.cancel()
        }
    }
}
