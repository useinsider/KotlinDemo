package com.useinsider.kotlindemo.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.useinsider.kotlindemo.action.GetProductsOverload
import com.useinsider.kotlindemo.model.MapEntryForm
import com.useinsider.kotlindemo.model.MapValueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmartRecommenderViewModelInstrumentedTest {

    private companion object {
        const val REFERENCE_ID = "kotlindemo-instrumented-reference"
    }

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private lateinit var viewModel: SmartRecommenderViewModel

    private fun <T> onMain(block: () -> T): T {
        var value: Result<T>? = null
        instrumentation.runOnMainSync { value = runCatching(block) }
        return value!!.getOrThrow()
    }

    private fun createViewModel(): SmartRecommenderViewModel = onMain { SmartRecommenderViewModel() }

    @Test
    fun getProductsWithReferenceAndLocaleRecordsTheLocaleOverloadAndStartsLoading() {
        viewModel = createViewModel()

        val (lastCall, isLoading) = onMain {
            viewModel.updateForm { it.copy(referenceId = REFERENCE_ID, locale = "en_US") }
            viewModel.getProducts()
            viewModel.lastCall to viewModel.isLoading
        }

        assertEquals(GetProductsOverload.REFERENCE_LOCALE.label, lastCall)
        assertTrue("callback fired synchronously", isLoading)
    }

    @Test
    fun getProductsWithReferenceAndIdentifiersRecordsTheIdentifiersOverloadAndStartsLoading() {
        viewModel = createViewModel()

        val (lastCall, isLoading) = onMain {
            viewModel.updateForm { it.copy(referenceId = REFERENCE_ID, email = "demo@useinsider.com") }
            viewModel.getProducts()
            viewModel.lastCall to viewModel.isLoading
        }

        assertEquals(GetProductsOverload.IDENTIFIERS_REFERENCE.label, lastCall)
        assertTrue("callback fired synchronously", isLoading)
    }

    @Test
    fun getProductsWhileLoadingIgnoresTheSecondCall() {
        viewModel = createViewModel()

        val (lastCall, isLoading) = onMain {
            viewModel.updateForm { it.copy(referenceId = REFERENCE_ID) }
            viewModel.getProducts()
            viewModel.updateForm { it.copy(email = "demo@useinsider.com", locale = "en_US") }
            viewModel.getProducts()
            viewModel.lastCall to viewModel.isLoading
        }

        assertTrue("callback fired synchronously", isLoading)
        assertEquals(GetProductsOverload.REFERENCE.label, lastCall)
    }

    @Test
    fun getProductsClearsTheSelectionAndLastActionOfThePreviousRequestBeforeTheCallback() {
        viewModel = createViewModel()
        val (selectedBefore, actionBefore) = onMain {
            viewModel.toggleSelected(0)
            viewModel.viewSelected()
            viewModel.selectedIndices to viewModel.lastAction
        }
        assertEquals(setOf(0), selectedBefore)
        assertNotNull(actionBefore)

        val (isLoading, selectedAfter, actionAfter) = onMain {
            viewModel.updateForm { it.copy(referenceId = REFERENCE_ID) }
            viewModel.getProducts()
            Triple(viewModel.isLoading, viewModel.selectedIndices, viewModel.lastAction)
        }

        assertTrue("callback fired synchronously", isLoading)
        assertEquals(emptySet<Int>(), selectedAfter)
        assertNull(actionAfter)
    }

    @Test
    fun viewSelectedWithNoSelectionRecordsAViewOfZeroProducts() {
        viewModel = createViewModel()

        val lastAction = onMain {
            viewModel.viewSelected()
            viewModel.lastAction
        }

        assertEquals("called view(0 products) — the SDK may refuse it, check logcat", lastAction)
    }

    @Test
    fun clickWithManualMapNamesTheTypedReferenceAndMapKeys() {
        viewModel = createViewModel()

        val lastAction = onMain {
            viewModel.updateManualReferenceId("manual-reference")
            viewModel.addMapEntry()
            viewModel.addMapEntry()
            viewModel.updateMapEntry(0) { MapEntryForm(key = "brand", type = MapValueType.STRING, value = "Nike") }
            viewModel.updateMapEntry(1) { MapEntryForm(key = "price", type = MapValueType.NUMBER, value = "9.5") }
            viewModel.clickWithManualMap()
            viewModel.lastAction
        }

        assertEquals(
            "called click(\"manual-reference\", Map [brand, price]) — the SDK may refuse it, check logcat",
            lastAction
        )
    }

    @Test
    fun viewWidgetNamesTheTypedReference() {
        viewModel = createViewModel()

        val lastAction = onMain {
            viewModel.updateManualReferenceId("manual-reference")
            viewModel.viewWidget()
            viewModel.lastAction
        }

        assertEquals("called viewWidget(\"manual-reference\") — the SDK may refuse it, check logcat", lastAction)
    }

    @Test
    fun clickWidgetNamesTheTypedReference() {
        viewModel = createViewModel()

        val lastAction = onMain {
            viewModel.updateManualReferenceId("manual-reference")
            viewModel.clickWidget()
            viewModel.lastAction
        }

        assertEquals("called clickWidget(\"manual-reference\") — the SDK may refuse it, check logcat", lastAction)
    }

    @Test
    fun viewWidgetWithEmptyReferencePassesItThrough() {
        viewModel = createViewModel()

        val lastAction = onMain {
            viewModel.viewWidget()
            viewModel.lastAction
        }

        assertEquals("called viewWidget(\"\") — the SDK may refuse it, check logcat", lastAction)
    }

    @Test
    fun clickWidgetWithEmptyReferencePassesItThrough() {
        viewModel = createViewModel()

        val lastAction = onMain {
            viewModel.clickWidget()
            viewModel.lastAction
        }

        assertEquals("called clickWidget(\"\") — the SDK may refuse it, check logcat", lastAction)
    }
}
