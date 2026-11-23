package com.useinsider.kotlindemo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.useinsider.insider.Insider
import com.useinsider.insider.InsiderAppSmartRecommenderCallback
import com.useinsider.insider.InsiderAppSmartRecommenderException
import com.useinsider.insider.InsiderAppSmartRecommenderProduct
import com.useinsider.insider.InsiderAppSmartRecommenderProductFilter
import com.useinsider.insider.InsiderAppSmartRecommenderQuery
import com.useinsider.insider.InsiderAppSmartRecommenderResult
import com.useinsider.insider.InsiderIdentifiers
import com.useinsider.insider.InsiderProduct
import com.useinsider.kotlindemo.action.GetProductsOverload
import com.useinsider.kotlindemo.action.SmartRecommenderRequestBuilder
import com.useinsider.kotlindemo.model.ClickableProduct
import com.useinsider.kotlindemo.model.ConditionForm
import com.useinsider.kotlindemo.model.MapEntryForm
import com.useinsider.kotlindemo.model.ProductForm
import com.useinsider.kotlindemo.model.SmartRecommenderForm

public class SmartRecommenderViewModel : ViewModel() {

    public var form: SmartRecommenderForm by mutableStateOf(SmartRecommenderForm())
        private set

    public var isLoading: Boolean by mutableStateOf(false)
        private set

    public var lastCall: String? by mutableStateOf<String?>(null)
        private set

    public var result: InsiderAppSmartRecommenderResult? by mutableStateOf<InsiderAppSmartRecommenderResult?>(null)
        private set

    public var error: InsiderAppSmartRecommenderException?
        by mutableStateOf<InsiderAppSmartRecommenderException?>(null)
        private set

    public var lastAction: String? by mutableStateOf<String?>(null)
        private set

    public var autoView: Boolean by mutableStateOf(true)
        private set

    public var selectedIndices: Set<Int> by mutableStateOf(emptySet())
        private set

    public var manualReferenceId: String by mutableStateOf("")
        private set

    public var mapEntries: List<MapEntryForm> by mutableStateOf(emptyList())
        private set

    public val selectedProducts: List<InsiderAppSmartRecommenderProduct>
        get() = result?.products.orEmpty().filterIndexed { index, _ -> index in selectedIndices }

    public fun updateForm(transform: (SmartRecommenderForm) -> SmartRecommenderForm): Unit {
        form = transform(form)
    }

    public fun addCondition(): Unit {
        form = form.copy(conditions = form.conditions + ConditionForm())
    }

    public fun updateCondition(index: Int, transform: (ConditionForm) -> ConditionForm): Unit {
        if (index !in form.conditions.indices) return

        form = form.copy(
            conditions = form.conditions.mapIndexed { position, condition ->
                if (position == index) transform(condition) else condition
            }
        )
    }

    public fun removeCondition(index: Int): Unit {
        if (index !in form.conditions.indices) return

        form = form.copy(conditions = form.conditions.filterIndexed { position, _ -> position != index })
    }

    public fun getProducts(): Unit {
        if (isLoading) return

        val request = form
        val overload = SmartRecommenderRequestBuilder.overload(request)
        val query = InsiderAppSmartRecommenderQuery(
            SmartRecommenderRequestBuilder.currency(request),
            SmartRecommenderRequestBuilder.filters(request) { product ->
                InsiderAppSmartRecommenderProductFilter(
                    Insider.Instance.createNewProduct(
                        product.id,
                        product.name,
                        SmartRecommenderRequestBuilder.split(product.taxonomy).toTypedArray(),
                        product.imageUrl,
                        price(product),
                        product.currency
                    )
                )
            }
        )
        val callback = InsiderAppSmartRecommenderCallback { products, failure -> onComplete(products, failure) }
        val recommender = Insider.Instance.appSmartRecommender()

        isLoading = true
        result = null
        error = null
        lastCall = overload.label

        when (overload) {
            GetProductsOverload.REFERENCE ->
                recommender.getProducts(request.referenceId, query, callback)
            GetProductsOverload.REFERENCE_LOCALE ->
                recommender.getProducts(request.referenceId, request.locale, query, callback)
            GetProductsOverload.IDENTIFIERS_REFERENCE ->
                recommender.getProducts(identifiers(request), request.referenceId, query, callback)
            GetProductsOverload.IDENTIFIERS_REFERENCE_LOCALE ->
                recommender.getProducts(identifiers(request), request.referenceId, request.locale, query, callback)
        }
    }

    public fun clickWithProduct(product: InsiderAppSmartRecommenderProduct): Unit {
        Insider.Instance.appSmartRecommender().click(product.context.referenceId, insiderProduct(product))
        lastAction = SmartRecommenderRequestBuilder.callLabel("click(referenceId, InsiderProduct) ${product.id}")
    }

    public fun clickWithMap(product: InsiderAppSmartRecommenderProduct): Unit {
        Insider.Instance.appSmartRecommender().click(product.context.referenceId, clickMap(product))
        lastAction = SmartRecommenderRequestBuilder.callLabel("click(referenceId, Map) ${product.id}")
    }

    public fun updateAutoView(enabled: Boolean): Unit {
        autoView = enabled
    }

    public fun toggleSelected(index: Int): Unit {
        selectedIndices = if (index in selectedIndices) selectedIndices - index else selectedIndices + index
    }

    public fun updateManualReferenceId(value: String): Unit {
        manualReferenceId = value
    }

    public fun viewSelected(): Unit {
        val products = selectedProducts

        Insider.Instance.appSmartRecommender().view(products)
        lastAction = SmartRecommenderRequestBuilder.callLabel("view(${products.size} products)")
    }

    public fun clickSelectedWithManualReference(): Unit {
        val product = selectedProducts.firstOrNull() ?: return

        Insider.Instance.appSmartRecommender().click(manualReferenceId, insiderProduct(product))
        lastAction = SmartRecommenderRequestBuilder.callLabel(
            "click(\"$manualReferenceId\", InsiderProduct) ${product.id}"
        )
    }

    public fun fillMapFromSelected(): Unit {
        val product = selectedProducts.firstOrNull() ?: return

        mapEntries = SmartRecommenderRequestBuilder.mapEntries(clickMap(product))
    }

    public fun addMapEntry(): Unit {
        mapEntries = mapEntries + MapEntryForm()
    }

    public fun updateMapEntry(index: Int, transform: (MapEntryForm) -> MapEntryForm): Unit {
        if (index !in mapEntries.indices) return

        mapEntries = mapEntries.mapIndexed { position, entry -> if (position == index) transform(entry) else entry }
    }

    public fun removeMapEntry(index: Int): Unit {
        if (index !in mapEntries.indices) return

        mapEntries = mapEntries.filterIndexed { position, _ -> position != index }
    }

    public fun clickWithManualMap(): Unit {
        val map = SmartRecommenderRequestBuilder.clickMap(mapEntries)

        Insider.Instance.appSmartRecommender().click(manualReferenceId, map)
        lastAction = SmartRecommenderRequestBuilder.callLabel("click(\"$manualReferenceId\", Map ${map.keys})")
    }

    public fun setGdprConsent(consent: Boolean): Unit {
        Insider.Instance.setGDPRConsent(consent)
        lastAction = "GDPR consent: $consent"
    }

    private fun onComplete(
        products: InsiderAppSmartRecommenderResult?,
        failure: InsiderAppSmartRecommenderException?
    ) {
        isLoading = false
        result = products
        error = failure
        selectedIndices = emptySet()
        products?.let { manualReferenceId = it.context.referenceId }

        // Recorded here, not from the composable: a recomposition must not record a second view.
        if (autoView && products != null && products.outcome == InsiderAppSmartRecommenderResult.Outcome.FILLED) {
            Insider.Instance.appSmartRecommender().view(products.products)
            lastAction = SmartRecommenderRequestBuilder.callLabel("view(${products.products.size} products)")
        }
    }

    private fun insiderProduct(product: InsiderAppSmartRecommenderProduct): InsiderProduct {
        val insiderProduct = Insider.Instance.createNewProduct(
            product.id,
            product.name,
            product.categories.toTypedArray(),
            product.imageUrl ?: "",
            product.price,
            product.currency
        )
        product.groupCode?.let { insiderProduct.setGroupCode(it) }

        return insiderProduct
    }

    private fun clickMap(product: InsiderAppSmartRecommenderProduct): Map<String, Any> =
        SmartRecommenderRequestBuilder.clickMap(
            ClickableProduct(
                id = product.id,
                name = product.name,
                categories = product.categories,
                imageUrl = product.imageUrl,
                price = product.price,
                currency = product.currency,
                groupCode = product.groupCode
            )
        )

    private fun identifiers(request: SmartRecommenderForm): InsiderIdentifiers {
        val identifiers = InsiderIdentifiers()

        if (request.email.isNotEmpty()) identifiers.addEmail(request.email)
        if (request.phoneNumber.isNotEmpty()) identifiers.addPhoneNumber(request.phoneNumber)
        if (request.userId.isNotEmpty()) identifiers.addUserID(request.userId)
        if (request.customIdentifierKey.isNotEmpty()) {
            identifiers.addCustomIdentifier(request.customIdentifierKey, request.customIdentifierValue)
        }

        return identifiers
    }

    private fun price(product: ProductForm): Double = product.price.toDoubleOrNull() ?: Double.NaN
}
