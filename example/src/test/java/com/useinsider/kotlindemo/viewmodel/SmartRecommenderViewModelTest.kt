package com.useinsider.kotlindemo.viewmodel

import com.useinsider.kotlindemo.model.ConditionForm
import com.useinsider.kotlindemo.model.MapEntryForm
import com.useinsider.kotlindemo.model.MapValueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmartRecommenderViewModelTest {

    private val viewModel = SmartRecommenderViewModel()

    @Test
    fun addConditionAppendsEmptyConditions() {
        viewModel.addCondition()
        viewModel.addCondition()

        assertEquals(listOf(ConditionForm(), ConditionForm()), viewModel.form.conditions)
    }

    @Test
    fun updateConditionChangesOnlyTheTargetIndex() {
        viewModel.addCondition()
        viewModel.addCondition()

        viewModel.updateCondition(1) { it.copy(value = "Nike") }

        assertEquals(listOf(ConditionForm(), ConditionForm(value = "Nike")), viewModel.form.conditions)
    }

    @Test
    fun updateConditionIgnoresOutOfRangeIndices() {
        viewModel.addCondition()
        viewModel.addCondition()
        val before = viewModel.form

        viewModel.updateCondition(5) { it.copy(value = "Nike") }
        viewModel.updateCondition(-1) { it.copy(value = "Nike") }

        assertEquals(before, viewModel.form)
    }

    @Test
    fun removeConditionRemovesOnlyTheTargetIndex() {
        viewModel.addCondition()
        viewModel.addCondition()
        viewModel.updateCondition(1) { it.copy(value = "Nike") }

        viewModel.removeCondition(0)

        assertEquals(listOf(ConditionForm(value = "Nike")), viewModel.form.conditions)
    }

    @Test
    fun removeConditionIgnoresOutOfRangeIndices() {
        viewModel.addCondition()
        viewModel.addCondition()
        val before = viewModel.form

        viewModel.removeCondition(2)
        viewModel.removeCondition(-1)

        assertEquals(before, viewModel.form)
    }

    @Test
    fun toggleSelectedAddsThenRemovesTheIndex() {
        viewModel.toggleSelected(2)
        assertEquals(setOf(2), viewModel.selectedIndices)

        viewModel.toggleSelected(2)
        assertEquals(emptySet<Int>(), viewModel.selectedIndices)
    }

    @Test
    fun addMapEntryAppendsAnEmptyEntry() {
        viewModel.addMapEntry()

        assertEquals(listOf(MapEntryForm()), viewModel.mapEntries)
    }

    @Test
    fun updateMapEntryChangesOnlyTheTargetIndex() {
        viewModel.addMapEntry()
        viewModel.addMapEntry()

        viewModel.updateMapEntry(1) { it.copy(key = "brand", value = "Nike") }

        assertEquals(listOf(MapEntryForm(), MapEntryForm(key = "brand", value = "Nike")), viewModel.mapEntries)
    }

    @Test
    fun updateMapEntryIgnoresOutOfRangeIndices() {
        viewModel.addMapEntry()
        val before = viewModel.mapEntries

        viewModel.updateMapEntry(1) { it.copy(key = "brand") }
        viewModel.updateMapEntry(-1) { it.copy(key = "brand") }

        assertEquals(before, viewModel.mapEntries)
    }

    @Test
    fun removeMapEntryRemovesOnlyTheTargetIndex() {
        viewModel.addMapEntry()
        viewModel.addMapEntry()
        viewModel.updateMapEntry(1) { it.copy(key = "brand") }

        viewModel.removeMapEntry(0)

        assertEquals(listOf(MapEntryForm(key = "brand")), viewModel.mapEntries)
    }

    @Test
    fun removeMapEntryIgnoresOutOfRangeIndices() {
        viewModel.addMapEntry()
        val before = viewModel.mapEntries

        viewModel.removeMapEntry(1)
        viewModel.removeMapEntry(-1)

        assertEquals(before, viewModel.mapEntries)
    }

    @Test
    fun clickSelectedWithManualReferenceWithoutResultRecordsNoAction() {
        viewModel.toggleSelected(0)
        viewModel.updateManualReferenceId("manual-ref")

        viewModel.clickSelectedWithManualReference()

        assertNull(viewModel.lastAction)
    }

    @Test
    fun fillMapFromSelectedWithoutResultKeepsTypedEntries() {
        viewModel.addMapEntry()
        viewModel.updateMapEntry(0) { MapEntryForm(key = "price", type = MapValueType.NUMBER, value = "9.5") }
        viewModel.toggleSelected(0)

        viewModel.fillMapFromSelected()

        assertEquals(
            listOf(MapEntryForm(key = "price", type = MapValueType.NUMBER, value = "9.5")),
            viewModel.mapEntries
        )
    }
}
