package com.useinsider.kotlindemo.viewmodel

import com.useinsider.kotlindemo.model.EventParameter
import com.useinsider.kotlindemo.model.ParameterType
import org.junit.Assert.assertEquals
import org.junit.Test

class CustomAttributesViewModelTest {

    private val viewModel = CustomAttributesViewModel()

    @Test
    fun startsWithOneEmptyStringAttribute() {
        assertEquals(listOf(EventParameter()), viewModel.attributes.toList())
    }

    @Test
    fun addAndRemoveAttribute() {
        viewModel.addAttribute()
        viewModel.updateAttributeName(1, "second")
        assertEquals(2, viewModel.attributes.size)

        viewModel.removeAttribute(0)

        assertEquals(listOf("second"), viewModel.attributes.map { it.name })
    }

    @Test
    fun updatesChangeOnlyTheTargetField() {
        viewModel.updateAttributeType(0, ParameterType.DOUBLE)
        viewModel.updateAttributeName(0, "score")
        viewModel.updateAttributeValue(0, "1.5")

        assertEquals(EventParameter(ParameterType.DOUBLE, "score", "1.5"), viewModel.attributes[0])
    }

    @Test
    fun outOfRangeIndicesAreIgnored() {
        val before = viewModel.attributes.toList()

        viewModel.removeAttribute(3)
        viewModel.updateAttributeType(3, ParameterType.BOOLEAN)
        viewModel.updateAttributeName(-1, "x")
        viewModel.updateAttributeValue(3, "y")

        assertEquals(before, viewModel.attributes.toList())
    }

    @Test
    fun setAttributesSkipsBlankNamesAndReportsTotalCount() {
        val messages = mutableListOf<String>()
        viewModel.addAttribute()
        viewModel.updateAttributeName(1, "  ")

        viewModel.setAttributes { messages += it }

        assertEquals(listOf("Custom attributes set (2)"), messages)
    }
}
