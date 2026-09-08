package com.useinsider.kotlindemo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.useinsider.kotlindemo.model.FrameSection

/**
 * Drives the runtime App Frames test screen: placement ids are added at runtime, each getting its
 * own [FrameSection] tracked in display order. Listener callbacks resolve their section directly
 * (the section is captured by the listener), and a template action is surfaced through
 * [pendingActionPayload] as a dialog.
 */
public class AppFramesViewModel : ViewModel() {

    private var nextId: Long = 0

    /** Sections in display order. */
    public val sections: MutableList<FrameSection> = mutableStateListOf()

    /** Non-null while a template action payload is being shown in a dialog. */
    public var pendingActionPayload: String? by mutableStateOf<String?>(null)
        private set

    /** Adds a section for [placementId]; blanks are ignored. Duplicates are allowed on purpose. */
    public fun addPlacement(placementId: String): Unit {
        val trimmed = placementId.trim()
        if (trimmed.isEmpty()) return
        sections.add(FrameSection(nextId++, trimmed))
    }

    /** Removes the section with [id] — used by Delete and by a template-driven dismiss. */
    public fun remove(id: Long): Unit {
        sections.removeAll { it.id == id }
    }

    public fun showAction(payload: String): Unit {
        pendingActionPayload = payload
    }

    public fun dismissAction(): Unit {
        pendingActionPayload = null
    }
}
