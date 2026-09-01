package com.useinsider.kotlindemo.benchmark

import android.app.Application
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.useinsider.insider.Insider
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Microbenchmarks for the R8-minified SDK, using `androidx.benchmark`.
 *
 * Unlike the load harness in `:example`, which hand-times a loop, [BenchmarkRule] warms up, runs
 * many iterations and reports a distribution — so this is the number worth gating on. The module
 * sets `testBuildType = "release"` because the runner refuses to measure a debuggable build.
 *
 *     ./gradlew :benchmark:connectedReleaseAndroidTest -PuseLocalInsiderAar -PinsiderPartnerName=<partner>
 *
 * Results land in `benchmark/build/outputs/connected_android_test_additional_output/` as JSON.
 *
 * **Run this on a physical device.** On an emulator the runner refuses to report without
 * `androidx.benchmark.suppressErrors=EMULATOR`, and the figure it then produces is far noisier
 * (measured: CoV 0.83 on an emulator versus 0.29 on a device). Do not add a blanket suppression —
 * that strictness is the point.
 *
 * If a run dies in `androidx.benchmark.ShellImpl` with `UiAutomationService ... already
 * registered`, a stale UiAutomation registration is holding the slot; reboot the device. Check
 * `/proc/uptime` afterwards, since a vendor "restart" may leave the kernel running.
 */
@RunWith(AndroidJUnit4::class)
class InsiderEventBenchmark {

    private companion object {
        const val EVENT_NAME = "insider_benchmark_probe"
        const val PARAMETER_KEY = "idx"
    }

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Before
    fun setUp() {
        val application = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext as Application

        // The deliverable here IS the number, so fail rather than benchmark a degraded init.
        assertNotEquals(
            "pass -PinsiderPartnerName=<partner>; the placeholder measures a degraded init path",
            BuildConfig.PLACEHOLDER_PARTNER_NAME,
            BuildConfig.PARTNER_NAME
        )
        assertTrue(
            "partner name is blank; -PinsiderPartnerName= with an unset variable resolves to empty",
            BuildConfig.PARTNER_NAME.isNotBlank()
        )

        // Init on the main thread, as integrators do: @Before runs on the instrumentation thread,
        // and the SDK's defensive catches would otherwise leave a partially initialised SDK to
        // measure.
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            Insider.Instance.init(application, BuildConfig.PARTNER_NAME)
        }

        assertNotNull("Insider.Instance must resolve from the minified AAR", Insider.Instance)
    }

    /**
     * Event dispatch. Disabled: state accumulates across iterations with no reset hook callable
     * from a benchmark, so the run never reaches steady state and the distribution reflects
     * collection pauses rather than the call. Re-enable when a reset can be called under
     * `runWithTimingDisabled`; this then becomes the primary benchmark.
     */
    @Ignore("Accumulates state across iterations with no reset hook; never reaches steady state")
    @Test
    fun tagEventBuild() {
        benchmarkRule.measureRepeated {
            val event = Insider.Instance.tagEvent(EVENT_NAME)
            event.addParameterWithString(PARAMETER_KEY, "0").build()
        }
    }

    /** Builder construction — allocates and discards, so it is steady-state and measurable. */
    @Test
    fun tagEventWithoutBuild() {
        benchmarkRule.measureRepeated {
            Insider.Instance.tagEvent(EVENT_NAME)
        }
    }
}
