package com.useinsider.kotlindemo.action

import com.useinsider.insider.ContentOptimizerDataType
import com.useinsider.insider.Insider
import com.useinsider.insider.InsiderIdentifiers
import com.useinsider.insider.InsiderProduct
import com.useinsider.kotlindemo.model.EventParameter
import com.useinsider.kotlindemo.model.ParameterType
import java.util.Date

public object InsiderActions {

    private val currentUser get() = Insider.Instance.currentUser

    private fun createSampleProduct(): InsiderProduct {
        return Insider.Instance.createNewProduct(
            "productID", "productName", arrayOf("taxonomy"), "imageURL", 1000.5, "USD"
        )
    }

    private fun createSampleIdentifiers(): InsiderIdentifiers {
        return InsiderIdentifiers().apply {
            addEmail("mobile@useinsider.com")
            addPhoneNumber("+901234567")
            addUserID("CRM-ID")
        }
    }

    // -- Custom --

    public fun runCustomAction(callback: (String) -> Unit): Unit {
        callback("Custom action executed")
    }

    // -- Core --

    public fun getInsiderID(callback: (String) -> Unit): Unit {
        val id = Insider.Instance.getInsiderID()
        callback("Insider ID: $id")
    }

    public fun startTrackingGeofence(callback: (String) -> Unit): Unit {
        Insider.Instance.startTrackingGeofence()
        callback("Geofence tracking started")
    }

    // -- Reinit --

    public fun reinitPartnerName(name: String, callback: (String) -> Unit): Unit {
        Insider.Instance.reinitWithPartnerName(name)
        callback("Reinit with: $name")
    }

    // -- Consent --

    public fun setGDPRConsent(enabled: Boolean, callback: (String) -> Unit): Unit {
        Insider.Instance.setGDPRConsent(enabled)
        callback("GDPR Consent: $enabled")
    }

    public fun setMobileAppAccess(enabled: Boolean, callback: (String) -> Unit): Unit {
        Insider.Instance.setMobileAppAccess(enabled)
        callback("Mobile App Access: $enabled")
    }

    public fun enableIpCollection(enabled: Boolean, callback: (String) -> Unit): Unit {
        Insider.Instance.enableIpCollection(enabled)
        callback("IP Collection: $enabled")
    }

    public fun enableCarrierCollection(enabled: Boolean, callback: (String) -> Unit): Unit {
        Insider.Instance.enableCarrierCollection(enabled)
        callback("Carrier Collection: $enabled")
    }

    public fun enableLocationCollection(enabled: Boolean, callback: (String) -> Unit): Unit {
        Insider.Instance.enableLocationCollection(enabled)
        callback("Location Collection: $enabled")
    }

    public fun setEmailOptin(enabled: Boolean, callback: (String) -> Unit): Unit {
        currentUser.setEmailOptin(enabled)
        callback("Email Optin: $enabled")
    }

    public fun setPushOptin(enabled: Boolean, callback: (String) -> Unit): Unit {
        currentUser.setPushOptin(enabled)
        callback("Push Optin: $enabled")
    }

    public fun setLocationOptin(enabled: Boolean, callback: (String) -> Unit): Unit {
        currentUser.setLocationOptin(enabled)
        callback("Location Optin: $enabled")
    }

    public fun setSMSOptin(enabled: Boolean, callback: (String) -> Unit): Unit {
        currentUser.setSMSOptin(enabled)
        callback("SMS Optin: $enabled")
    }

    public fun setWhatsappOptin(enabled: Boolean, callback: (String) -> Unit): Unit {
        currentUser.setWhatsappOptin(enabled)
        callback("WhatsApp Optin: $enabled")
    }

    // -- User --

    public fun login(callback: (String) -> Unit): Unit {
        val identifiers = createSampleIdentifiers()
        currentUser.login(identifiers) { insiderId ->
            callback("Login - Insider ID: $insiderId")
        }
    }

    public fun logout(callback: (String) -> Unit): Unit {
        currentUser.logout()
        callback("User logged out")
    }

    public fun logoutResettingInsiderID(callback: (String) -> Unit): Unit {
        currentUser.logoutResettingInsiderID(null) { insiderId ->
            callback("Logout Reset ID - New ID: $insiderId")
        }
    }

    // -- User Attributes --

    public fun setName(value: String, callback: (String) -> Unit): Unit {
        currentUser.setName(value)
        callback("Name set: $value")
    }

    public fun setSurname(value: String, callback: (String) -> Unit): Unit {
        currentUser.setSurname(value)
        callback("Surname set: $value")
    }

    public fun setEmail(value: String, callback: (String) -> Unit): Unit {
        currentUser.setEmail(value)
        callback("Email set: $value")
    }

    public fun setPhoneNumber(value: String, callback: (String) -> Unit): Unit {
        currentUser.setPhoneNumber(value)
        callback("Phone set: $value")
    }

    public fun setAge(value: String, callback: (String) -> Unit): Unit {
        val age = value.toIntOrNull() ?: return callback("Invalid age")
        currentUser.setAge(age)
        callback("Age set: $age")
    }

    public fun setGender(value: String, callback: (String) -> Unit): Unit {
        currentUser.setGender(mapGender(value))
        callback("Gender set: $value")
    }

    public fun setBirthday(value: String, callback: (String) -> Unit): Unit {
        currentUser.setBirthday(Date())
        callback("Birthday set")
    }

    public fun setLanguage(value: String, callback: (String) -> Unit): Unit {
        currentUser.setLanguage(value)
        callback("Language set: $value")
    }

    public fun setLocale(value: String, callback: (String) -> Unit): Unit {
        currentUser.setLocale(value)
        callback("Locale set: $value")
    }

    public fun setFacebookID(value: String, callback: (String) -> Unit): Unit {
        currentUser.setFacebookID(value)
        callback("Facebook ID set: $value")
    }

    public fun setTwitterID(value: String, callback: (String) -> Unit): Unit {
        currentUser.setTwitterID(value)
        callback("Twitter ID set: $value")
    }

    // -- Inapp --

    public fun enableInAppMessages(callback: (String) -> Unit): Unit {
        Insider.Instance.enableInAppMessages()
        callback("InApp messages enabled")
    }

    public fun disableInAppMessages(callback: (String) -> Unit): Unit {
        Insider.Instance.disableInAppMessages()
        callback("InApp messages disabled")
    }

    // -- Event --

    public fun visitHomePage(callback: (String) -> Unit): Unit {
        Insider.Instance.visitHomePage()
        callback("Visit Home Page")
    }

    public fun visitHomePageWithCustomParams(callback: (String) -> Unit): Unit {
        val params = hashMapOf<String, Any>(
            "source" to "homepage",
            "campaign_id" to "camp_123",
            "page_number" to 1
        )
        Insider.Instance.visitHomePage(params)
        callback("Visit Home Page (Custom Params)")
    }

    public fun visitListingPage(callback: (String) -> Unit): Unit {
        Insider.Instance.visitListingPage(arrayOf("Electronics", "Smartphones", "iPhone"))
        callback("Visit Listing Page")
    }

    public fun visitListingPageWithCustomParams(callback: (String) -> Unit): Unit {
        val params = hashMapOf<String, Any>(
            "page_number" to 1,
            "sort_by" to "price",
            "filter_applied" to true
        )
        Insider.Instance.visitListingPage(
            arrayOf("Electronics", "Smartphones", "iPhone"), params
        )
        callback("Visit Listing Page (Custom Params)")
    }

    public fun visitProductDetailPageWithCustomParams(callback: (String) -> Unit): Unit {
        val product = createSampleProduct()
        val params = hashMapOf<String, Any>(
            "source" to "recommendation",
            "campaign_id" to "camp_456",
            "recommendation_id" to "rec_789"
        )
        Insider.Instance.visitProductDetailPage(product, params)
        callback("Visit Product Page (Custom Params)")
    }

    public fun visitCartPageWithCustomParams(callback: (String) -> Unit): Unit {
        val product = createSampleProduct()
        val params = hashMapOf<String, Any>(
            "cart_total" to 99.99,
            "item_count" to 3,
            "discount_applied" to false
        )
        Insider.Instance.visitCartPage(arrayOf(product), params)
        callback("Visit Cart Page (Custom Params)")
    }

    // -- Product --

    public fun visitProductDetailPage(callback: (String) -> Unit): Unit {
        val product = createSampleProduct()
        Insider.Instance.visitProductDetailPage(product)
        callback("Visit Product Detail Page")
    }

    public fun itemAddedToCart(callback: (String) -> Unit): Unit {
        val product = createSampleProduct()
        Insider.Instance.itemAddedToCart(product)
        callback("Item Added to Cart")
    }

    public fun itemAddedToCartWithCustomParams(callback: (String) -> Unit): Unit {
        val product = createSampleProduct()
        val params = hashMapOf<String, Any>(
            "quantity" to 2,
            "source" to "product_page",
            "variant_id" to "var_123"
        )
        Insider.Instance.itemAddedToCart(product, params)
        callback("Item Added to Cart (Custom Params)")
    }

    public fun itemRemovedFromCart(callback: (String) -> Unit): Unit {
        Insider.Instance.itemRemovedFromCart("productID")
        callback("Item Removed from Cart")
    }

    public fun itemRemovedFromCartWithCustomParams(callback: (String) -> Unit): Unit {
        val params = hashMapOf<String, Any>(
            "reason" to "changed_mind",
            "cart_total_after" to 50.0
        )
        Insider.Instance.itemRemovedFromCart("productID", params)
        callback("Item Removed from Cart (Custom Params)")
    }

    public fun visitCartPage(callback: (String) -> Unit): Unit {
        val product1 = createSampleProduct()
        val product2 = createSampleProduct()
        Insider.Instance.visitCartPage(arrayOf(product1, product2))
        callback("Visit Cart Page")
    }

    public fun itemPurchased(callback: (String) -> Unit): Unit {
        val product = createSampleProduct()
        Insider.Instance.itemPurchased("SALE-12345", product)
        callback("Item Purchased")
    }

    public fun itemPurchasedWithCustomParams(callback: (String) -> Unit): Unit {
        val product = createSampleProduct()
        val params = hashMapOf<String, Any>(
            "payment_method" to "credit_card",
            "discount_applied" to true,
            "shipping_method" to "express"
        )
        Insider.Instance.itemPurchased("SALE-12345", product, params)
        callback("Item Purchased (Custom Params)")
    }

    public fun cartClearedWithCustomParams(callback: (String) -> Unit): Unit {
        val params = hashMapOf<String, Any>(
            "items_count_before" to 5,
            "total_value_before" to 250.0,
            "reason" to "checkout_completed"
        )
        Insider.Instance.cartCleared(params)
        callback("Cart Cleared (Custom Params)")
    }

    // -- Wishlist --

    public fun visitWishlist(callback: (String) -> Unit): Unit {
        val product = createSampleProduct()
        Insider.Instance.visitWishlistPage(arrayOf(product))
        callback("Visit Wishlist")
    }

    public fun visitWishlistWithCustomParams(callback: (String) -> Unit): Unit {
        val product = createSampleProduct()
        val params = hashMapOf<String, Any>(
            "wishlist_total_items" to 3,
            "wishlist_total_value" to 150.0
        )
        Insider.Instance.visitWishlistPage(arrayOf(product), params)
        callback("Visit Wishlist (Custom Params)")
    }

    public fun itemAddedToWishlist(callback: (String) -> Unit): Unit {
        val product = createSampleProduct()
        Insider.Instance.itemAddedToWishlist(product)
        callback("Item Added to Wishlist")
    }

    public fun itemAddedToWishlistWithCustomParams(callback: (String) -> Unit): Unit {
        val product = createSampleProduct()
        val params = hashMapOf<String, Any>(
            "source" to "product_page",
            "recommendation_id" to "rec_123",
            "wishlist_size" to 5
        )
        Insider.Instance.itemAddedToWishlist(product, params)
        callback("Item Added to Wishlist (Custom Params)")
    }

    public fun itemRemovedFromWishlist(callback: (String) -> Unit): Unit {
        Insider.Instance.itemRemovedFromWishlist("productID")
        callback("Item Removed from Wishlist")
    }

    public fun itemRemovedFromWishlistWithCustomParams(callback: (String) -> Unit): Unit {
        val params = hashMapOf<String, Any>(
            "reason" to "purchased",
            "wishlist_size_after" to 2
        )
        Insider.Instance.itemRemovedFromWishlist("productID", params)
        callback("Item Removed from Wishlist (Custom Params)")
    }

    public fun wishlistCleared(callback: (String) -> Unit): Unit {
        Insider.Instance.wishlistCleared()
        callback("Wishlist Cleared")
    }

    public fun wishlistClearedWithCustomParams(callback: (String) -> Unit): Unit {
        val params = hashMapOf<String, Any>(
            "items_count_before" to 5,
            "reason" to "cleanup"
        )
        Insider.Instance.wishlistCleared(params)
        callback("Wishlist Cleared (Custom Params)")
    }

    // -- Content Optimizer --

    public fun getContentStringWithoutCache(name: String, callback: (String) -> Unit): Unit {
        val result = Insider.Instance.getContentStringWithoutCache(
            name, "defaultValue", ContentOptimizerDataType.ELEMENT
        )
        callback("Content String (no cache): $result")
    }

    public fun getContentStringWithName(name: String, callback: (String) -> Unit): Unit {
        val result = Insider.Instance.getContentStringWithName(
            name, "defaultValue", ContentOptimizerDataType.ELEMENT
        )
        callback("Content String: $result")
    }

    public fun getContentBoolWithoutCache(name: String, callback: (String) -> Unit): Unit {
        val result = Insider.Instance.getContentBoolWithoutCache(
            name, true, ContentOptimizerDataType.ELEMENT
        )
        callback("Content Bool (no cache): $result")
    }

    public fun getContentBoolWithName(name: String, callback: (String) -> Unit): Unit {
        val result = Insider.Instance.getContentBoolWithName(
            name, true, ContentOptimizerDataType.ELEMENT
        )
        callback("Content Bool: $result")
    }

    public fun getContentIntWithoutCache(name: String, callback: (String) -> Unit): Unit {
        val result = Insider.Instance.getContentIntWithoutCache(
            name, 42, ContentOptimizerDataType.ELEMENT
        )
        callback("Content Int (no cache): $result")
    }

    public fun getContentIntWithName(name: String, callback: (String) -> Unit): Unit {
        val result = Insider.Instance.getContentIntWithName(
            name, 42, ContentOptimizerDataType.ELEMENT
        )
        callback("Content Int: $result")
    }

    // -- Custom Event --

    public fun tagCustomEvent(name: String, parameters: List<EventParameter>, callback: (String) -> Unit): Unit {
        if (name.isBlank()) {
            callback("Event name is required")
            return
        }
        val event = Insider.Instance.tagEvent(name)
        parameters.forEach { param ->
            if (param.name.isNotBlank()) {
                when (param.type) {
                    ParameterType.STRING -> event.addParameterWithString(param.name, param.value)
                    ParameterType.INTEGER -> event.addParameterWithInt(
                        param.name, coerceInt(param.value)
                    )
                    ParameterType.DOUBLE -> event.addParameterWithDouble(
                        param.name, coerceDouble(param.value)
                    )
                    ParameterType.BOOLEAN -> event.addParameterWithBoolean(
                        param.name, coerceBoolean(param.value)
                    )
                }
            }
        }
        event.build()
        callback("Event sent: $name (${parameters.size} params)")
    }

    // -- Custom User Attributes --

    public fun setCustomAttributes(attributes: List<EventParameter>, callback: (String) -> Unit): Unit {
        attributes.forEach { attr ->
            if (attr.name.isNotBlank()) {
                when (attr.type) {
                    ParameterType.STRING -> currentUser.setCustomAttributeWithString(
                        attr.name, attr.value
                    )
                    ParameterType.INTEGER -> currentUser.setCustomAttributeWithInt(
                        attr.name, coerceInt(attr.value)
                    )
                    ParameterType.DOUBLE -> currentUser.setCustomAttributeWithDouble(
                        attr.name, coerceDouble(attr.value)
                    )
                    ParameterType.BOOLEAN -> currentUser.setCustomAttributeWithBoolean(
                        attr.name, coerceBoolean(attr.value)
                    )
                }
            }
        }
        callback("Custom attributes set (${attributes.size})")
    }
}
