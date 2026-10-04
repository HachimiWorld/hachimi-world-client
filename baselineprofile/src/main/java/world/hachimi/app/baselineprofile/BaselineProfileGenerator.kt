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
 * Journeys use **UI Automator 2.4 DSL** (`uiAutomator` / `onElement*`).
 *
 * Run (connected device, API 33+ or rooted API 28+):
 * ```
 * ./gradlew :androidApp:generateReleaseBaselineProfile
 * ```
 *
 * Tips:
 * - Physical device; zh or en locale (labels try both).
 * - Network so home feed loads (more code paths).
 * - Prefer Compose `testTag` + `testTagsAsResourceId` later → `viewIdResourceName` predicates.
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
