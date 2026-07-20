package com.useinsider.kotlindemo.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.useinsider.kotlindemo.util.DismissedFramesStore

/** UI status of a single App Frames placement, driven by the SDK view listener callbacks. */
public sealed interface FrameStatus {
    public object Idle : FrameStatus
    public object Loading : FrameStatus
    public object Loaded : FrameStatus
    public object HeightUpdated : FrameStatus
    public object ActionTriggered : FrameStatus
    public object Dismissed : FrameStatus
    public data class Failed(val message: String) : FrameStatus
}

public class AppFramesViewModel(application: Application) : AndroidViewModel(application) {

    private val store: DismissedFramesStore = DismissedFramesStore(application)

    // Per-placement latest status. mutableStateMapOf → reads recompose only the affected chip.
    private val statuses = mutableStateMapOf<String, FrameStatus>()

    public var dismissed: Set<String> by mutableStateOf(store.dismissed())
        private set

    public fun statusFor(placementId: String): FrameStatus =
        statuses[placementId] ?: FrameStatus.Idle

    public fun updateStatus(placementId: String, status: FrameStatus): Unit {
        statuses[placementId] = status
    }

    public fun isDismissed(placementId: String): Boolean = dismissed.contains(placementId)

    /** Recommended pattern: persist the dismiss and drop the frame from the UI. */
    public fun persistDismiss(placementId: String): Unit {
        store.add(placementId)
        dismissed = store.dismissed()
        statuses[placementId] = FrameStatus.Dismissed
    }

    public fun resetDismissed(): Unit {
        store.clear()
        dismissed = store.dismissed()
        statuses.clear()
    }
}
