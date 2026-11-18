package com.useinsider.kotlindemo.action

import com.useinsider.insider.InsiderAppSmartRecommenderAttribute
import com.useinsider.insider.InsiderAppSmartRecommenderAttributesFilter
import com.useinsider.insider.InsiderAppSmartRecommenderCategoriesFilter
import com.useinsider.insider.InsiderAppSmartRecommenderFilter
import com.useinsider.insider.InsiderAppSmartRecommenderOperator
import com.useinsider.insider.InsiderAppSmartRecommenderProductsFilter
import com.useinsider.kotlindemo.model.ClickableProduct
import com.useinsider.kotlindemo.model.ConditionForm
import com.useinsider.kotlindemo.model.ConditionValueType
import com.useinsider.kotlindemo.model.ProductForm
import com.useinsider.kotlindemo.model.SmartRecommenderForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartRecommenderRequestBuilderTest {

    private class StubProductFilter : InsiderAppSmartRecommenderFilter {
        override fun serialize(): Map<String, Any>? = null
    }

    private val stubProductFilter = StubProductFilter()

    private fun filters(form: SmartRecommenderForm): List<InsiderAppSmartRecommenderFilter> =
        SmartRecommenderRequestBuilder.filters(form) { stubProductFilter }

    @Test
    fun emptyLocaleNoIdentifiersPicksReference() {
        assertEquals(
            GetProductsOverload.REFERENCE,
            SmartRecommenderRequestBuilder.overload(SmartRecommenderForm(referenceId = "r"))
        )
    }

    @Test
    fun localePicksReferenceLocale() {
        assertEquals(
            GetProductsOverload.REFERENCE_LOCALE,
            SmartRecommenderRequestBuilder.overload(SmartRecommenderForm(referenceId = "r", locale = "en_US"))
        )
    }

    @Test
    fun identifiersPickIdentifiersReference() {
        val forms = listOf(
            SmartRecommenderForm(email = "user@example.com"),
            SmartRecommenderForm(phoneNumber = "+900000000000"),
            SmartRecommenderForm(userId = "user-1"),
            SmartRecommenderForm(customIdentifierKey = "loyalty_id")
        )

        forms.forEach { form ->
            assertEquals(GetProductsOverload.IDENTIFIERS_REFERENCE, SmartRecommenderRequestBuilder.overload(form))
        }
    }

    @Test
    fun identifiersAndLocalePickFullOverload() {
        assertEquals(
            GetProductsOverload.IDENTIFIERS_REFERENCE_LOCALE,
            SmartRecommenderRequestBuilder.overload(SmartRecommenderForm(email = "user@example.com", locale = "tr_TR"))
        )
    }

    @Test
    fun blankLocaleIsStillALocale() {
        assertEquals(
            GetProductsOverload.REFERENCE_LOCALE,
            SmartRecommenderRequestBuilder.overload(SmartRecommenderForm(locale = " "))
        )
    }

    @Test
    fun emptyCurrencyIsNullOthersPassThrough() {
        assertNull(SmartRecommenderRequestBuilder.currency(SmartRecommenderForm(currency = "")))
        assertEquals("try", SmartRecommenderRequestBuilder.currency(SmartRecommenderForm(currency = "try")))
        assertEquals(" ", SmartRecommenderRequestBuilder.currency(SmartRecommenderForm(currency = " ")))
    }

    @Test
    fun splitKeepsEmptyEntries() {
        assertEquals(listOf("a", "b"), SmartRecommenderRequestBuilder.split("a, b"))
        assertEquals(listOf("a", "", "b"), SmartRecommenderRequestBuilder.split("a,,b"))
        assertEquals(emptyList<String>(), SmartRecommenderRequestBuilder.split(""))
    }

    @Test
    fun noFilterEnabledGivesEmptyList() {
        assertEquals(emptyList<InsiderAppSmartRecommenderFilter>(), filters(SmartRecommenderForm()))
    }

    @Test
    fun enabledFilterWithEmptyTextSendsEmptyList() {
        val result = filters(SmartRecommenderForm(productsFilterEnabled = true, productIds = ""))

        val filter = result.single() as InsiderAppSmartRecommenderProductsFilter
        assertEquals(emptyList<String>(), filter.productIds)
    }

    @Test
    fun allFiltersInOrder() {
        val form = SmartRecommenderForm(
            productsFilterEnabled = true, productIds = "p1,p2",
            categoriesFilterEnabled = true, categories = "Shoes",
            attributesFilterEnabled = true,
            conditions = listOf(
                ConditionForm(attribute = "brand", value = "Nike"),
                ConditionForm(attribute = "price", valueType = ConditionValueType.NUMBER, value = "10")
            ),
            productFilterEnabled = true, product = ProductForm(id = "p9"),
            partnerFilterEnabled = true, partnerKey = "demo_field", partnerValue = "x"
        )

        val result = filters(form)

        assertEquals(5, result.size)
        assertEquals(listOf("p1", "p2"), (result[0] as InsiderAppSmartRecommenderProductsFilter).productIds)
        assertEquals(listOf("Shoes"), (result[1] as InsiderAppSmartRecommenderCategoriesFilter).categories)
        assertSame(stubProductFilter, result[2])
        assertEquals(2, (result[3] as InsiderAppSmartRecommenderAttributesFilter).conditions.size)
        assertEquals("demo_field", (result[4] as PartnerFilter).key)
    }

    @Test
    fun productFilterReceivesTheProductForm() {
        var received: ProductForm? = null
        val product = ProductForm(id = "p9", name = "Shoe")

        SmartRecommenderRequestBuilder.filters(SmartRecommenderForm(productFilterEnabled = true, product = product)) {
            received = it
            stubProductFilter
        }

        assertEquals(product, received)
    }

    @Test
    fun partnerFilterWritesItsField() {
        assertEquals(mapOf<String, Any>("demo_field" to "x"), PartnerFilter("demo_field", "x").serialize())
    }

    @Test
    fun conditionMapsStandardAndCustomAttribute() {
        val standard = SmartRecommenderRequestBuilder.condition(
            ConditionForm(attribute = "brand", operator = InsiderAppSmartRecommenderOperator.NOT_EQUALS, value = "Nike")
        )
        val custom = SmartRecommenderRequestBuilder.condition(
            ConditionForm(attribute = SmartRecommenderRequestBuilder.CUSTOM_ATTRIBUTE, customAttribute = "material")
        )

        assertEquals(InsiderAppSmartRecommenderAttribute.BRAND, standard.attribute)
        assertEquals(InsiderAppSmartRecommenderOperator.NOT_EQUALS, standard.operator)
        assertEquals(InsiderAppSmartRecommenderAttribute.custom("material"), custom.attribute)
    }

    @Test
    fun conditionValueTypes() {
        fun value(type: ConditionValueType, text: String): Any? =
            SmartRecommenderRequestBuilder.condition(ConditionForm(valueType = type, value = text)).value

        assertEquals("Nike", value(ConditionValueType.STRING, "Nike"))
        assertEquals(10.5, value(ConditionValueType.NUMBER, "10.5"))
        assertEquals(true, value(ConditionValueType.BOOLEAN, "true"))
        assertEquals(false, value(ConditionValueType.BOOLEAN, "x"))
    }

    @Test
    fun unparsableNumberBecomesNaN() {
        val condition = SmartRecommenderRequestBuilder.condition(
            ConditionForm(valueType = ConditionValueType.NUMBER, value = "abc")
        )

        assertTrue((condition.value as Double).isNaN())
    }

    @Test
    fun attributeNamesAreThe26StandardPlusCustom() {
        val standard = SmartRecommenderRequestBuilder.standardAttributes
        val names = SmartRecommenderRequestBuilder.attributeNames

        assertEquals(26, standard.size)
        assertEquals(26, standard.map { it.name }.toSet().size)
        assertEquals(27, names.size)
        assertEquals(standard.map { it.name }, names.dropLast(1))
        assertEquals("custom", names.last())
    }

    @Test
    fun clickMapCarriesTheSdkKeys() {
        val product = ClickableProduct(
            id = "p1", name = "Shoe", categories = listOf("Shoes", "Running"),
            imageUrl = "https://example.com/p1.png", price = 19.9, currency = "TRY", groupCode = "g1"
        )

        assertEquals(
            mapOf<String, Any>(
                "product_id" to "p1",
                "name" to "Shoe",
                "taxonomy" to listOf("Shoes", "Running"),
                "image_url" to "https://example.com/p1.png",
                "price" to 19.9,
                "currency" to "TRY",
                "group_code" to "g1"
            ),
            SmartRecommenderRequestBuilder.clickMap(product)
        )
    }

    @Test
    fun clickMapUsesEmptyImageUrlAndOmitsNullGroupCode() {
        val product = ClickableProduct(
            id = "p1", name = "Shoe", categories = emptyList(),
            imageUrl = null, price = 1.0, currency = "TRY", groupCode = null
        )

        val map = SmartRecommenderRequestBuilder.clickMap(product)

        assertEquals("", map["image_url"])
        assertFalse(map.containsKey("group_code"))
    }

    @Test
    fun callLabelNamesTheCallWithoutClaimingItWasRecorded() {
        assertEquals(
            "called click(referenceId, Map) p1 — the SDK may refuse it, check logcat",
            SmartRecommenderRequestBuilder.callLabel("click(referenceId, Map) p1")
        )
    }
}
