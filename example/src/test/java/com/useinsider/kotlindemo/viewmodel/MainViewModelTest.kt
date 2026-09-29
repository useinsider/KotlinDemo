package com.useinsider.kotlindemo.viewmodel

import com.useinsider.kotlindemo.callback.CallbackStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        CallbackStore.update("")
    }

    @After
    fun tearDown() {
        CallbackStore.update("")
        Dispatchers.resetMain()
    }

    @Test
    fun printLabelStartsEmpty() {
        assertEquals("", MainViewModel().printLabelText)
    }

    @Test
    fun callbackStoreMessagesReachThePrintLabel() {
        val viewModel = MainViewModel()

        CallbackStore.update("Login - Insider ID: abc")

        assertEquals("Login - Insider ID: abc", viewModel.printLabelText)
    }

    @Test
    fun latestCallbackIsReplayedToANewViewModel() {
        CallbackStore.update("earlier")

        assertEquals("earlier", MainViewModel().printLabelText)
    }

    @Test
    fun emptyCallbackDoesNotClearThePrintLabel() {
        val viewModel = MainViewModel()
        CallbackStore.update("kept")

        CallbackStore.update("")

        assertEquals("kept", viewModel.printLabelText)
    }

    @Test
    fun updateAndClearPrintLabel() {
        val viewModel = MainViewModel()

        viewModel.updatePrintLabel("manual")
        assertEquals("manual", viewModel.printLabelText)

        viewModel.clearPrintLabel()
        assertEquals("", viewModel.printLabelText)
    }
}
