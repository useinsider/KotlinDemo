package com.useinsider.kotlindemo.model

import com.useinsider.insider.InsiderAppSmartRecommenderOperator

public enum class ConditionValueType { STRING, NUMBER, BOOLEAN }

public data class ConditionForm(
    val attribute: String = "brand",
    val customAttribute: String = "",
    val operator: InsiderAppSmartRecommenderOperator = InsiderAppSmartRecommenderOperator.EQUALS,
    val valueType: ConditionValueType = ConditionValueType.STRING,
    val value: String = ""
)

public data class ProductForm(
    val id: String = "",
    val name: String = "",
    val taxonomy: String = "",
    val imageUrl: String = "",
    val price: String = "",
    val currency: String = ""
)

public data class SmartRecommenderForm(
    val referenceId: String = "",
    val locale: String = "",
    val currency: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val userId: String = "",
    val customIdentifierKey: String = "",
    val customIdentifierValue: String = "",
    val productsFilterEnabled: Boolean = false,
    val productIds: String = "",
    val categoriesFilterEnabled: Boolean = false,
    val categories: String = "",
    val attributesFilterEnabled: Boolean = false,
    val conditions: List<ConditionForm> = emptyList(),
    val productFilterEnabled: Boolean = false,
    val product: ProductForm = ProductForm(),
    val partnerFilterEnabled: Boolean = false,
    val partnerKey: String = "",
    val partnerValue: String = ""
)

public data class ClickableProduct(
    val id: String,
    val name: String,
    val categories: List<String>,
    val imageUrl: String?,
    val price: Double,
    val currency: String,
    val groupCode: String?
)

public enum class MapValueType { STRING, NUMBER, BOOLEAN, LIST }

public data class MapEntryForm(
    val key: String = "",
    val type: MapValueType = MapValueType.STRING,
    val value: String = ""
)
