package com.useinsider.kotlindemo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.useinsider.kotlindemo.action.InsiderActions
import com.useinsider.kotlindemo.model.EventParameter
import com.useinsider.kotlindemo.model.ParameterType

public class CustomEventViewModel : ViewModel() {

    public var eventName: String by mutableStateOf("")
    public var nameError: String? by mutableStateOf(null)
    public val parameters: SnapshotStateList<EventParameter> = mutableStateListOf(EventParameter())

    public fun addParameter(): Unit {
        parameters.add(EventParameter())
    }

    public fun removeParameter(index: Int): Unit {
        if (index in parameters.indices) {
            parameters.removeAt(index)
        }
    }

    public fun updateParameterType(index: Int, type: ParameterType): Unit {
        if (index in parameters.indices) {
            parameters[index] = parameters[index].copy(type = type)
        }
    }

    public fun updateParameterName(index: Int, name: String): Unit {
        if (index in parameters.indices) {
            parameters[index] = parameters[index].copy(name = name)
        }
    }

    public fun updateParameterValue(index: Int, value: String): Unit {
        if (index in parameters.indices) {
            parameters[index] = parameters[index].copy(value = value)
        }
    }

    // onSent runs only when the event was sent, so the screen stays open with what was typed on a blank name.
    public fun sendEvent(onSent: (String) -> Unit): Unit {
        if (eventName.isBlank()) {
            nameError = "Event name is required"
            return
        }
        nameError = null
        InsiderActions.tagCustomEvent(eventName, parameters.toList(), onSent)
    }
}
