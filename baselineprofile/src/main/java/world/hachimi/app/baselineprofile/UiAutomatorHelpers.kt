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
    const val NAV_MENU = "nav_menu"
    const val NAV_HOME = "nav_home"
    const val NAV_EVENTS = "nav_events"
    const val NAV_MY_FOLLOWS = "nav_my_follows"
    const val NAV_SETTINGS = "nav_settings"

    const val HOME_FEED = "home_feed"
    const val HOME_RECENT_MORE = "home_recent_more"
    const val HOME_WEEKLY_MORE = "home_weekly_more"

    const val SONG_CARD = "song_card"

    const val MINI_PLAYER = "mini_player"
    const val PLAYER_SCREEN = "player_screen"
    const val PLAYER_SHRINK = "player_shrink"

    const val SETTINGS_SCREEN = "settings_screen"
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

    fun visibleTag(tag: String, timeoutMs: Long = 400): Boolean =
        findTag(tag, timeoutMs) != null

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

    fun scrollDown(times: Int = 1) {
        if (!ensureInApp("scrollDown")) return
        val w = device.displayWidth
        val h = device.displayHeight
        // Stay away from top status-bar gesture zone and bottom nav/home indicator
        val y0 = (h * 0.68f).toInt()
        val y1 = (h * 0.38f).toInt()
        repeat(times) {
            if (!inApp()) {
                bpLog("abort scrollDown — left app")
                return
            }
            device.swipe(w / 2, y0, w / 2, y1, 18)
            briefSettle(100)
        }
    }

    fun scrollUp(times: Int = 1) {
        if (!ensureInApp("scrollUp")) return
        val w = device.displayWidth
        val h = device.displayHeight
        val y0 = (h * 0.38f).toInt()
        val y1 = (h * 0.68f).toInt()
        repeat(times) {
            if (!inApp()) {
                bpLog("abort scrollUp — left app")
                return
            }
            device.swipe(w / 2, y0, w / 2, y1, 18)
            briefSettle(100)
        }
    }

    /**
     * Safe back: only via tagged in-app chrome. Never blind [pressBack] —
     * that exits to the launcher when the nav stack is empty.
     */
    fun leaveOverlay(prefer: String? = null): Boolean {
        if (!inApp()) return false
        when (prefer) {
            Tags.AUTH_SCREEN, Tags.AUTH_BACK -> {
                if (clickTag(Tags.AUTH_BACK, 1_000)) return true
            }
            Tags.PLAYER_SCREEN, Tags.PLAYER_SHRINK -> {
                if (clickTag(Tags.PLAYER_SHRINK, 1_000)) return true
            }
            Tags.SETTINGS_SCREEN -> {
                // Settings uses scaffold back; tag may only be on content.
                // Prefer drawer → home over system Back.
            }
        }
        // Generic: auth / player shrink buttons if present
        if (visibleTag(Tags.AUTH_BACK, 200) && clickTag(Tags.AUTH_BACK, 600)) return true
        if (visibleTag(Tags.PLAYER_SHRINK, 200) && clickTag(Tags.PLAYER_SHRINK, 600)) return true
        return false
    }

    fun openDrawer(): Boolean {
        if (!ensureInApp("openDrawer")) return false
        if (clickTag(Tags.NAV_MENU, 1_200)) {
            briefSettle(200)
            return visibleTag(Tags.NAV_EVENTS, 800) || visibleTag(Tags.NAV_HOME, 400)
        }
        bpLog("nav_menu miss — no coordinate fallback (would hit launcher)")
        return false
    }

    fun waitHome(timeoutMs: Long = 12_000) {
        timed("waitHome") {
            ensureInApp("waitHome")
            findTag(Tags.HOME_FEED, timeoutMs)
            briefSettle(80)
        }
    }

    fun navPrimary(tag: String): Boolean =
        timed("nav($tag)") {
            if (!ensureInApp("nav($tag)")) return@timed false
            // Tag only — never label-match "设置" on the launcher
            if (clickTag(tag, 600)) return@timed true
            if (!openDrawer()) return@timed false
            clickTag(tag, 2_000)
        }

    fun goHome() {
        timed("goHome") {
            if (!ensureInApp("goHome")) return@timed
            if (visibleTag(Tags.HOME_FEED, 350)) {
                briefSettle(60)
                return@timed
            }
            leaveOverlay(Tags.AUTH_SCREEN)
            leaveOverlay(Tags.PLAYER_SCREEN)
            // Leave settings / secondary by navigating Home via drawer — no pressBack
            if (navPrimary(Tags.NAV_HOME)) {
                findTag(Tags.HOME_FEED, 2_500)
            } else {
                bpLog("goHome: nav_home failed, relaunch")
                relaunch()
                findTag(Tags.HOME_FEED, 5_000)
            }
            briefSettle(80)
        }
    }

    /**
     * Secondary list from home segment "更多".
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
            // Only scroll secondary if still in app and left the home feed
            if (inApp() && !visibleTag(Tags.HOME_FEED, 200)) {
                scrollDown(1)
                // Secondary screens use scaffold back via system Back — only if feed gone
                if (!visibleTag(Tags.HOME_FEED, 150)) {
                    safeBackFromSecondary()
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

    /**
     * One guarded Back for secondary routes only.
     * If still no home feed after Back and we left the package → relaunch.
     */
    private fun safeBackFromSecondary() {
        if (!inApp()) {
            ensureInApp("safeBack/already-out")
            return
        }
        bpLog("secondary Back (guarded)")
        scope.pressBack()
        briefSettle(280)
        if (!inApp()) {
            bpLog("Back left app → relaunch")
            ensureInApp("safeBack/after-back")
            return
        }
        // If Back did nothing useful, drawer home
        if (!visibleTag(Tags.HOME_FEED, 500)) {
            navPrimary(Tags.NAV_HOME)
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
            if (!visibleTag(Tags.PLAYER_SCREEN, 3_000)) {
                bpLog("player_screen not shown ($reason)")
                return@timed
            }
            briefSettle(400)
            if (!clickTag(Tags.PLAYER_SHRINK, 2_000)) {
                leaveOverlay(Tags.PLAYER_SHRINK)
            }
            briefSettle(300)
            findTag(Tags.MINI_PLAYER, 2_000)
        }
    }

    fun clickAvatar(): Boolean {
        if (!ensureInApp("avatar")) return false
        if (clickTag(Tags.PROFILE_AVATAR, 2_500)) return true
        bpLog("avatar tag miss — no coord fallback (hits status bar / launcher)")
        return false
    }
}

/**
 * Redesigned CUJ for Baseline Profile.
 *
 * Safety rules:
 * - **testTag only** (no "设置"/"更多" label match → system Settings / launcher)
 * - **ensureInApp** before scroll/click; relaunch if package is not ours
 * - **no coordinate fallbacks** (status bar / dock / home icons)
 * - **no blind pressBack** except one guarded secondary Back
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

        // ── 1. Startup / home ─────────────────────────────────────────
        journey.waitHome()
        timed("scroll home") {
            journey.scrollDown(3)
            journey.scrollUp(1)
        }
        journey.openHomeMoreAndBack(Tags.HOME_RECENT_MORE, preferTop = true)
        journey.openHomeMoreAndBack(Tags.HOME_WEEKLY_MORE, preferTop = false)
        journey.goHome()

        // ── 2. Primary nav (guest-usable; tag-only) ───────────────────
        timed("nav events") {
            if (journey.navPrimary(Tags.NAV_EVENTS)) {
                briefSettle(250)
                journey.scrollDown(1)
            }
            journey.goHome()
        }
        timed("nav follows") {
            if (journey.navPrimary(Tags.NAV_MY_FOLLOWS)) {
                briefSettle(300)
            }
            journey.goHome()
        }
        timed("nav settings") {
            if (journey.navPrimary(Tags.NAV_SETTINGS)) {
                if (journey.visibleTag(Tags.SETTINGS_SCREEN, 1_500)) {
                    journey.scrollDown(1)
                    // Prefer nav home over Back (Back may finish root)
                    journey.navPrimary(Tags.NAV_HOME)
                }
            }
            journey.goHome()
        }

        // ── 3. Auth transition ────────────────────────────────────────
        timed("auth transition") {
            journey.goHome()
            if (!journey.clickAvatar()) {
                bpLog("avatar miss")
                return@timed
            }
            briefSettle(450)
            if (journey.visibleTag(Tags.AUTH_SCREEN, 3_000)) {
                briefSettle(350)
                if (!journey.clickTag(Tags.AUTH_BACK, 1_500)) {
                    journey.leaveOverlay(Tags.AUTH_BACK)
                }
            } else {
                bpLog("auth screen not shown")
            }
            journey.findTag(Tags.HOME_FEED, 2_500)
        }

        // ── 4. MiniPlayer ─────────────────────────────────────────────
        timed("player empty expand") {
            journey.goHome()
            journey.expandAndShrinkPlayer("empty")
        }

        timed("player 1 song") {
            journey.goHome()
            journey.scrollUp(1)
            if (journey.clickTag(Tags.SONG_CARD, 4_000)) {
                briefSettle(500)
                journey.expandAndShrinkPlayer("song1")
            } else {
                bpLog("no song_card for song1")
            }
        }

        timed("player 2 songs") {
            journey.goHome()
            journey.scrollUp(1)
            val cards = if (journey.ensureInApp("song2")) {
                onElements(timeoutMs = 2_500, pollIntervalMs = 50) {
                    hasTag(Tags.SONG_CARD)
                }
            } else {
                emptyList()
            }
            val target = cards.getOrNull(1) ?: cards.getOrNull(0)
            if (target != null) {
                try {
                    target.click()
                    briefSettle(500)
                    journey.expandAndShrinkPlayer("song2")
                } catch (_: StaleObjectException) {
                    journey.clickTag(Tags.SONG_CARD, 2_000)
                    briefSettle(500)
                    journey.expandAndShrinkPlayer("song2")
                }
            } else {
                bpLog("no song_card for song2")
            }
        }

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
