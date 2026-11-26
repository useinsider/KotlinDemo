package com.useinsider.kotlindemo.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.useinsider.kotlindemo.component.InsiderGradientButton
import com.useinsider.kotlindemo.component.ParameterRow
import com.useinsider.kotlindemo.ui.theme.InsiderBeige
import com.useinsider.kotlindemo.ui.theme.InsiderDanger
import com.useinsider.kotlindemo.ui.theme.InsiderInputBg
import com.useinsider.kotlindemo.ui.theme.InsiderInputBorder
import com.useinsider.kotlindemo.ui.theme.InsiderTextDark
import com.useinsider.kotlindemo.viewmodel.CustomEventViewModel

@Composable
public fun CustomEventScreen(
    viewModel: CustomEventViewModel,
    onBack: () -> Unit,
    onEventSent: (String) -> Unit
): Unit {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InsiderBeige)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = InsiderTextDark
                )
            }
            Text(
                text = "Create Custom Event",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InsiderTextDark
            )
        }

        Spacer(Modifier.height(16.dp))

        // Name field
        Text(
            text = "Name",
            fontSize = 14.sp,
            color = InsiderTextDark,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = viewModel.eventName,
            onValueChange = {
                viewModel.eventName = it
                viewModel.nameError = null
            },
            placeholder = { Text("e.g., product_viewed") },
            isError = viewModel.nameError != null,
            supportingText = viewModel.nameError?.let { error -> { Text(error) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InsiderInputBg,
                unfocusedContainerColor = InsiderInputBg,
                focusedBorderColor = InsiderInputBorder,
                unfocusedBorderColor = InsiderInputBorder
            )
        )

        Spacer(Modifier.height(16.dp))

        // Parameters header
        Text(
            text = "Parameters",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = InsiderTextDark,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Parameter rows
        viewModel.parameters.forEachIndexed { index, param ->
            ParameterRow(
                parameter = param,
                onTypeChange = { viewModel.updateParameterType(index, it) },
                onNameChange = { viewModel.updateParameterName(index, it) },
                onValueChange = { viewModel.updateParameterValue(index, it) },
                onDelete = { viewModel.removeParameter(index) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // + Add Parameter
        TextButton(
            onClick = { viewModel.addParameter() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "+ Add Parameter",
                color = InsiderDanger,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(16.dp))

        // Send Event button
        InsiderGradientButton(
            text = "Send Event",
            onClick = {
                viewModel.sendEvent { result ->
                    onEventSent(result)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
    }
}
