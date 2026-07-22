package com.useinsider.kotlindemo.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.useinsider.kotlindemo.action.InsiderActions
import com.useinsider.kotlindemo.permission.rememberRuntimePermissionRequester
import com.useinsider.kotlindemo.component.ButtonGrid
import com.useinsider.kotlindemo.component.HeaderBar
import com.useinsider.kotlindemo.component.InputFieldGrid
import com.useinsider.kotlindemo.component.InputFieldItem
import com.useinsider.kotlindemo.component.InsiderGradientButton
import com.useinsider.kotlindemo.component.PrintLabel
import com.useinsider.kotlindemo.component.SectionHeader
import com.useinsider.kotlindemo.component.TextFieldWithSetButton
import com.useinsider.kotlindemo.model.ButtonItem
import com.useinsider.kotlindemo.ui.theme.InsiderBeige
import com.useinsider.kotlindemo.viewmodel.MainViewModel

@Composable
public fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToCustomEvent: () -> Unit,
    onNavigateToCustomAttributes: () -> Unit,
    onNavigateToAppCards: () -> Unit,
    onNavigateToAppFrames: () -> Unit
): Unit {
    val callback: (String) -> Unit = { viewModel.updatePrintLabel(it) }
    val permissions = rememberRuntimePermissionRequester()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InsiderBeige)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        HeaderBar(onTrashClick = { viewModel.clearPrintLabel() })

        // Print Label
        PrintLabel(text = viewModel.printLabelText)

        Column(modifier = Modifier.padding(horizontal = 12.dp)) {

            // -- Custom --
            SectionHeader("Custom")
            InsiderGradientButton(
                text = "Run Custom Action",
                onClick = { InsiderActions.runCustomAction(callback) },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // -- Core --
            SectionHeader("Core")
            ButtonGrid(
                columns = 3,
                buttons = listOf(
                    ButtonItem("Insider ID") { InsiderActions.getInsiderID(callback) },
                    ButtonItem("Start Tracking Geofence") {
                        permissions.withLocationPermission { granted ->
                            if (granted) InsiderActions.startTrackingGeofence(callback)
                            else callback("Geofence: location permission denied")
                        }
                    },
                    ButtonItem("App Cards") { onNavigateToAppCards() },
                    ButtonItem("App Frames") { onNavigateToAppFrames() }
                )
            )
            Spacer(Modifier.height(16.dp))

            // -- Reinit --
            SectionHeader("Reinit")
            TextFieldWithSetButton(
                label = "Reinit",
                value = viewModel.reinitName,
                onValueChange = { viewModel.reinitName = it },
                onSetClick = { InsiderActions.reinitPartnerName(viewModel.reinitName, callback) }
            )
            Spacer(Modifier.height(16.dp))

            // -- Consent --
            SectionHeader("Consent")
            val consentButtons = listOf(
                ButtonItem("GDPR Consent (true)") { InsiderActions.setGDPRConsent(true, callback) },
                ButtonItem("GDPR Consent (false)") { InsiderActions.setGDPRConsent(false, callback) },
                ButtonItem("Mobile App Access (true)") { InsiderActions.setMobileAppAccess(true, callback) },
                ButtonItem("Mobile App Access (false)") { InsiderActions.setMobileAppAccess(false, callback) },
                ButtonItem("IP Collection (true)") { InsiderActions.enableIpCollection(true, callback) },
                ButtonItem("IP Collection (false)") { InsiderActions.enableIpCollection(false, callback) },
                ButtonItem("Carrier Collection (true)") { InsiderActions.enableCarrierCollection(true, callback) },
                ButtonItem("Carrier Collection (false)") { InsiderActions.enableCarrierCollection(false, callback) },
                ButtonItem("Location Collection (true)") { InsiderActions.enableLocationCollection(true, callback) },
                ButtonItem("Location Collection (false)") { InsiderActions.enableLocationCollection(false, callback) },
                ButtonItem("Email Optin (true)") { InsiderActions.setEmailOptin(true, callback) },
                ButtonItem("Email Optin (false)") { InsiderActions.setEmailOptin(false, callback) },
                ButtonItem("Push Optin (true)") { InsiderActions.setPushOptin(true, callback) },
                ButtonItem("Push Optin (false)") { InsiderActions.setPushOptin(false, callback) },
                ButtonItem("Location Optin (true)") { InsiderActions.setLocationOptin(true, callback) },
                ButtonItem("Location Optin (false)") { InsiderActions.setLocationOptin(false, callback) },
                ButtonItem("SMS Optin (true)") { InsiderActions.setSMSOptin(true, callback) },
                ButtonItem("SMS Optin (false)") { InsiderActions.setSMSOptin(false, callback) },
                ButtonItem("WhatsApp Optin (true)") { InsiderActions.setWhatsappOptin(true, callback) },
                ButtonItem("WhatsApp Optin (false)") { InsiderActions.setWhatsappOptin(false, callback) }
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .height(IntrinsicSize.Max)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                consentButtons.forEach { item ->
                    InsiderGradientButton(
                        text = item.text,
                        onClick = item.onClick,
                        modifier = Modifier.width(150.dp).fillMaxHeight()
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // -- User --
            SectionHeader("User")
            ButtonGrid(
                columns = 2,
                buttons = listOf(
                    ButtonItem("Login") { InsiderActions.login(callback) },
                    ButtonItem("Logout") { InsiderActions.logout(callback) },
                    ButtonItem("Logout (Reset ID)") { InsiderActions.logoutResettingInsiderID(callback) },
                    ButtonItem("Custom User Attributes") { onNavigateToCustomAttributes() }
                )
            )
            Spacer(Modifier.height(16.dp))

            // -- User Attributes --
            SectionHeader("User Attributes")
            InputFieldGrid(
                fields = listOf(
                    InputFieldItem("Name", viewModel.userName, { viewModel.userName = it }) {
                        InsiderActions.setName(viewModel.userName, callback)
                    },
                    InputFieldItem("Surname", viewModel.userSurname, { viewModel.userSurname = it }) {
                        InsiderActions.setSurname(viewModel.userSurname, callback)
                    },
                    InputFieldItem("E-mail", viewModel.userEmail, { viewModel.userEmail = it }) {
                        InsiderActions.setEmail(viewModel.userEmail, callback)
                    },
                    InputFieldItem("Phone Number", viewModel.userPhone, { viewModel.userPhone = it }) {
                        InsiderActions.setPhoneNumber(viewModel.userPhone, callback)
                    },
                    InputFieldItem("Age", viewModel.userAge, { viewModel.userAge = it }) {
                        InsiderActions.setAge(viewModel.userAge, callback)
                    },
                    InputFieldItem("Gender", viewModel.userGender, { viewModel.userGender = it }) {
                        InsiderActions.setGender(viewModel.userGender, callback)
                    },
                    InputFieldItem("Birthday", viewModel.userBirthday, { viewModel.userBirthday = it }) {
                        InsiderActions.setBirthday(viewModel.userBirthday, callback)
                    },
                    InputFieldItem("Language", viewModel.userLanguage, { viewModel.userLanguage = it }) {
                        InsiderActions.setLanguage(viewModel.userLanguage, callback)
                    },
                    InputFieldItem("Locale", viewModel.userLocale, { viewModel.userLocale = it }) {
                        InsiderActions.setLocale(viewModel.userLocale, callback)
                    },
                    InputFieldItem("Facebook ID", viewModel.userFacebookID, { viewModel.userFacebookID = it }) {
                        InsiderActions.setFacebookID(viewModel.userFacebookID, callback)
                    },
                    InputFieldItem("Twitter ID", viewModel.userTwitterID, { viewModel.userTwitterID = it }) {
                        InsiderActions.setTwitterID(viewModel.userTwitterID, callback)
                    }
                )
            )
            Spacer(Modifier.height(16.dp))

            // -- Inapp --
            SectionHeader("Inapp")
            ButtonGrid(
                columns = 2,
                buttons = listOf(
                    ButtonItem("Enable Inapp Messages") { InsiderActions.enableInAppMessages(callback) },
                    ButtonItem("Disable Inapp Messages") { InsiderActions.disableInAppMessages(callback) }
                )
            )
            Spacer(Modifier.height(16.dp))

            // -- Event --
            SectionHeader("Event")
            ButtonGrid(
                columns = 3,
                buttons = listOf(
                    ButtonItem("Visit Home Page") { InsiderActions.visitHomePage(callback) },
                    ButtonItem("Visit Homepage (Custom Params)") { InsiderActions.visitHomePageWithCustomParams(callback) },
                    ButtonItem("Visit Listing Page") { InsiderActions.visitListingPage(callback) },
                    ButtonItem("Visit Listing Page (Custom Params)") { InsiderActions.visitListingPageWithCustomParams(callback) },
                    ButtonItem("Visit Product Page (Custom Params)") { InsiderActions.visitProductDetailPageWithCustomParams(callback) },
                    ButtonItem("Visit Cart Page (Custom Params)") { InsiderActions.visitCartPageWithCustomParams(callback) },
                    ButtonItem("Custom Event") { onNavigateToCustomEvent() }
                )
            )
            Spacer(Modifier.height(16.dp))

            // -- Product --
            SectionHeader("Product")
            ButtonGrid(
                columns = 3,
                buttons = listOf(
                    ButtonItem("Visit Product Detail Page") { InsiderActions.visitProductDetailPage(callback) },
                    ButtonItem("Add Item to Cart") { InsiderActions.itemAddedToCart(callback) },
                    ButtonItem("Add to Cart (Custom Params)") { InsiderActions.itemAddedToCartWithCustomParams(callback) },
                    ButtonItem("Remove Item from Cart") { InsiderActions.itemRemovedFromCart(callback) },
                    ButtonItem("Remove from Cart (Custom Params)") { InsiderActions.itemRemovedFromCartWithCustomParams(callback) },
                    ButtonItem("Visit Cart Page") { InsiderActions.visitCartPage(callback) },
                    ButtonItem("Item Purchased") { InsiderActions.itemPurchased(callback) },
                    ButtonItem("Item Purchased (Custom Params)") { InsiderActions.itemPurchasedWithCustomParams(callback) },
                    ButtonItem("Cart Cleared (Custom Params)") { InsiderActions.cartClearedWithCustomParams(callback) }
                )
            )
            Spacer(Modifier.height(16.dp))

            // -- Wishlist --
            SectionHeader("Wishlist")
            ButtonGrid(
                columns = 3,
                buttons = listOf(
                    ButtonItem("Visit Wishlist") { InsiderActions.visitWishlist(callback) },
                    ButtonItem("Visit Wishlist (Custom Params)") { InsiderActions.visitWishlistWithCustomParams(callback) },
                    ButtonItem("Add Item to Wishlist") { InsiderActions.itemAddedToWishlist(callback) },
                    ButtonItem("Add to Wishlist (Custom Params)") { InsiderActions.itemAddedToWishlistWithCustomParams(callback) },
                    ButtonItem("Remove Item from Wishlist") { InsiderActions.itemRemovedFromWishlist(callback) },
                    ButtonItem("Remove from Wishlist (Custom Params)") { InsiderActions.itemRemovedFromWishlistWithCustomParams(callback) },
                    ButtonItem("Clear Wishlist") { InsiderActions.wishlistCleared(callback) },
                    ButtonItem("Wishlist Cleared (Custom Params)") { InsiderActions.wishlistClearedWithCustomParams(callback) }
                )
            )
            Spacer(Modifier.height(16.dp))

            // -- Content Optimizer --
            SectionHeader("Content Optimizer")
            InputFieldGrid(
                fields = listOf(
                    InputFieldItem("Content String", viewModel.contentString, { viewModel.contentString = it }) {
                        InsiderActions.getContentStringWithoutCache(viewModel.contentString, callback)
                    },
                    InputFieldItem("Content String (Cache)", viewModel.contentStringCache, { viewModel.contentStringCache = it }) {
                        InsiderActions.getContentStringWithName(viewModel.contentStringCache, callback)
                    },
                    InputFieldItem("Content Bool", viewModel.contentBool, { viewModel.contentBool = it }) {
                        InsiderActions.getContentBoolWithoutCache(viewModel.contentBool, callback)
                    },
                    InputFieldItem("Content Bool (Cache)", viewModel.contentBoolCache, { viewModel.contentBoolCache = it }) {
                        InsiderActions.getContentBoolWithName(viewModel.contentBoolCache, callback)
                    },
                    InputFieldItem("Content Int", viewModel.contentInt, { viewModel.contentInt = it }) {
                        InsiderActions.getContentIntWithoutCache(viewModel.contentInt, callback)
                    },
                    InputFieldItem("Content Int (Cache)", viewModel.contentIntCache, { viewModel.contentIntCache = it }) {
                        InsiderActions.getContentIntWithName(viewModel.contentIntCache, callback)
                    }
                )
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
