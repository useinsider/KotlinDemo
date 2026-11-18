package com.useinsider.kotlindemo.action

import com.useinsider.insider.InsiderAppSmartRecommenderAttribute
import com.useinsider.insider.InsiderAppSmartRecommenderAttributesFilter
import com.useinsider.insider.InsiderAppSmartRecommenderCategoriesFilter
import com.useinsider.insider.InsiderAppSmartRecommenderFilter
import com.useinsider.insider.InsiderAppSmartRecommenderProductsFilter
import com.useinsider.kotlindemo.model.ClickableProduct
import com.useinsider.kotlindemo.model.ConditionForm
import com.useinsider.kotlindemo.model.ConditionValueType
import com.useinsider.kotlindemo.model.ProductForm
import com.useinsider.kotlindemo.model.SmartRecommenderForm

public enum class GetProductsOverload(public val label: String) {
    REFERENCE("getProducts(referenceId, query)"),
    REFERENCE_LOCALE("getProducts(referenceId, locale, query)"),
    IDENTIFIERS_REFERENCE("getProducts(identifiers, referenceId, query)"),
    IDENTIFIERS_REFERENCE_LOCALE("getProducts(identifiers, referenceId, locale, query)")
}

/** Example of a filter an integrator writes for a products request field the SDK has no class for. */
public class PartnerFilter(public val key: String, public val value: String) : InsiderAppSmartRecommenderFilter {
    override fun serialize(): Map<String, Any> = mapOf(key to value)
}

public object SmartRecommenderRequestBuilder {

    public const val CUSTOM_ATTRIBUTE: String = "custom"

    public val standardAttributes: List<InsiderAppSmartRecommenderAttribute> = listOf(
        InsiderAppSmartRecommenderAttribute.ITEM_ID,
        InsiderAppSmartRecommenderAttribute.NAME,
        InsiderAppSmartRecommenderAttribute.URL,
        InsiderAppSmartRecommenderAttribute.IMAGE_URL,
        InsiderAppSmartRecommenderAttribute.DESCRIPTION,
        InsiderAppSmartRecommenderAttribute.BRAND,
        InsiderAppSmartRecommenderAttribute.COLOR,
        InsiderAppSmartRecommenderAttribute.SIZE,
        InsiderAppSmartRecommenderAttribute.SKU,
        InsiderAppSmartRecommenderAttribute.GENDER,
        InsiderAppSmartRecommenderAttribute.GROUP_CODE,
        InsiderAppSmartRecommenderAttribute.MULTIPACK,
        InsiderAppSmartRecommenderAttribute.PRODUCT_TYPE,
        InsiderAppSmartRecommenderAttribute.GTIN,
        InsiderAppSmartRecommenderAttribute.CATEGORY,
        InsiderAppSmartRecommenderAttribute.IN_STOCK,
        InsiderAppSmartRecommenderAttribute.RATING,
        InsiderAppSmartRecommenderAttribute.STOCK_COUNT,
        InsiderAppSmartRecommenderAttribute.ITEM_UPDATE_DATE,
        InsiderAppSmartRecommenderAttribute.ITEM_START_DATE,
        InsiderAppSmartRecommenderAttribute.ITEM_END_DATE,
        InsiderAppSmartRecommenderAttribute.PRICE,
        InsiderAppSmartRecommenderAttribute.ORIGINAL_PRICE,
        InsiderAppSmartRecommenderAttribute.DISCOUNT,
        InsiderAppSmartRecommenderAttribute.OMNIBUS_PRICE,
        InsiderAppSmartRecommenderAttribute.OMNIBUS_DISCOUNT
    )

    public val attributeNames: List<String> = standardAttributes.map { it.name } + CUSTOM_ATTRIBUTE

    public fun hasIdentifiers(form: SmartRecommenderForm): Boolean =
        form.email.isNotEmpty() || form.phoneNumber.isNotEmpty() ||
            form.userId.isNotEmpty() || form.customIdentifierKey.isNotEmpty()

    public fun overload(form: SmartRecommenderForm): GetProductsOverload {
        val hasLocale = form.locale.isNotEmpty()

        return if (hasIdentifiers(form)) {
            if (hasLocale) GetProductsOverload.IDENTIFIERS_REFERENCE_LOCALE else GetProductsOverload.IDENTIFIERS_REFERENCE
        } else {
            if (hasLocale) GetProductsOverload.REFERENCE_LOCALE else GetProductsOverload.REFERENCE
        }
    }

    public fun currency(form: SmartRecommenderForm): String? = form.currency.ifEmpty { null }

    // Empty entries are kept on purpose: the SDK, not the demo, rejects them.
    public fun split(text: String): List<String> =
        if (text.isEmpty()) emptyList() else text.split(",").map { it.trim() }

    public fun condition(form: ConditionForm): InsiderAppSmartRecommenderAttributesFilter.Condition {
        val attribute = if (form.attribute == CUSTOM_ATTRIBUTE) {
            InsiderAppSmartRecommenderAttribute.custom(form.customAttribute)
        } else {
            standardAttributes.firstOrNull { it.name == form.attribute }
                ?: InsiderAppSmartRecommenderAttribute.custom(form.attribute)
        }

        return when (form.valueType) {
            ConditionValueType.STRING ->
                InsiderAppSmartRecommenderAttributesFilter.Condition(attribute, form.operator, form.value)
            // NaN reaches the SDK so that it, not the demo, reports the invalid number.
            ConditionValueType.NUMBER -> InsiderAppSmartRecommenderAttributesFilter.Condition(
                attribute, form.operator, form.value.toDoubleOrNull() ?: Double.NaN
            )
            ConditionValueType.BOOLEAN ->
                InsiderAppSmartRecommenderAttributesFilter.Condition(attribute, form.operator, form.value == "true")
        }
    }

    public fun filters(
        form: SmartRecommenderForm,
        productFilter: (ProductForm) -> InsiderAppSmartRecommenderFilter
    ): List<InsiderAppSmartRecommenderFilter> {
        val filters = mutableListOf<InsiderAppSmartRecommenderFilter>()

        if (form.productsFilterEnabled) {
            filters.add(InsiderAppSmartRecommenderProductsFilter(split(form.productIds)))
        }
        if (form.categoriesFilterEnabled) {
            filters.add(InsiderAppSmartRecommenderCategoriesFilter(split(form.categories)))
        }
        if (form.productFilterEnabled) {
            filters.add(productFilter(form.product))
        }
        if (form.attributesFilterEnabled) {
            filters.add(InsiderAppSmartRecommenderAttributesFilter(form.conditions.map { condition(it) }))
        }
        if (form.partnerFilterEnabled) {
            filters.add(PartnerFilter(form.partnerKey, form.partnerValue))
        }

        return filters
    }

    public fun clickMap(product: ClickableProduct): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "product_id" to product.id,
            "name" to product.name,
            "taxonomy" to product.categories,
            "image_url" to (product.imageUrl ?: ""),
            "price" to product.price,
            "currency" to product.currency
        )
        product.groupCode?.let { map["group_code"] = it }

        return map
    }
}
