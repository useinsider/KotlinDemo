package com.useinsider.kotlindemo.screen

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.useinsider.insider.InsiderAppFramesError
import com.useinsider.insider.InsiderAppFramesView
import com.useinsider.insider.InsiderAppFramesViewListener
import com.useinsider.kotlindemo.component.InsiderGradientButton
import com.useinsider.kotlindemo.ui.theme.Figtree
import com.useinsider.kotlindemo.ui.theme.InsiderBeige
import com.useinsider.kotlindemo.ui.theme.InsiderTextDark
import com.useinsider.kotlindemo.ui.theme.InsiderTextGray
import com.useinsider.kotlindemo.viewmodel.AppFramesViewModel
import com.useinsider.kotlindemo.viewmodel.FrameStatus
import org.json.JSONObject

private const val LOG_TAG: String = "AppFramesDemo"

/** A placement to render; [fixedHeightDp] non-null → the fixed-height drop-in variant. */
private data class PlacementSpec(val placementId: String, val fixedHeightDp: Int?)

private val Placements: List<PlacementSpec> = listOf(
    PlacementSpec("home_page", null),
    PlacementSpec("placement_1", null),
    PlacementSpec("placement_2", null),
    PlacementSpec("placement_3", null),
    PlacementSpec("placement_4", 200), // fixed-height drop-in
)

private val StatusIdle = Color(0xFF9CA3AF)
private val StatusLoading = Color(0xFFFF6B35)
private val StatusOk = Color(0xFF2E9E5B)
private val StatusFailed = Color(0xFFD32F2F)
private val CardBg = Color.White

@Composable
public fun AppFramesScreen(
    viewModel: AppFramesViewModel,
    onBack: () -> Unit
): Unit {
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
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = InsiderTextDark
                )
            }
            Text(
                text = "App Frames",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Figtree,
                color = InsiderTextDark
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            InsiderGradientButton(
                text = "Reset dismissed frames",
                onClick = { viewModel.resetDismissed() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp)
            )

            Placements.forEach { spec ->
                PlacementSection(spec = spec, viewModel = viewModel)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlacementSection(
    spec: PlacementSpec,
    viewModel: AppFramesViewModel
): Unit {
    val heightLabel = if (spec.fixedHeightDp != null) "fixed ${spec.fixedHeightDp}dp" else "wrap_content"

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = "Placement: ${spec.placementId}  ($heightLabel)",
            fontSize = 13.sp,
            fontFamily = Figtree,
            fontWeight = FontWeight.SemiBold,
            color = InsiderTextGray
        )
        Spacer(Modifier.height(6.dp))

        if (viewModel.isDismissed(spec.placementId)) {
            DismissedPlaceholder()
        } else {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val frameModifier = if (spec.fixedHeightDp != null) {
                    Modifier.fillMaxWidth().height(spec.fixedHeightDp.dp)
                } else {
                    Modifier.fillMaxWidth()
                }
                AndroidView(
                    modifier = frameModifier,
                    factory = { ctx ->
                        InsiderAppFramesView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                if (spec.fixedHeightDp != null) {
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                } else {
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                }
                            )
                            setPlacementId(spec.placementId)
                            setAppFramesListener(
                                frameListener(spec.placementId, viewModel)
                            )
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        StatusChip(status = viewModel.statusFor(spec.placementId))
    }
}

/** One listener per view; captures the placement id so callbacks update the right chip. */
private fun frameListener(
    placementId: String,
    viewModel: AppFramesViewModel
): InsiderAppFramesViewListener = object : InsiderAppFramesViewListener {

    override fun onLoadStarted(view: InsiderAppFramesView) {
        viewModel.updateStatus(placementId, FrameStatus.Loading)
    }

    override fun onLoadFinished(view: InsiderAppFramesView) {
        viewModel.updateStatus(placementId, FrameStatus.Loaded)
    }

    override fun onLoadFailed(view: InsiderAppFramesView, error: InsiderAppFramesError) {
        val code = error.code.name
        val message = error.message
        val text = if (message.isNullOrEmpty()) code else "$code: $message"
        viewModel.updateStatus(placementId, FrameStatus.Failed(text))
    }

    override fun onHeightUpdateRequested(view: InsiderAppFramesView, heightPx: Int) {
        viewModel.updateStatus(placementId, FrameStatus.HeightUpdated)
    }

    override fun onFrameActionTriggered(view: InsiderAppFramesView, data: JSONObject) {
        Log.d(LOG_TAG, "Frame action ($placementId): $data")
        viewModel.updateStatus(placementId, FrameStatus.ActionTriggered)
    }

    override fun onDismissRequested(view: InsiderAppFramesView) {
        // Recommended pattern: persist and drop the frame; recomposition removes the AndroidView.
        viewModel.persistDismiss(placementId)
    }
}

@Composable
private fun DismissedPlaceholder(): Unit {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEDEDED))
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Dismissed (persisted) — use Reset to restore",
            fontSize = 13.sp,
            fontFamily = Figtree,
            color = InsiderTextGray
        )
    }
}

@Composable
private fun StatusChip(status: FrameStatus): Unit {
    val (label, color) = when (status) {
        FrameStatus.Idle -> "Idle" to StatusIdle
        FrameStatus.Loading -> "Loading…" to StatusLoading
        FrameStatus.Loaded -> "Loaded" to StatusOk
        FrameStatus.HeightUpdated -> "Height updated" to StatusOk
        FrameStatus.ActionTriggered -> "Action triggered" to StatusOk
        FrameStatus.Dismissed -> "Dismissed" to StatusLoading
        is FrameStatus.Failed -> "Failed — ${status.message}" to StatusFailed
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontFamily = Figtree,
            color = InsiderTextDark
        )
    }
}
