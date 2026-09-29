package com.useinsider.kotlindemo.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppFramesViewModelTest {

    private val viewModel = AppFramesViewModel()

    @Test
    fun addPlacementTrimsAndAppendsInOrder() {
        viewModel.addPlacement("  home  ")
        viewModel.addPlacement("cart")

        assertEquals(listOf("home", "cart"), viewModel.sections.map { it.placementId })
    }

    @Test
    fun addPlacementIgnoresBlankInput() {
        viewModel.addPlacement("")
        viewModel.addPlacement("   ")

        assertTrue(viewModel.sections.isEmpty())
    }

    @Test
    fun duplicatePlacementIdsGetDistinctSectionIds() {
        viewModel.addPlacement("home")
        viewModel.addPlacement("home")

        assertEquals(2, viewModel.sections.size)
        assertEquals(2, viewModel.sections.map { it.id }.toSet().size)
    }

    @Test
    fun removeDeletesOnlyTheMatchingSection() {
        viewModel.addPlacement("home")
        viewModel.addPlacement("home")
        val (first, second) = viewModel.sections.toList()

        viewModel.remove(first.id)

        assertEquals(listOf(second.id), viewModel.sections.map { it.id })
    }

    @Test
    fun sectionIdsAreNotReusedAfterRemoval() {
        viewModel.addPlacement("home")
        val removedId = viewModel.sections.single().id
        viewModel.remove(removedId)

        viewModel.addPlacement("home")

        assertTrue(viewModel.sections.single().id != removedId)
    }

    @Test
    fun removeWithUnknownIdIsNoOp() {
        viewModel.addPlacement("home")

        viewModel.remove(999)

        assertEquals(1, viewModel.sections.size)
    }

    @Test
    fun showAndDismissActionDrivePendingPayload() {
        assertNull(viewModel.pendingActionPayload)

        viewModel.showAction("{\"k\":1}")
        assertEquals("{\"k\":1}", viewModel.pendingActionPayload)

        viewModel.dismissAction()
        assertNull(viewModel.pendingActionPayload)
    }
}
