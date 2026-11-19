package world.hachimi.app.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a Baseline Profile covering startup + critical UI journeys.
 *
 * Journeys use **UI Automator 2.4 DSL** (`uiAutomator` / `onElement*`) and, over the phone
 * layout, walk the bottom bar's three tabs: home feed, the square, the Me page.
 *
 * Run (connected device, API 33+ or rooted API 28+):
 * ```
 * ./gradlew :androidApp:generateReleaseBaselineProfile
 * ```
 *
 * Tips:
 * - Physical device preferred; an emulator works for the method list too.
 * - Guest state is enough for these journeys; sign-in is only opened, never completed.
 * - Network so the feeds load (more code paths).
 * - Journeys only use `testTag` ids from `TestTags`; add a tag over matching on labels.
 *
 * @see <a href="https://d.android.com/training/testing/other-components/ui-automator">UI Automator 2.4</a>
 * @see <a href="https://d.android.com/topic/performance/baselineprofiles">Baseline Profiles</a>
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        val packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: error("targetAppId not passed as instrumentation runner arg")

        rule.collect(
            packageName = packageName,
            maxIterations = 3,
            includeInStartupProfile = true,
        ) {
            pressHome()
            startActivityAndWait()
            criticalUserJourneys()
        }
    }
}
