package world.hachimi.app.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TokenRefreshTest {
    private val serverNow = Instant.fromEpochSeconds(1_800_000_000)
    // Access tokens live for 5 minutes
    private val exp = serverNow.epochSeconds + 300

    @Test
    fun refreshesShortlyBeforeExpiry() {
        assertFalse(shouldRefreshAccessToken(exp, serverNow + 269.seconds, 0))
        assertTrue(shouldRefreshAccessToken(exp, serverNow + 270.seconds, 0))
        assertTrue(shouldRefreshAccessToken(exp, serverNow + 400.seconds, 0))
    }

    @Test
    fun localClockBehindServer() {
        // The local clock runs 2 minutes behind. Judged by local time alone, the token would look valid
        // until 2 minutes after the server has started rejecting it.
        val localNow = serverNow + 280.seconds - 120.seconds
        assertFalse(shouldRefreshAccessToken(exp, localNow, 0))
        assertTrue(shouldRefreshAccessToken(exp, localNow, 120))
    }

    @Test
    fun localClockAheadOfServer() {
        // The local clock runs 10 minutes ahead: without the offset every token would look expired at once
        val localNow = serverNow + 600.seconds
        assertTrue(shouldRefreshAccessToken(exp, localNow, 0))
        assertFalse(shouldRefreshAccessToken(exp, localNow, -600))
    }

    @Test
    fun offsetFromFreshToken() {
        assertEquals(120, clockOffsetSeconds(serverNow.epochSeconds, serverNow - 120.seconds))
        assertEquals(-600, clockOffsetSeconds(serverNow.epochSeconds, serverNow + 600.seconds))
        assertEquals(0, clockOffsetSeconds(serverNow.epochSeconds, serverNow))
    }
}
