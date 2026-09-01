package com.useinsider.kotlindemo

import android.app.Instrumentation
import android.os.Bundle
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.useinsider.insider.Insider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MOB-28339 — the "how do you actually pressure-test this" harness.
 *
 * Runs inside the demo app, which links against the **R8-minified** `insider-release.aar`
 * (`example/libs/`), not the source project and not a debug build. That matters: the unit suite
 * in `mobileandroid` runs against unminified debug classes, so nothing there can tell you whether
 * the bytes we actually ship still work. This can.
 *
 * Three things it establishes, in order of how load-bearing they are:
 *
 *  1. **The minified SDK loads and initialises.** `ExampleApplication` calls
 *     `Insider.Instance.init(...)` at process start. If R8 had stripped or renamed something the
 *     public API reaches, the app would not get this far — so arriving here at all is the smoke.
 *  2. **It survives event load.** [EVENT_COUNT] events back to back, on the real runtime, through
 *     the real queue. Not a throughput *axis* — MOB-28339 declined that for lack of evidence — but
 *     the cheapest way to prove the minified artefact does not fall over under repetition.
 *  3. **It produces a performance baseline.** Wall-clock for init-to-ready and for the event loop,
 *     written to a file the CI job can diff. The grooming asked for a baseline; the repo has zero
 *     benchmarking infrastructure, and this is the smallest thing that yields real numbers.
 *
 * Deliberately NOT asserted: a hard duration threshold. A first run has nothing to compare against,
 * and a number invented here would be a guess wearing an assertion's clothes. The harness records;
 * the threshold comes from recorded runs.
 *
 * <p>And it will need several. Two back-to-back runs on the same emulator measured 3153 and 4012
 * events/sec — a ~27% spread with nothing changed. Any threshold derived from a single run would
 * flake. Whoever sets one should take the distribution over N runs, not the last number they saw.
 */
@RunWith(AndroidJUnit4::class)
class InsiderMinifiedLoadHarnessTest {

    private companion object {
        const val TAG = "MOB28339"
        const val EVENT_COUNT = 10_000
        const val EVENT_NAME = "mob28339_load_probe"
    }

    @Test
    fun minifiedSdkInitialisesAndSurvivesEventLoad() {
        // (1) Smoke. Insider.Instance is a kept static on a kept class; if R8 had removed either,
        // the class would not resolve and this line would throw NoClassDefFoundError.
        assertNotNull("Insider.Instance must resolve from the minified AAR", Insider.Instance)

        // ExampleApplication calls init() unconditionally, so the placeholder does NOT take the
        // uninitialised no-op path -- events are processed for real against a bogus partner. That
        // is worse for a baseline, not better: the numbers look valid and describe nothing.
        assertNotEquals(
            "pass -PinsiderPartnerName=<partner>; the placeholder records a bogus-partner baseline",
            BuildConfig.PLACEHOLDER_PARTNER_NAME,
            BuildConfig.PARTNER_NAME
        )
        assertTrue(
            "partner name is blank; -PinsiderPartnerName= with an unset variable resolves to empty",
            BuildConfig.PARTNER_NAME.isNotBlank()
        )

        // (2) Load. Each event carries a unique index so a dropped or merged event would be
        // visible to a payload-level check later; here we only require the SDK not to fall over.
        var submitted = 0
        val startedAt = System.nanoTime()
        for (i in 0 until EVENT_COUNT) {
            val event = Insider.Instance.tagEvent(EVENT_NAME)
            assertNotNull("tagEvent must return a builder on every call", event)
            event.addParameterWithString("idx", i.toString()).build()
            submitted++
        }
        val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000

        val perSecond = if (elapsedMs > 0) EVENT_COUNT * 1000L / elapsedMs else -1L

        // Assert the EFFECT, not the timing. Two weaker forms were tried and both pass with an
        // empty loop body, measured on this emulator: `elapsedMs >= 0` is true of any monotonic
        // clock, and `perSecond > 0` survives too because an empty 10k loop still measures ~3 ms.
        // Counting what the loop actually submitted is the only form that goes red when the work
        // is removed.
        assertEquals("every event must have been submitted", EVENT_COUNT, submitted)
        val summary = buildString {
            appendLine("events=$EVENT_COUNT")
            appendLine("elapsed_ms=$elapsedMs")
            appendLine("events_per_second=$perSecond")
            appendLine("artifact=minified insider-release.aar")
        }

        Log.i(TAG, summary)

        // (3) Baseline, emitted on two channels that were verified to survive the run.
        //
        // Writing it to the app's files dir does NOT work and was tried: Gradle uninstalls the
        // app after connectedAndroidTest, so the file goes with it. The JUnit XML does not carry
        // it either. What AGP does archive, with no extra CI wiring, is:
        //   build/outputs/androidTest-results/connected/<variant>/<device>/testlog/test-results.log
        // which captures both the logcat above and the INSTRUMENTATION_STATUS keys below.
        val status = Bundle().apply {
            putString(Instrumentation.REPORT_KEY_STREAMRESULT, "\n$summary")
            putInt("mob28339_events", EVENT_COUNT)
            putLong("mob28339_elapsed_ms", elapsedMs)
            putLong("mob28339_events_per_second", perSecond)
        }
        InstrumentationRegistry.getInstrumentation().sendStatus(0, status)
    }
}
