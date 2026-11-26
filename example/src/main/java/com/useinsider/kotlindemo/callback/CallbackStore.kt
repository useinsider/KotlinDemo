package com.useinsider.kotlindemo.callback

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

public object CallbackStore {
    // Not a StateFlow: it drops a value equal to the current one, so a repeated callback would not reach the label.
    private val _latestCallback = MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    public val latestCallback: SharedFlow<String> = _latestCallback

    public fun update(message: String): Unit {
        _latestCallback.tryEmit(message)
    }
}
