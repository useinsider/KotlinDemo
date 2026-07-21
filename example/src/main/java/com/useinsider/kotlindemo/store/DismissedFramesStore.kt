package com.useinsider.kotlindemo.store

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists the set of App Frames placement ids the user has dismissed, so a dismissed frame stays
 * hidden across app relaunches. Plain SharedPreferences — the dismissed set is non-sensitive.
 */
public class DismissedFramesStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    public fun dismissed(): Set<String> =
        // getStringSet's returned set must not be mutated — copy it defensively.
        prefs.getStringSet(KEY_DISMISSED, emptySet())?.toSet() ?: emptySet()

    public fun add(placementId: String): Unit {
        val updated = dismissed().toMutableSet().apply { add(placementId) }
        prefs.edit().putStringSet(KEY_DISMISSED, updated).apply()
    }

    public fun clear(): Unit {
        prefs.edit().remove(KEY_DISMISSED).apply()
    }

    private companion object {
        const val PREFS_NAME: String = "insider_app_frames"
        const val KEY_DISMISSED: String = "dismissed_placements"
    }
}
