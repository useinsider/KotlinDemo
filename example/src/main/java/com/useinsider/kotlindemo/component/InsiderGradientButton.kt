package com.useinsider.kotlindemo.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.useinsider.kotlindemo.ui.theme.Figtree
import com.useinsider.kotlindemo.ui.theme.InsiderOrangeEnd
import com.useinsider.kotlindemo.ui.theme.InsiderOrangeStart

@Composable
public fun InsiderGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
): Unit {
    val gradient = Brush.horizontalGradient(
        colors = listOf(InsiderOrangeStart, InsiderOrangeEnd)
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .alpha(if (enabled) 1f else 0.5f)
            .background(gradient)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = Figtree,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}
