package com.useinsider.kotlindemo.benchmark

import android.app.Application
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.useinsider.insider.Insider
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MOB-28339 — the statistically valid half of "baseline belirlemek".
 *
 * <p>The load harness in `:example` answers "does the minified SDK survive repetition". It does
 * NOT produce a usable baseline: two back-to-back runs on one emulator measured 3153 and 4012
 * events/sec, a ~27% spread with nothing changed, so any threshold derived from it would flake.
 * That spread is what hand-timing a loop in a debuggable app looks like — no warmup, no clock
 * control, no distribution, one number.
 *
 * <p>This module exists to replace that number with one you can actually gate on.
 * [BenchmarkRule] warms up until the measurement stabilises, runs many iterations, and reports
 * min/median/max plus the allocation count — and it refuses to run at all against a debuggable
 * build, which is why this module sets `testBuildType = "release"`.
 *
 * <p>Read the results from `benchmark/build/outputs/connected_android_test_additional_output/`,
 * where the runner writes a JSON report. That JSON is the baseline artefact; the harness's logcat
 * line never was one.
 *
 * <p><b>A physical device is required. This is measured, not assumed.</b> Two runs on
 * `emulator-5554` (Pixel 7a AVD, Android 16):
 *
 * <ul>
 *   <li>Default config: both benchmarks fail with `ERRORS (not suppressed): EMULATOR`. The runner
 *       refuses to emit a number it cannot stand behind. This is correct behaviour and the module
 *       is deliberately left in that state — do NOT add a blanket suppression.</li>
 *   <li>Forced with `-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR`:
 *       the run reaches instrumentation and then dies with `OutOfMemoryError` after ~5 minutes, so
 *       no JSON is produced either way. Whether that OOM is an emulator heap limit or something in
 *       the SDK's per-event retention is UNRESOLVED — it cannot be attributed from an environment
 *       the tool already declared unmeasurable. Settle it on a device before drawing a conclusion.</li>
 * </ul>
 *
 * <p>The contrast with the hand-written harness is the point of this module: on that same emulator
 * the harness happily reported 3153-4012 events/sec. It had no way to know the environment could
 * not support the measurement. This one does.
 *
 * <p><b>Not a correctness test.</b> Nothing here asserts SDK behaviour beyond the SDK resolving
 * and initialising — [com.useinsider.kotlindemo.InsiderMinifiedLoadHarnessTest] owns that. If this
 * class ever starts asserting outcomes, the measurement stops being a measurement.
 */
@RunWith(AndroidJUnit4::class)
class InsiderEventBenchmark {

    private companion object {
        const val EVENT_NAME = "mob28339_benchmark_probe"
        const val PARAMETER_KEY = "idx"
    }

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Before
    fun setUp() {
        val application = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext as Application

        // The benchmark module has no ExampleApplication, so initialise here. init is idempotent
        // across the class; measuring it is a separate benchmark below.
        Insider.Instance.init(application, BuildConfig.INSIDER_PARTNER_NAME)

        assertNotNull("Insider.Instance must resolve from the minified AAR", Insider.Instance)
    }

    /**
     * The operation the load harness loops 10,000 times, measured properly.
     *
     * <p>Comparable across runs in a way `events/sec` from a wall-clock loop is not: same warmup,
     * same iteration policy, and the reported spread is the measurement's own, not the platform's.
     */
    @Test
    fun tagEventBuild() {
        benchmarkRule.measureRepeated {
            val event = Insider.Instance.tagEvent(EVENT_NAME)
            event.addParameterWithString(PARAMETER_KEY, "0").build()
        }
    }

    /**
     * Builder construction alone, so a regression can be attributed.
     *
     * <p>Without this, a slowdown in [tagEventBuild] is ambiguous between "constructing the event
     * got slower" and "dispatching it got slower".
     */
    @Test
    fun tagEventWithoutBuild() {
        benchmarkRule.measureRepeated {
            Insider.Instance.tagEvent(EVENT_NAME)
        }
    }
}
