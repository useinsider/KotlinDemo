package com.useinsider.kotlindemo.screen

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.useinsider.insider.InsiderAppFramesError
import com.useinsider.insider.InsiderAppFramesView
import com.useinsider.insider.InsiderAppFramesViewListener
import com.useinsider.kotlindemo.component.InsiderGradientButton
import com.useinsider.kotlindemo.model.FrameSection
import com.useinsider.kotlindemo.ui.theme.Figtree
import com.useinsider.kotlindemo.ui.theme.InsiderBeige
import com.useinsider.kotlindemo.ui.theme.InsiderDanger
import com.useinsider.kotlindemo.ui.theme.InsiderOrangeStart
import com.useinsider.kotlindemo.ui.theme.InsiderTextDark
import com.useinsider.kotlindemo.ui.theme.InsiderTextGray
import com.useinsider.kotlindemo.viewmodel.AppFramesViewModel
import org.json.JSONObject

private val CardBg = Color.White

@Composable
public fun AppFramesScreen(
    viewModel: AppFramesViewModel,
    onBack: () -> Unit
): Unit {
    val focusManager = LocalFocusManager.current
    var placementInput by remember { mutableStateOf("") }

    val addPlacement: () -> Unit = {
        viewModel.addPlacement(placementInput)
        placementInput = ""
        focusManager.clearFocus()
    }

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
            OutlinedTextField(
                value = placementInput,
                onValueChange = { placementInput = it },
                singleLine = true,
                placeholder = {
                    Text("e.g., home_page", fontFamily = Figtree, color = InsiderTextGray)
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { addPlacement() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            InsiderGradientButton(
                text = "+ Add Placement",
                onClick = addPlacement,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp)
            )

            // key(section.id) so composition identity follows the section across removals —
            // sections are keyed by id, not placementId, and each holds a stateful AndroidView.
            viewModel.sections.forEach { section ->
                key(section.id) {
                    FrameSectionView(
                        section = section,
                        onDelete = { viewModel.remove(section.id) },
                        onAction = { viewModel.showAction(it) }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    val payload = viewModel.pendingActionPayload
    if (payload != null) {
        ActionPayloadDialog(payload = payload, onDismiss = { viewModel.dismissAction() })
    }
}

@Composable
private fun FrameSectionView(
    section: FrameSection,
    onDelete: () -> Unit,
    onAction: (String) -> Unit
): Unit {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: placement id + Detach/Attach + Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = section.placementId,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Figtree,
                    color = InsiderTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { section.toggleAttached() }) {
                    Text(
                        text = if (section.attached) "Detach" else "Attach",
                        fontFamily = Figtree,
                        color = InsiderOrangeStart
                    )
                }
                TextButton(onClick = onDelete) {
                    Text(text = "Delete", fontFamily = Figtree, color = InsiderDanger)
                }
            }

            if (section.attached) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    factory = { ctx ->
                        InsiderAppFramesView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setPlacementId(section.placementId)
                            setAppFramesListener(sectionListener(section, onDelete, onAction))
                        }
                    }
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = section.status,
                fontSize = 13.sp,
                fontFamily = Figtree,
                color = InsiderTextDark
            )
            Text(
                text = section.counters,
                fontSize = 12.sp,
                fontFamily = Figtree,
                color = InsiderTextGray
            )
        }
    }
}

/**
 * One listener per view, capturing its [section]. A template-driven dismiss removes the whole
 * section (the same path as Delete); an action both bumps the counter and surfaces the payload.
 */
private fun sectionListener(
    section: FrameSection,
    onDismiss: () -> Unit,
    onAction: (String) -> Unit
): InsiderAppFramesViewListener = object : InsiderAppFramesViewListener {

    override fun onLoadStarted(view: InsiderAppFramesView) {
        section.onStartLoading()
    }

    override fun onLoadFinished(view: InsiderAppFramesView) {
        section.onFinishLoading()
    }

    override fun onLoadFailed(view: InsiderAppFramesView, error: InsiderAppFramesError) {
        section.onFailed(describeError(error))
    }

    override fun onHeightUpdateRequested(view: InsiderAppFramesView, heightPx: Int) {
        section.onHeightChange(heightPx)
    }

    override fun onFrameActionTriggered(view: InsiderAppFramesView, data: JSONObject) {
        section.onAction()
        onAction(prettyJson(data))
    }

    override fun onDismissRequested(view: InsiderAppFramesView) {
        onDismiss()
    }
}

@Composable
private fun ActionPayloadDialog(payload: String, onDismiss: () -> Unit): Unit {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Frame Action", fontWeight = FontWeight.Bold, fontFamily = Figtree) },
        text = {
            Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                Text(
                    text = payload,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = InsiderTextDark
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = InsiderOrangeStart)
            }
        }
    )
}

/** Names the App Frames error code, appending the SDK message when present. */
private fun describeError(error: InsiderAppFramesError): String {
    val name = error.code.name
    val message = error.message
    return if (message.isNullOrEmpty()) name else "$name — $message"
}

private fun prettyJson(data: JSONObject): String =
    try {
        data.toString(2)
    } catch (_: Exception) {
        data.toString()
    }
