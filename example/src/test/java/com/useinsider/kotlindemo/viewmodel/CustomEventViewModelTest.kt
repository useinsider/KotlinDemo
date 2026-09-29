package com.useinsider.kotlindemo.viewmodel

import com.useinsider.kotlindemo.model.EventParameter
import com.useinsider.kotlindemo.model.ParameterType
import org.junit.Assert.assertEquals
import org.junit.Test

class CustomEventViewModelTest {

    private val viewModel = CustomEventViewModel()

    @Test
    fun startsWithOneEmptyStringParameter() {
        assertEquals("", viewModel.eventName)
        assertEquals(listOf(EventParameter()), viewModel.parameters.toList())
        assertEquals(ParameterType.STRING, viewModel.parameters.single().type)
    }

    @Test
    fun addAndRemoveParameter() {
        viewModel.addParameter()
        viewModel.updateParameterName(1, "second")
        assertEquals(2, viewModel.parameters.size)

        viewModel.removeParameter(0)

        assertEquals(listOf("second"), viewModel.parameters.map { it.name })
    }

    @Test
    fun updatesChangeOnlyTheTargetField() {
        viewModel.updateParameterType(0, ParameterType.INTEGER)
        viewModel.updateParameterName(0, "count")
        viewModel.updateParameterValue(0, "3")

        assertEquals(EventParameter(ParameterType.INTEGER, "count", "3"), viewModel.parameters[0])
    }

    @Test
    fun outOfRangeIndicesAreIgnored() {
        val before = viewModel.parameters.toList()

        viewModel.removeParameter(5)
        viewModel.removeParameter(-1)
        viewModel.updateParameterType(5, ParameterType.BOOLEAN)
        viewModel.updateParameterName(5, "x")
        viewModel.updateParameterValue(-1, "y")

        assertEquals(before, viewModel.parameters.toList())
    }

    @Test
    fun sendEventWithBlankNameReportsErrorWithoutCallingSdk() {
        val messages = mutableListOf<String>()
        viewModel.eventName = "   "

        viewModel.sendEvent { messages += it }

        assertEquals(listOf("Event name is required"), messages)
    }
}
