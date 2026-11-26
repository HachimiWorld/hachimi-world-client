package world.hachimi.app.baselineprofile

import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiAutomatorTestScope
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.simpleViewResourceName
import androidx.test.uiautomator.uiAutomator

private const val LOG_TAG = "HachimiBp"

/** logcat: `adb logcat -s HachimiBp` */
private fun bpLog(msg: String) = Log.i(LOG_TAG, msg)

private inline fun <T> timed(step: String, block: () -> T): T {
    val t0 = SystemClock.elapsedRealtime()
    return try {
        block()
    } finally {
        bpLog("⏱ $step  ${SystemClock.elapsedRealtime() - t0}ms")
    }
}

/** Keep in sync with [world.hachimi.app.ui.TestTags]. */
internal object Tags {
    // The phone's bottom bar. The wide layout carries the same ids in its side navigation.
    const val NAV_HOME = "nav_home"
    /** The square tab; its tag keeps the name from when the square was the events page. */
    const val NAV_SQUARE = "nav_events"
    const val NAV_ME = "nav_me"

    const val HOME_FEED = "home_feed"
    const val HOME_RECENT_MORE = "home_recent_more"
    const val HOME_WEEKLY_MORE = "home_weekly_more"

    const val SQUARE_MOMENTS = "square_moments"
    const val SQUARE_ANNOUNCEMENTS = "square_announcements"

    const val SONG_CARD = "song_card"

    const val MINI_PLAYER = "mini_player"
    const val MINI_PLAYER_PLAY = "mini_player_play"
    const val MINI_PLAYER_NEXT = "mini_player_next"
    const val PLAYER_SCREEN = "player_screen"
    const val PLAYER_SHRINK = "player_shrink"

    const val NAV_SETTINGS = "nav_settings"
    const val NAV_MY_FOLLOWS = "nav_my_follows"
    const val SETTINGS_SCREEN = "settings_screen"
    const val ME_GUEST_LOGIN = "me_guest_login"
    const val PROFILE_AVATAR = "profile_avatar"

    const val AUTH_SCREEN = "auth_screen"
    const val AUTH_BACK = "auth_back"
}

// —— Predicates (tag-only; never match launcher/system labels like 设置) ——

private fun AccessibilityNodeInfo.hasTag(tag: String): Boolean {
    val simple = simpleViewResourceName()
    val full = viewIdResourceName
    return simple == tag || full == tag || full?.endsWith("/$tag") == true
}

// —— Core helpers ——

private fun briefSettle(ms: Long = 120) {
    if (ms > 0) SystemClock.sleep(ms)
}

/**
 * Journey context: target package + startActivity so we never drive the launcher.
 *
 * Critical: label matching ("设置"/"Settings") opens **system Settings** on the home screen.
 * All interaction is **testTag-only** + package guard.
 */
private class Journey(
    private val scope: UiAutomatorTestScope,
    private val packageName: String,
    private val relaunch: () -> Unit,
) {
    private val device get() = scope.device

    fun currentPkg(): String? = try {
        device.currentPackageName
    } catch (_: Exception) {
        null
    }

    fun inApp(): Boolean {
        val pkg = currentPkg() ?: return false
        return pkg == packageName || pkg.startsWith("$packageName.")
    }

    /** If we left the app (launcher / system settings), relaunch. */
    fun ensureInApp(reason: String): Boolean {
        if (inApp()) return true
        bpLog("⚠ left app (pkg=${currentPkg()}) @ $reason → relaunch")
        try {
            relaunch()
        } catch (e: Exception) {
            bpLog("relaunch failed: ${e.message}")
            return false
        }
        briefSettle(400)
        val ok = inApp()
        if (!ok) bpLog("relaunch still not in app pkg=${currentPkg()}")
        return ok
    }

    fun findTag(tag: String, timeoutMs: Long = 3_000): UiObject2? {
        if (!ensureInApp("findTag($tag)")) return null
        return scope.onElementOrNull(timeoutMs = timeoutMs, pollIntervalMs = 50) {
            hasTag(tag)
        }
    }

    fun awaitTag(tag: String, timeoutMs: Long = 4_000): Boolean {
        val found = findTag(tag, timeoutMs) != null
        if (!found) bpLog("wait miss tag=$tag t=${timeoutMs}ms pkg=${currentPkg()}")
        return found
    }

    fun visibleTag(tag: String, timeoutMs: Long = 400): Boolean = findTag(tag, timeoutMs) != null

    fun clickTag(tag: String, timeoutMs: Long = 3_000): Boolean {
        if (!ensureInApp("clickTag($tag)")) return false
        val node = findTag(tag, timeoutMs) ?: run {
            bpLog("click miss tag=$tag t=${timeoutMs}ms pkg=${currentPkg()}")
            return false
        }
        return try {
            // Prefer click even if a11y reports non-clickable (Compose merge quirks)
            node.click()
            briefSettle()
            true
        } catch (_: StaleObjectException) {
            val again = findTag(tag, 600) ?: return false
            try {
                again.click()
                briefSettle()
                true
            } catch (_: StaleObjectException) {
                false
            }
        }
    }

    fun scrollDown(times: Int = 1) = scroll(0.68f, 0.38f, times)

    fun scrollUp(times: Int = 1) = scroll(0.38f, 0.68f, times)

    /** Vertical drag over the content, clear of the status bar and the tab bar. */
    private fun scroll(fromFraction: Float, toFraction: Float, times: Int) {
        if (!ensureInApp("scroll")) return
        val w = device.displayWidth
        val h = device.displayHeight
        repeat(times) {
            if (!inApp()) {
                bpLog("abort scroll — left app")
                return
            }
            device.swipe(w / 2, (h * fromFraction).toInt(), w / 2, (h * toFraction).toInt(), 18)
            briefSettle(100)
        }
    }

    /** One guarded Back, for a page opened over the shell. */
    private fun pressBackGuarded(reason: String) {
        if (!inApp()) {
            ensureInApp(reason)
            return
        }
        bpLog("Back (guarded) @ $reason")
        scope.pressBack()
        briefSettle(300)
        if (!inApp()) {
            bpLog("Back left app @ $reason → relaunch")
            ensureInApp("$reason/after-back")
        }
    }

    /** Leaves a page that was opened over the shell and waits for what was under it. */
    fun backFromSecondary(expected: String) {
        pressBackGuarded("secondary")
        awaitTag(expected, 2_500)
    }

    /** Picks a bottom bar tab; the caller waits for what that tab shows. */
    fun openTab(tag: String): Boolean = timed("tab($tag)") {
        if (!ensureInApp("tab($tag)")) return@timed false
        // A page opened over the shell covers the bar; one guarded Back returns to it
        if (!visibleTag(tag, 250) && !visibleTag(Tags.NAV_HOME, 250)) {
            pressBackGuarded("tab($tag)")
        }
        clickTag(tag, 2_000)
    }

    fun goHome() {
        timed("goHome") {
            if (!ensureInApp("goHome")) return@timed
            if (visibleTag(Tags.HOME_FEED, 350)) {
                briefSettle(60)
                return@timed
            }
            openTab(Tags.NAV_HOME)
            if (!awaitTag(Tags.HOME_FEED, 5_000)) {
                bpLog("goHome: home feed never showed → relaunch")
                ensureInApp("goHome/relaunch")
                awaitTag(Tags.HOME_FEED, 6_000)
            }
            briefSettle(80)
        }
    }

    /**
     * Horizontal drag over the square, to the announcements tab or back.
     * Waits for [expected] and reports whether the tab changed.
     */
    fun swipeSquare(toNext: Boolean, expected: String): Boolean = timed("squareSwipe($expected)") {
        if (!ensureInApp("squareSwipe")) return@timed false
        val w = device.displayWidth
        val h = device.displayHeight
        val y = (h * 0.5f).toInt()
        val left = (w * 0.85f).toInt()
        val right = (w * 0.15f).toInt()
        repeat(2) {
            if (!inApp()) return@timed false
            if (toNext) device.swipe(left, y, right, y, 20) else device.swipe(right, y, left, y, 20)
            briefSettle(350)
            if (visibleTag(expected, 1_500)) return@timed true
        }
        bpLog("square swipe did not reach $expected")
        false
    }

    /**
     * Secondary list from a home segment's "更多".
     * Returns only after we're back on home feed (or abort). Never pressBack to launcher.
     */
    fun openHomeMoreAndBack(moreTag: String, preferTop: Boolean) {
        timed("homeMore($moreTag)") {
            if (!ensureInApp("homeMore($moreTag)")) return@timed
            if (preferTop) scrollUp(2) else scrollDown(2)

            if (!clickTag(moreTag, 2_000)) {
                if (preferTop) scrollDown(1) else scrollDown(2)
                if (!clickTag(moreTag, 2_000)) {
                    bpLog("more button miss $moreTag")
                    return@timed
                }
            }
            briefSettle(350)
            // Only scroll the secondary if it opened (home feed is covered by it)
            if (inApp() && !visibleTag(Tags.HOME_FEED, 200)) {
                scrollDown(1)
                // The tab bar is covered too; one guarded Back returns to the shell
                if (!visibleTag(Tags.NAV_HOME, 200)) {
                    pressBackGuarded("homeMore/$moreTag")
                }
            } else {
                bpLog("more click did not open secondary ($moreTag)")
            }
            // Always recover to home without leaving the package
            if (!visibleTag(Tags.HOME_FEED, 400)) {
                goHome()
            }
        }
    }

    fun expandAndShrinkPlayer(reason: String) {
        timed("playerExpandShrink($reason)") {
            if (!ensureInApp("player($reason)")) return@timed
            if (!clickTag(Tags.MINI_PLAYER, 2_000)) {
                bpLog("mini_player miss ($reason) — no coord fallback")
                return@timed
            }
            briefSettle(400)
            if (!awaitTag(Tags.PLAYER_SCREEN, 3_000)) {
                bpLog("player_screen not shown ($reason)")
                return@timed
            }
            briefSettle(400)
            if (!clickTag(Tags.PLAYER_SHRINK, 2_000)) {
                pressBackGuarded("player($reason)")
            }
            briefSettle(300)
            findTag(Tags.MINI_PLAYER, 2_000)
        }
    }

    /** The mini player's own controls, which sit above every root page. */
    fun tapMiniPlayerControls() {
        timed("miniPlayerControls") {
            if (!ensureInApp("miniControls")) return@timed
            if (!awaitTag(Tags.MINI_PLAYER, 2_000)) return@timed
            clickTag(Tags.MINI_PLAYER_PLAY, 1_500)
            briefSettle(200)
            clickTag(Tags.MINI_PLAYER_NEXT, 1_500)
            briefSettle(300)
        }
    }
}

/**
 * Critical user journeys for the Baseline Profile, over the phone layout:
 * home feed, the square (moments and announcements), the Me page with settings, the
 * sign-in page, and the player.
 *
 * Safety rules:
 * - **testTag only** (no "设置"/"更多" label match → system Settings / launcher)
 * - **ensureInApp** before scroll/click; relaunch if package is not ours
 * - **no coordinate fallbacks** (status bar / dock / home icons)
 * - **guarded pressBack only** for a page that was opened over the tab bar
 */
internal fun MacrobenchmarkScope.criticalUserJourneys() {
    val pkg = packageName
    uiAutomator {
        val journey = Journey(
            scope = this,
            packageName = pkg,
            relaunch = {
                // MacrobenchmarkScope.startActivityAndWait is on outer scope
                this@criticalUserJourneys.startActivityAndWait()
            },
        )
        val t0 = SystemClock.elapsedRealtime()
        bpLog("═══ journey start pkg=$pkg ═══")

        // ── 1. Startup / home feed ────────────────────────────────────
        journey.goHome()
        timed("scroll home") {
            journey.scrollDown(3)
            journey.scrollUp(1)
        }
        journey.openHomeMoreAndBack(Tags.HOME_RECENT_MORE, preferTop = true)
        journey.openHomeMoreAndBack(Tags.HOME_WEEKLY_MORE, preferTop = false)
        journey.goHome()

        // ── 2. The square: moments, then announcements ────────────────
        timed("square moments") {
            if (journey.openTab(Tags.NAV_SQUARE)) {
                journey.awaitTag(Tags.SQUARE_MOMENTS, 6_000)
                journey.scrollDown(2)
                journey.scrollUp(1)
            }
        }
        timed("square announcements") {
            if (journey.swipeSquare(toNext = true, expected = Tags.SQUARE_ANNOUNCEMENTS)) {
                journey.scrollDown(1)
                journey.swipeSquare(toNext = false, expected = Tags.SQUARE_MOMENTS)
            }
        }
        journey.goHome()

        // ── 3. Me: the page, settings, the follow list, sign-in ───────
        timed("me") {
            if (journey.openTab(Tags.NAV_ME)) {
                journey.awaitTag(Tags.NAV_SETTINGS, 4_000)
                journey.scrollDown(1)
                journey.scrollUp(1)
            }
        }
        timed("settings") {
            if (journey.clickTag(Tags.NAV_SETTINGS, 2_000)) {
                journey.awaitTag(Tags.SETTINGS_SCREEN, 4_000)
                journey.scrollDown(1)
                journey.backFromSecondary(expected = Tags.NAV_SETTINGS)
            }
        }
        timed("follow list") {
            if (journey.clickTag(Tags.NAV_MY_FOLLOWS, 2_000)) {
                briefSettle(600)
                // A guest gets the sign-in prompt there; either way it was a page of its own
                journey.backFromSecondary(expected = Tags.NAV_SETTINGS)
            }
        }
        timed("auth transition") {
            if (journey.clickTag(Tags.ME_GUEST_LOGIN, 2_000)) {
                if (journey.awaitTag(Tags.AUTH_SCREEN, 4_000)) {
                    briefSettle(350)
                    if (!journey.clickTag(Tags.AUTH_BACK, 2_000)) {
                        journey.backFromSecondary(expected = Tags.NAV_SETTINGS)
                    }
                } else {
                    bpLog("auth screen not shown")
                }
            }
            journey.awaitTag(Tags.NAV_SETTINGS, 3_000)
        }

        // ── 4. Player ─────────────────────────────────────────────────
        timed("player empty expand") {
            journey.goHome()
            journey.expandAndShrinkPlayer("empty")
        }
        timed("player with a song") {
            journey.goHome()
            journey.scrollUp(1)
            if (journey.clickTag(Tags.SONG_CARD, 6_000)) {
                briefSettle(600)
                journey.expandAndShrinkPlayer("song")
            } else {
                bpLog("no song_card on the home feed")
            }
        }
        journey.tapMiniPlayerControls()

        bpLog("═══ journey done total=${SystemClock.elapsedRealtime() - t0}ms pkg=${journey.currentPkg()} ═══")
    }
}

internal fun MacrobenchmarkScope.waitForHomeContent() {
    val pkg = packageName
    uiAutomator {
        timed("waitForHomeContent") {
            if (device.currentPackageName != pkg) {
                this@waitForHomeContent.startActivityAndWait()
            }
            onElementOrNull(timeoutMs = 12_000, pollIntervalMs = 50) {
                hasTag(Tags.HOME_FEED)
            }
            briefSettle(80)
        }
    }
}
