package com.useinsider.kotlindemo.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Observable state for one App Frames placement section. Each added placement gets its own section
 * with an independent status line and per-callback counters (load · height · action · error). The
 * same placement id may appear in multiple sections, so sections are keyed by [id], not by
 * [placementId].
 */
public class FrameSection(public val id: Long, public val placementId: String) {

    public var attached: Boolean by mutableStateOf(true)
        private set
    public var status: String by mutableStateOf("idle")
        private set
    public var load: Int by mutableIntStateOf(0)
        private set
    public var height: Int by mutableIntStateOf(0)
        private set
    public var action: Int by mutableIntStateOf(0)
        private set
    public var error: Int by mutableIntStateOf(0)
        private set

    public val counters: String
        get() = "load $load · height $height · action $action · error $error"

    internal fun toggleAttached() { attached = !attached }

    internal fun onStartLoading() { status = "startLoading" }
    internal fun onFinishLoading() { load += 1; status = "finishLoading" }
    internal fun onFailed(description: String) { error += 1; status = "failed — $description" }
    internal fun onHeightChange(heightPx: Int) { height += 1; status = "heightChange $heightPx" }
    internal fun onAction() { action += 1; status = "action" }
}
