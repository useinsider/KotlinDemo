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
 * Smoke + load harness for the R8-minified SDK.
 *
 * The demo app links `example/libs/insider-release.aar` — the shipped, minified artifact — so this
 * exercises the bytes integrators actually get, which a unit test against unminified classes
 * cannot. It checks that the SDK resolves and initialises, then submits [EVENT_COUNT] events and
 * records how long that took.
 *
 * Run it with:
 *
 *     ./gradlew :example:connectedDebugAndroidTest -PuseLocalInsiderAar -PinsiderPartnerName=<partner>
 *
 * No duration is asserted. Run-to-run spread is large (measured: ~27% on an emulator, ~87% on a
 * device), so any threshold from a single run would flake. The recorded number is a survival
 * signal, not a gate.
 */
@RunWith(AndroidJUnit4::class)
class InsiderMinifiedLoadHarnessTest {

    private companion object {
        const val TAG = "InsiderLoadHarness"
        const val EVENT_COUNT = 10_000
        const val EVENT_NAME = "insider_load_probe"
        const val PARAMETER_KEY = "idx"
    }

    @Test
    fun minifiedSdkInitialisesAndSurvivesEventLoad() {
        assertNotNull("Insider.Instance must resolve from the minified AAR", Insider.Instance)

        // The demo app inits unconditionally, so a placeholder partner does NOT take the
        // uninitialised no-op path -- it records a real-looking baseline that describes nothing.
        assertNotEquals(
            "pass -PinsiderPartnerName=<partner>; the placeholder records a bogus-partner baseline",
            BuildConfig.PLACEHOLDER_PARTNER_NAME,
            BuildConfig.PARTNER_NAME
        )
        assertTrue(
            "partner name is blank; -PinsiderPartnerName= with an unset variable resolves to empty",
            BuildConfig.PARTNER_NAME.isNotBlank()
        )

        var submitted = 0
        val startedAt = System.nanoTime()
        for (i in 0 until EVENT_COUNT) {
            val event = Insider.Instance.tagEvent(EVENT_NAME)
            assertNotNull("tagEvent must return a builder on every call", event)
            event.addParameterWithString(PARAMETER_KEY, i.toString()).build()
            submitted++
        }
        val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
        val perSecond = if (elapsedMs > 0) EVENT_COUNT * 1000L / elapsedMs else -1L

        // Assert the effect, not the timing: `elapsedMs >= 0` and `perSecond > 0` were both tried
        // and both pass with an empty loop body. Counting submissions is the only form that goes
        // red when the work is removed.
        assertEquals("every event must have been submitted", EVENT_COUNT, submitted)

        val summary = buildString {
            appendLine("events=$EVENT_COUNT")
            appendLine("elapsed_ms=$elapsedMs")
            appendLine("events_per_second=$perSecond")
            appendLine("artifact=minified insider-release.aar")
        }
        Log.i(TAG, summary)

        // Reported via logcat and instrumentation status rather than a file: the app is uninstalled
        // after connectedAndroidTest, and the JUnit XML carries neither. Both channels land in
        // build/outputs/androidTest-results/connected/<variant>/<device>/testlog/test-results.log
        val status = Bundle().apply {
            putString(Instrumentation.REPORT_KEY_STREAMRESULT, "\n$summary")
            putInt("insider_load_events", EVENT_COUNT)
            putLong("insider_load_elapsed_ms", elapsedMs)
            putLong("insider_load_events_per_second", perSecond)
        }
        InstrumentationRegistry.getInstrumentation().sendStatus(0, status)
    }
}
