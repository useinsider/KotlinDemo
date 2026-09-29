package com.useinsider.kotlindemo.model

import com.useinsider.insider.InsiderAppFramesViewStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FrameSectionTest {

    private val ready = InsiderAppFramesViewStatus.READY
    private val notReady = InsiderAppFramesViewStatus.entries.first { it != ready }

    @Test
    fun newSectionStartsAttachedIdleAndWithZeroCounters() {
        val section = FrameSection(id = 7, placementId = "home")

        assertEquals(7L, section.id)
        assertEquals("home", section.placementId)
        assertTrue(section.attached)
        assertEquals("idle", section.status)
        assertEquals("load 0 · height 0 · action 0 · error 0", section.counters)
    }

    @Test
    fun toggleAttachedFlipsBackAndForth() {
        val section = FrameSection(0, "home")

        section.toggleAttached()
        assertFalse(section.attached)
        section.toggleAttached()
        assertTrue(section.attached)
    }

    @Test
    fun statusChangeIntoReadyIncrementsLoad() {
        val section = FrameSection(0, "home")

        section.onStatusChange(ready, notReady)

        assertEquals(1, section.load)
        assertEquals("${notReady.name} → ${ready.name}", section.status)
    }

    @Test
    fun statusChangeIntoAnyOtherStatusDoesNotIncrementLoad() {
        val section = FrameSection(0, "home")

        InsiderAppFramesViewStatus.entries.filter { it != ready }.forEach {
            section.onStatusChange(it, ready)
        }

        assertEquals(0, section.load)
    }

    @Test
    fun failureIncrementsErrorAndShowsDescription() {
        val section = FrameSection(0, "home")

        section.onFailed("timeout")
        section.onFailed("offline")

        assertEquals(2, section.error)
        assertEquals("failed — offline", section.status)
    }

    @Test
    fun heightChangeIncrementsHeightAndShowsPixels() {
        val section = FrameSection(0, "home")

        section.onHeightChange(320)

        assertEquals(1, section.height)
        assertEquals("heightChange 320", section.status)
    }

    @Test
    fun actionIncrementsAction() {
        val section = FrameSection(0, "home")

        section.onAction()

        assertEquals(1, section.action)
        assertEquals("action", section.status)
    }

    @Test
    fun countersAreIndependent() {
        val section = FrameSection(0, "home")

        section.onStatusChange(ready, notReady)
        section.onHeightChange(10)
        section.onHeightChange(20)
        section.onAction()
        section.onAction()
        section.onAction()
        section.onFailed("x")

        assertEquals("load 1 · height 2 · action 3 · error 1", section.counters)
    }
}
