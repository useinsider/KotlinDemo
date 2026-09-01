package com.useinsider.kotlindemo.benchmark

import android.app.Application
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.useinsider.insider.Insider
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Ignore
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
 * <p><b>Measured, and it changed the design.</b> On `emulator-5554` (Pixel 7a AVD, Android 16):
 *
 * <ul>
 *   <li>Default config: both benchmarks fail with `ERRORS (not suppressed): EMULATOR`. The runner
 *       refuses to emit a number it cannot stand behind. Correct behaviour — the module is left in
 *       that strict state on purpose, so do NOT add a blanket suppression.</li>
 *   <li>Forced with `-Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR`:
 *       the dispatching benchmark reached `Warmup: t=8.170, iter=23793`, then died with
 *       `OutOfMemoryError` at `target footprint 201326592`. No JSON was produced.</li>
 * </ul>
 *
 * <p>An earlier version of this file blamed that OOM on the emulator and told the next engineer to
 * "settle it on a device". <b>That was wrong, and the run artefacts refute it.</b> Warmup is
 * TIME-boxed, not iteration-boxed: a faster physical device executes MORE iterations inside the
 * same 8.17 s window, retains MORE per-event state, and reaches OOM sooner rather than later. The
 * device is not the fix.
 *
 * <p>The distribution was already unusable before the OOM. Against a body of 50-120 us the run
 * recorded `timeNs[10:20]: ... 1038981500` (1.04 s) and `timeNs[30:40]: ... 6031167` (6 ms) — GC
 * pauses for the accumulating state. min/median/max therefore move with iteration count, which is
 * precisely the un-gateable number this module was created to replace.
 *
 * <p><b>Consequence: dispatching is not benchmarkable in steady state today.</b>
 * `tagEvent(...).build()` retains per-event state that the SDK releases only at session stop, and
 * there is no public reset hook to call from `runWithTimingDisabled`. That is a finding about the
 * SDK's testability, not an emulator artefact, so [tagEventBuild] is `@Ignore`d with that reason
 * rather than deleted or left to fail. [tagEventWithoutBuild] measures the allocation-only path,
 * which IS steady-state, and is the benchmark to run.
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
        const val PLACEHOLDER_PARTNER = "partnername"
    }

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Before
    fun setUp() {
        val application = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext as Application

        // Fail fast rather than silently measuring the placeholder path. The deliverable here IS
        // the number, so a run against a non-existent partner would produce a baseline describing
        // whatever a degraded init leaves behind.
        assertNotEquals(
            "pass -PinsiderPartnerName=<partner>; the placeholder measures a degraded init path",
            PLACEHOLDER_PARTNER,
            BuildConfig.PARTNER_NAME
        )

        // On the main thread, the way ExampleApplication and real integrators do it. @Before runs
        // on the instrumentation thread, and SDK init registers process-lifecycle observers that
        // expect the main looper; the SDK's defensive catches would swallow the difference and
        // leave a partially initialised SDK for the benchmark to measure.
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            Insider.Instance.init(application, BuildConfig.PARTNER_NAME)
        }

        assertNotNull("Insider.Instance must resolve from the minified AAR", Insider.Instance)
    }

    /**
     * Disabled, with evidence: this cannot produce a steady-state measurement today.
     *
     * <p>See the class KDoc. `build()` retains state released only at session stop, warmup is
     * time-boxed, and the recorded distribution was dominated by GC pauses before the run OOMed.
     * Re-enable once the SDK exposes a reset hook that can be called under
     * `runWithTimingDisabled` — at that point this becomes the primary benchmark.
     */
    @Ignore("Retains per-event state with no public reset; OOMs and GC-contaminates the distribution")
    @Test
    fun tagEventBuild() {
        benchmarkRule.measureRepeated {
            val event = Insider.Instance.tagEvent(EVENT_NAME)
            event.addParameterWithString(PARAMETER_KEY, "0").build()
        }
    }

    /**
     * Builder construction alone — the one path that is steady-state, and therefore the benchmark
     * that actually yields a baseline.
     *
     * <p>Allocates and discards; nothing is retained across iterations, so the reported spread is
     * the measurement's own rather than the collector's.
     */
    @Test
    fun tagEventWithoutBuild() {
        benchmarkRule.measureRepeated {
            Insider.Instance.tagEvent(EVENT_NAME)
        }
    }
}
