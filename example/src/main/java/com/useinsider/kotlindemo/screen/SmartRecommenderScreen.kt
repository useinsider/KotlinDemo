package com.useinsider.kotlindemo.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.useinsider.insider.InsiderAppSmartRecommenderException
import com.useinsider.insider.InsiderAppSmartRecommenderOperator
import com.useinsider.insider.InsiderAppSmartRecommenderProduct
import com.useinsider.insider.InsiderAppSmartRecommenderResult
import com.useinsider.kotlindemo.action.SmartRecommenderRequestBuilder
import com.useinsider.kotlindemo.component.InsiderGradientButton
import com.useinsider.kotlindemo.component.SectionHeader
import com.useinsider.kotlindemo.model.ConditionForm
import com.useinsider.kotlindemo.model.ConditionValueType
import com.useinsider.kotlindemo.model.MapEntryForm
import com.useinsider.kotlindemo.model.MapValueType
import com.useinsider.kotlindemo.ui.theme.Figtree
import com.useinsider.kotlindemo.ui.theme.InsiderBeige
import com.useinsider.kotlindemo.ui.theme.InsiderDanger
import com.useinsider.kotlindemo.ui.theme.InsiderInputBg
import com.useinsider.kotlindemo.ui.theme.InsiderInputBorder
import com.useinsider.kotlindemo.ui.theme.InsiderOrangeStart
import com.useinsider.kotlindemo.ui.theme.InsiderTextDark
import com.useinsider.kotlindemo.ui.theme.InsiderTextGray
import com.useinsider.kotlindemo.viewmodel.SmartRecommenderViewModel

@Composable
public fun SmartRecommenderScreen(
    viewModel: SmartRecommenderViewModel,
    onBack: () -> Unit
): Unit {
    val form = viewModel.form

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InsiderBeige)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("sr_back")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = InsiderTextDark
                )
            }
            Text(
                text = "Smart Recommender",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Figtree,
                color = InsiderTextDark
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
                .padding(bottom = 24.dp)
        ) {
            SectionHeader("Inputs")
            Field("Reference ID", form.referenceId, "sr_reference_id") { value ->
                viewModel.updateForm { it.copy(referenceId = value) }
            }
            Field("Locale (empty: overload without locale)", form.locale, "sr_locale") { value ->
                viewModel.updateForm { it.copy(locale = value) }
            }
            Field("Currency (the SDK requires it, e.g. TRY)", form.currency, "sr_currency") { value ->
                viewModel.updateForm { it.copy(currency = value) }
            }
            Field("Identifier: Email", form.email, "sr_email") { value ->
                viewModel.updateForm { it.copy(email = value) }
            }
            Field("Identifier: Phone Number", form.phoneNumber, "sr_phone_number") { value ->
                viewModel.updateForm { it.copy(phoneNumber = value) }
            }
            Field("Identifier: User ID", form.userId, "sr_user_id") { value ->
                viewModel.updateForm { it.copy(userId = value) }
            }
            Field("Custom Identifier Key", form.customIdentifierKey, "sr_custom_identifier_key") { value ->
                viewModel.updateForm { it.copy(customIdentifierKey = value) }
            }
            Field("Custom Identifier Value", form.customIdentifierValue, "sr_custom_identifier_value") { value ->
                viewModel.updateForm { it.copy(customIdentifierValue = value) }
            }

            SectionHeader("Filters", modifier = Modifier.padding(top = 8.dp))
            FilterSwitch("ProductsFilter", form.productsFilterEnabled, "sr_filter_products") { enabled ->
                viewModel.updateForm { it.copy(productsFilterEnabled = enabled) }
            }
            if (form.productsFilterEnabled) {
                Field("Product IDs (comma separated)", form.productIds, "sr_product_ids") { value ->
                    viewModel.updateForm { it.copy(productIds = value) }
                }
            }

            FilterSwitch("CategoriesFilter", form.categoriesFilterEnabled, "sr_filter_categories") { enabled ->
                viewModel.updateForm { it.copy(categoriesFilterEnabled = enabled) }
            }
            if (form.categoriesFilterEnabled) {
                Field("Categories (comma separated)", form.categories, "sr_categories") { value ->
                    viewModel.updateForm { it.copy(categories = value) }
                }
            }

            FilterSwitch("ProductFilter", form.productFilterEnabled, "sr_filter_product") { enabled ->
                viewModel.updateForm { it.copy(productFilterEnabled = enabled) }
            }
            if (form.productFilterEnabled) {
                val product = form.product

                Field("Product ID", product.id, "sr_product_id") { value ->
                    viewModel.updateForm { it.copy(product = it.product.copy(id = value)) }
                }
                Field("Product Name", product.name, "sr_product_name") { value ->
                    viewModel.updateForm { it.copy(product = it.product.copy(name = value)) }
                }
                Field("Product Taxonomy (comma separated)", product.taxonomy, "sr_product_taxonomy") { value ->
                    viewModel.updateForm { it.copy(product = it.product.copy(taxonomy = value)) }
                }
                Field("Product Image URL", product.imageUrl, "sr_product_image_url") { value ->
                    viewModel.updateForm { it.copy(product = it.product.copy(imageUrl = value)) }
                }
                Field("Product Price", product.price, "sr_product_price") { value ->
                    viewModel.updateForm { it.copy(product = it.product.copy(price = value)) }
                }
                Field("Product Currency", product.currency, "sr_product_currency") { value ->
                    viewModel.updateForm { it.copy(product = it.product.copy(currency = value)) }
                }
            }

            FilterSwitch("AttributesFilter", form.attributesFilterEnabled, "sr_filter_attributes") { enabled ->
                viewModel.updateForm { it.copy(attributesFilterEnabled = enabled) }
            }
            if (form.attributesFilterEnabled) {
                form.conditions.forEachIndexed { index, condition ->
                    ConditionRow(
                        index = index,
                        condition = condition,
                        onChange = { transform -> viewModel.updateCondition(index, transform) },
                        onRemove = { viewModel.removeCondition(index) }
                    )
                }
                InsiderGradientButton(
                    text = "Add condition",
                    onClick = { viewModel.addCondition() },
                    modifier = Modifier.testTag("sr_add_condition")
                )
            }

            FilterSwitch("Partner filter (custom field)", form.partnerFilterEnabled, "sr_filter_partner") { enabled ->
                viewModel.updateForm { it.copy(partnerFilterEnabled = enabled) }
            }
            if (form.partnerFilterEnabled) {
                Field("Request field name", form.partnerKey, "sr_partner_key") { value ->
                    viewModel.updateForm { it.copy(partnerKey = value) }
                }
                Field("Request field value", form.partnerValue, "sr_partner_value") { value ->
                    viewModel.updateForm { it.copy(partnerValue = value) }
                }
            }

            SectionHeader("Consent", modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                InsiderGradientButton(
                    text = "GDPR opt-in",
                    onClick = { viewModel.setGdprConsent(true) },
                    modifier = Modifier.weight(1f).testTag("sr_gdpr_opt_in")
                )
                InsiderGradientButton(
                    text = "GDPR opt-out",
                    onClick = { viewModel.setGdprConsent(false) },
                    modifier = Modifier.weight(1f).testTag("sr_gdpr_opt_out")
                )
            }

            FilterSwitch("Auto view on FILLED result", viewModel.autoView, "sr_auto_view") { enabled ->
                viewModel.updateAutoView(enabled)
            }

            InsiderGradientButton(
                text = "Get Products",
                onClick = { viewModel.getProducts() },
                enabled = !viewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("sr_get_products")
            )
            if (viewModel.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = InsiderOrangeStart,
                        modifier = Modifier.testTag("sr_loading")
                    )
                }
            }
            viewModel.lastCall?.let { InfoLine("call", it, "sr_last_call") }
            viewModel.lastAction?.let { InfoLine("last action", it, "sr_last_action") }

            viewModel.error?.let { ErrorSection(it) }
            viewModel.result?.let { result ->
                ResultSection(result)
                ProductsSection(
                    products = result.products,
                    selectedIndices = viewModel.selectedIndices,
                    onToggleSelected = { viewModel.toggleSelected(it) },
                    onClickProduct = { viewModel.clickWithProduct(it) },
                    onClickMap = { viewModel.clickWithMap(it) }
                )
            }

            ManualSection(viewModel)
        }
    }
}

@Composable
private fun ResultSection(result: InsiderAppSmartRecommenderResult) {
    val context = result.context

    SectionHeader("Result", modifier = Modifier.padding(top = 8.dp))
    InfoLine("outcome", result.outcome.name, "sr_outcome")
    result.reason?.let { InfoLine("reason", it, "sr_reason") }
    InfoLine("request_id", context.requestId, "sr_request_id")
    InfoLine("reference_id", context.referenceId, "sr_context_reference_id")
    InfoLine("campaign_id", context.campaignId.toString(), "sr_campaign_id")
    InfoLine("variant_id", context.variantId.toString(), "sr_variant_id")
    InfoLine("is_control_group", context.isControlGroup.toString(), "sr_is_control_group")
    InfoLine("locale", context.locale.toString(), "sr_context_locale")
    InfoLine("currency", context.currency.toString(), "sr_context_currency")
}

@Composable
private fun ErrorSection(error: InsiderAppSmartRecommenderException) {
    SectionHeader("Error", modifier = Modifier.padding(top = 8.dp))
    InfoLine("code", error.code.name, "sr_error_code", color = InsiderDanger)
    InfoLine("message", error.message.toString(), "sr_error_message")
    InfoLine("request_id", error.requestId, "sr_error_request_id")
    error.field?.let { InfoLine("field", it, "sr_error_field") }
}

@Composable
private fun ProductsSection(
    products: List<InsiderAppSmartRecommenderProduct>,
    selectedIndices: Set<Int>,
    onToggleSelected: (Int) -> Unit,
    onClickProduct: (InsiderAppSmartRecommenderProduct) -> Unit,
    onClickMap: (InsiderAppSmartRecommenderProduct) -> Unit
) {
    SectionHeader("Products (${products.size})", modifier = Modifier.padding(top = 8.dp))
    products.forEachIndexed { index, product ->
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(10.dp)
                .testTag("sr_product_$index")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(InsiderInputBg)
                )
                Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(
                        text = product.name,
                        color = InsiderTextDark,
                        fontFamily = Figtree,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${product.price} ${product.currency}",
                        color = InsiderTextDark,
                        fontFamily = Figtree,
                        fontSize = 13.sp
                    )
                    Text(text = product.id, color = InsiderTextGray, fontFamily = Figtree, fontSize = 12.sp)
                }
                Checkbox(
                    checked = index in selectedIndices,
                    onCheckedChange = { onToggleSelected(index) },
                    modifier = Modifier.testTag("sr_select_product_$index")
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                InsiderGradientButton(
                    text = "Click (Product)",
                    onClick = { onClickProduct(product) },
                    modifier = Modifier.weight(1f).testTag("sr_click_product_$index")
                )
                InsiderGradientButton(
                    text = "Click (Map)",
                    onClick = { onClickMap(product) },
                    modifier = Modifier.weight(1f).testTag("sr_click_map_$index")
                )
            }
        }
    }
}

@Composable
private fun ManualSection(viewModel: SmartRecommenderViewModel) {
    val selectedCount = viewModel.selectedIndices.size

    SectionHeader("Manual view / click", modifier = Modifier.padding(top = 8.dp))
    Text(
        text = "Select products with the checkboxes above. Nothing selected sends view with an empty list.",
        color = InsiderTextGray,
        fontFamily = Figtree,
        fontSize = 12.sp
    )
    InsiderGradientButton(
        text = "View selected ($selectedCount)",
        onClick = { viewModel.viewSelected() },
        modifier = Modifier.fillMaxWidth().testTag("sr_view_selected")
    )
    Field("Reference ID for manual click", viewModel.manualReferenceId, "sr_manual_reference_id") { value ->
        viewModel.updateManualReferenceId(value)
    }
    InsiderGradientButton(
        text = "Click first selected (Product)",
        onClick = { viewModel.clickSelectedWithManualReference() },
        enabled = selectedCount > 0,
        modifier = Modifier.fillMaxWidth().testTag("sr_click_selected_product")
    )

    Text(
        text = "Click map",
        color = InsiderTextDark,
        fontFamily = Figtree,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        InsiderGradientButton(
            text = "Fill from selected",
            onClick = { viewModel.fillMapFromSelected() },
            enabled = selectedCount > 0,
            modifier = Modifier.weight(1f).testTag("sr_map_fill")
        )
        InsiderGradientButton(
            text = "Add key",
            onClick = { viewModel.addMapEntry() },
            modifier = Modifier.weight(1f).testTag("sr_map_add")
        )
    }
    viewModel.mapEntries.forEachIndexed { index, entry ->
        MapEntryRow(
            index = index,
            entry = entry,
            onChange = { transform -> viewModel.updateMapEntry(index, transform) },
            onRemove = { viewModel.removeMapEntry(index) }
        )
    }
    InsiderGradientButton(
        text = "Click (Map above)",
        onClick = { viewModel.clickWithManualMap() },
        modifier = Modifier.fillMaxWidth().testTag("sr_click_manual_map")
    )
}

@Composable
private fun MapEntryRow(
    index: Int,
    entry: MapEntryForm,
    onChange: ((MapEntryForm) -> MapEntryForm) -> Unit,
    onRemove: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, InsiderInputBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                Field("Key", entry.key, "sr_map_key_$index") { value -> onChange { it.copy(key = value) } }
            }
            IconButton(onClick = onRemove, modifier = Modifier.testTag("sr_map_remove_$index")) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Remove key", tint = InsiderDanger)
            }
        }
        Dropdown(
            selected = entry.type.name,
            options = MapValueType.entries.map { it.name },
            tag = "sr_map_type_$index"
        ) { value -> onChange { it.copy(type = MapValueType.valueOf(value)) } }
        Field("Value (LIST: comma separated)", entry.value, "sr_map_value_$index") { value ->
            onChange { it.copy(value = value) }
        }
    }
}

@Composable
private fun ConditionRow(
    index: Int,
    condition: ConditionForm,
    onChange: ((ConditionForm) -> ConditionForm) -> Unit,
    onRemove: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, InsiderInputBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Dropdown(
                selected = condition.attribute,
                options = SmartRecommenderRequestBuilder.attributeNames,
                tag = "sr_condition_attribute_$index",
                modifier = Modifier.weight(1f)
            ) { value -> onChange { it.copy(attribute = value) } }
            IconButton(onClick = onRemove, modifier = Modifier.testTag("sr_condition_remove_$index")) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = "Remove condition", tint = InsiderDanger)
            }
        }
        if (condition.attribute == SmartRecommenderRequestBuilder.CUSTOM_ATTRIBUTE) {
            Field("Custom attribute name", condition.customAttribute, "sr_condition_custom_$index") { value ->
                onChange { it.copy(customAttribute = value) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Dropdown(
                selected = condition.operator.name,
                options = InsiderAppSmartRecommenderOperator.entries.map { it.name },
                tag = "sr_condition_operator_$index",
                modifier = Modifier.weight(1f)
            ) { value -> onChange { it.copy(operator = InsiderAppSmartRecommenderOperator.valueOf(value)) } }
            Dropdown(
                selected = condition.valueType.name,
                options = ConditionValueType.entries.map { it.name },
                tag = "sr_condition_type_$index",
                modifier = Modifier.weight(1f)
            ) { value -> onChange { it.copy(valueType = ConditionValueType.valueOf(value)) } }
        }
        Field("Value", condition.value, "sr_condition_value_$index") { value ->
            onChange { it.copy(value = value) }
        }
    }
}

@Composable
private fun Dropdown(
    selected: String,
    options: List<String>,
    tag: String,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Text(
            text = "$selected ▾",
            color = InsiderTextDark,
            fontFamily = Figtree,
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(InsiderInputBg)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 12.dp)
                .testTag(tag)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, tag: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = InsiderInputBg,
            unfocusedContainerColor = InsiderInputBg,
            focusedBorderColor = InsiderInputBorder,
            unfocusedBorderColor = InsiderInputBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    )
}

@Composable
private fun FilterSwitch(label: String, checked: Boolean, tag: String, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = InsiderTextDark,
            fontFamily = Figtree,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.testTag(tag))
    }
}

@Composable
private fun InfoLine(label: String, value: String, tag: String, color: Color = InsiderTextDark) {
    Text(
        text = "$label: $value",
        color = color,
        fontFamily = Figtree,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    )
}
