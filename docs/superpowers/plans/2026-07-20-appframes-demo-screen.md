# App Frames Demo Screen — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an App Frames example screen to the Kotlin demo app that demonstrates `InsiderAppFramesView` with a wrap-content drop-in, a fixed-height drop-in, all listener callbacks, and a recommended dismiss-persistence pattern.

**Architecture:** A Jetpack Compose screen (`AppFramesScreen`) backed by an `AppFramesViewModel`, wired into the existing `NavGraph` and reachable from `MainScreen`'s Core section. The SDK's `InsiderAppFramesView` (an Android `View`) is embedded via Compose `AndroidView` interop, one per placement, each with its own listener updating a per-placement status chip. Dismiss state is persisted in `SharedPreferences`.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, `androidx.navigation:navigation-compose`, `androidx.lifecycle` (AndroidViewModel), Insider Android SDK (`com.useinsider.insider.InsiderAppFramesView`), Gradle (Kotlin DSL, AGP 9).

## Global Constraints

- **Worktree:** all demo edits happen in `/Users/bedir.aktas/Documents/development/insider/Kotlin-Demo-appframes-demo` on branch `feature/MOB-27871-appframes-demo`. Never touch the sibling `Kotlin-Demo` checkout.
- **explicitApi():** the `example` module has `explicitApi()` on — every new top-level/public declaration needs an explicit visibility modifier (`public`/`private`) and explicit return type. Match the existing files (e.g. `AppCardsScreen.kt`).
- **Committed partner name stays `"partnername"`** in `example/build.gradle.kts`. `qaautomatin1` is a local-only override applied for the emulator test and reverted before any commit.
- **No binary in git:** the locally-built SDK aar lives at `example/libs/insider-appframes.aar` and MUST be git-ignored.
- **SDK version built from:** `mobileandroid` branch `feature/appframes-action-listener` (insider `versionName 16.0.7`), whose `InsiderAppFramesViewListener` has 6 callbacks.
- **No test framework in this demo:** there is no JUnit/Robolectric setup and we do not add one (YAGNI, matches repo convention). The per-task verification gate is a Gradle compile/build; end-to-end behavior is verified on the emulator in the final task.
- **Theme/components to reuse:** colors `InsiderBeige`, `InsiderOrangeStart`, `InsiderTextDark`, `InsiderTextGray` (`ui/theme/Color.kt`); `Figtree` font (`ui/theme/Type.kt`); `InsiderGradientButton`, `SectionHeader` components.
- **Placements:** `home_page`, `placement_1`, `placement_2`, `placement_3`, `placement_4`. `placement_4` is the fixed-height (200.dp) variant; the rest are wrap-content.

---

## File Structure

**Create (in `example/src/main/java/com/useinsider/kotlindemo/`):**
- `util/DismissedFramesStore.kt` — `SharedPreferences`-backed dismissed-placement set.
- `viewmodel/AppFramesViewModel.kt` — per-placement `FrameStatus` state + dismiss orchestration; also declares the `FrameStatus` sealed interface.
- `screen/AppFramesScreen.kt` — the Compose screen (top bar, placement sections via `AndroidView`, status chips, Reset button).

**Create (build):**
- `example/libs/insider-appframes.aar` — locally-built SDK (git-ignored).

**Modify:**
- `settings.gradle.kts` — add `flatDir { dirs("libs") }` repo.
- `example/build.gradle.kts` — swap `insider.sdk` for the local aar; add two missing transitive deps.
- `.gitignore` — ignore `example/libs/*.aar`.
- `example/src/main/java/com/useinsider/kotlindemo/navigation/NavGraph.kt` — add route + composable, thread `onNavigateToAppFrames`.
- `example/src/main/java/com/useinsider/kotlindemo/screen/MainScreen.kt` — add param + Core-section button.
- `README.md` (repo root) — document the screen.

---

## Task 1: Source and wire the App Frames SDK aar

**Files:**
- Create: `example/libs/insider-appframes.aar` (built artifact, git-ignored)
- Modify: `settings.gradle.kts`
- Modify: `example/build.gradle.kts`
- Modify: `.gitignore`

**Interfaces:**
- Produces: a resolvable `com.useinsider.insider.InsiderAppFramesView` + `InsiderAppFramesViewListener` on the `example` module's compile classpath. All later tasks depend on this.

- [ ] **Step 1: Create an isolated mobileandroid worktree for the SDK branch**

```bash
cd /Users/bedir.aktas/Documents/development/insider/mobileandroid
git worktree add /Users/bedir.aktas/Documents/development/insider/mobileandroid-appframes-build feature/appframes-action-listener
```
Expected: "Preparing worktree … HEAD is now at …". If the branch is already checked out elsewhere, use `git worktree add --detach <path> feature/appframes-action-listener` instead.

- [ ] **Step 2: Build the release aar**

```bash
cd /Users/bedir.aktas/Documents/development/insider/mobileandroid-appframes-build
./gradlew :insider:assembleRelease -x test -x lint
```
Expected: `BUILD SUCCESSFUL`, producing `insider/build/outputs/aar/insider-release.aar`.

- [ ] **Step 3: Verify the aar contains App Frames, then copy it into the demo**

```bash
cd /Users/bedir.aktas/Documents/development/insider/mobileandroid-appframes-build
unzip -p insider/build/outputs/aar/insider-release.aar classes.jar > /tmp/af-classes.jar
unzip -l /tmp/af-classes.jar | grep -i "InsiderAppFramesView"
mkdir -p /Users/bedir.aktas/Documents/development/insider/Kotlin-Demo-appframes-demo/example/libs
cp insider/build/outputs/aar/insider-release.aar \
   /Users/bedir.aktas/Documents/development/insider/Kotlin-Demo-appframes-demo/example/libs/insider-appframes.aar
```
Expected: the grep prints `com/useinsider/insider/InsiderAppFramesView.class` (and `...Listener.class`). If it prints nothing, STOP — the wrong branch was built.

- [ ] **Step 4: Add the flatDir repo to `settings.gradle.kts`**

In `dependencyResolutionManagement { repositories { … } }`, add `flatDir` as the first repository:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        flatDir { dirs("example/libs") }
        google()
        mavenCentral()
        maven { url = uri("https://mobilesdk.useinsider.com/android") }
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}
```

- [ ] **Step 5: Point the `example` module at the local aar + add the two missing transitive deps**

In `example/build.gradle.kts`, replace the line `implementation(libs.insider.sdk)` with the block below (leave `implementation(libs.insider.webview)` and every other dependency exactly as-is):

```kotlin
    //Required
    // App Frames demo: local unpublished SDK build (example/libs/insider-appframes.aar,
    // git-ignored) — it carries the InsiderAppFramesView API not yet in any published release.
    // Swap back to `implementation(libs.insider.sdk)` once an App-Frames version ships.
    implementation(files("libs/insider-appframes.aar"))
    // A bare aar carries no POM, so the SDK's runtime deps are declared here. Most are already
    // listed below; these two are the ones the SDK needs that the demo did not previously declare.
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.android.play:review:2.0.2")
    implementation(libs.insider.webview)
```

- [ ] **Step 6: Git-ignore the local aar**

Append to the repo-root `.gitignore`:

```gitignore

# App Frames demo: locally-built, unpublished Insider SDK aar (not for commit)
example/libs/*.aar
```

- [ ] **Step 7: Verify the demo resolves and compiles against the new SDK**

```bash
cd /Users/bedir.aktas/Documents/development/insider/Kotlin-Demo-appframes-demo
./gradlew :example:compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL` (no App Frames code yet — this only proves the SDK + transitive deps resolve).

- [ ] **Step 8: Commit**

```bash
cd /Users/bedir.aktas/Documents/development/insider/Kotlin-Demo-appframes-demo
git add settings.gradle.kts example/build.gradle.kts .gitignore
git status --short   # confirm example/libs/insider-appframes.aar is NOT staged
git commit -m "build(MOB-27871): wire local App Frames SDK aar into demo"
```
(The pre-commit security gate runs automatically; follow its flow.)

---

## Task 2: State layer — `FrameStatus`, `DismissedFramesStore`, `AppFramesViewModel`

**Files:**
- Create: `example/src/main/java/com/useinsider/kotlindemo/util/DismissedFramesStore.kt`
- Create: `example/src/main/java/com/useinsider/kotlindemo/viewmodel/AppFramesViewModel.kt`

**Interfaces:**
- Consumes: nothing beyond the Android SDK and AndroidX lifecycle.
- Produces:
  - `sealed interface FrameStatus` with objects `Idle`, `Loading`, `Loaded`, `HeightUpdated`, `ActionTriggered`, `Dismissed` and `data class Failed(val message: String)`.
  - `class DismissedFramesStore(context: Context)` with `fun dismissed(): Set<String>`, `fun add(placementId: String)`, `fun clear()`.
  - `class AppFramesViewModel(application: Application) : AndroidViewModel` with `fun statusFor(placementId: String): FrameStatus`, `fun updateStatus(placementId: String, status: FrameStatus)`, `fun isDismissed(placementId: String): Boolean`, `fun persistDismiss(placementId: String)`, `fun resetDismissed()`, and `val dismissed: Set<String>`.

- [ ] **Step 1: Create `DismissedFramesStore.kt`**

```kotlin
package com.useinsider.kotlindemo.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists the set of App Frames placement ids the user has dismissed, so a dismissed frame stays
 * hidden across app relaunches. Plain SharedPreferences — the dismissed set is non-sensitive.
 */
public class DismissedFramesStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    public fun dismissed(): Set<String> =
        // getStringSet's returned set must not be mutated — copy it defensively.
        prefs.getStringSet(KEY_DISMISSED, emptySet())?.toSet() ?: emptySet()

    public fun add(placementId: String): Unit {
        val updated = dismissed().toMutableSet().apply { add(placementId) }
        prefs.edit().putStringSet(KEY_DISMISSED, updated).apply()
    }

    public fun clear(): Unit {
        prefs.edit().remove(KEY_DISMISSED).apply()
    }

    private companion object {
        const val PREFS_NAME: String = "insider_app_frames"
        const val KEY_DISMISSED: String = "dismissed_placements"
    }
}
```

- [ ] **Step 2: Create `AppFramesViewModel.kt`**

```kotlin
package com.useinsider.kotlindemo.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.useinsider.kotlindemo.util.DismissedFramesStore

/** UI status of a single App Frames placement, driven by the SDK view listener callbacks. */
public sealed interface FrameStatus {
    public object Idle : FrameStatus
    public object Loading : FrameStatus
    public object Loaded : FrameStatus
    public object HeightUpdated : FrameStatus
    public object ActionTriggered : FrameStatus
    public object Dismissed : FrameStatus
    public data class Failed(val message: String) : FrameStatus
}

public class AppFramesViewModel(application: Application) : AndroidViewModel(application) {

    private val store: DismissedFramesStore = DismissedFramesStore(application)

    // Per-placement latest status. mutableStateMapOf → reads recompose only the affected chip.
    private val statuses = mutableStateMapOf<String, FrameStatus>()

    public var dismissed: Set<String> by mutableStateOf(store.dismissed())
        private set

    public fun statusFor(placementId: String): FrameStatus =
        statuses[placementId] ?: FrameStatus.Idle

    public fun updateStatus(placementId: String, status: FrameStatus): Unit {
        statuses[placementId] = status
    }

    public fun isDismissed(placementId: String): Boolean = dismissed.contains(placementId)

    /** Recommended pattern: persist the dismiss and drop the frame from the UI. */
    public fun persistDismiss(placementId: String): Unit {
        store.add(placementId)
        dismissed = store.dismissed()
        statuses[placementId] = FrameStatus.Dismissed
    }

    public fun resetDismissed(): Unit {
        store.clear()
        dismissed = store.dismissed()
        statuses.clear()
    }
}
```

- [ ] **Step 3: Verify it compiles**

```bash
cd /Users/bedir.aktas/Documents/development/insider/Kotlin-Demo-appframes-demo
./gradlew :example:compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add example/src/main/java/com/useinsider/kotlindemo/util/DismissedFramesStore.kt \
        example/src/main/java/com/useinsider/kotlindemo/viewmodel/AppFramesViewModel.kt
git commit -m "feat(MOB-27871): add App Frames status + dismiss-persistence state layer"
```

---

## Task 3: `AppFramesScreen` composable

**Files:**
- Create: `example/src/main/java/com/useinsider/kotlindemo/screen/AppFramesScreen.kt`

**Interfaces:**
- Consumes: `AppFramesViewModel`, `FrameStatus` (Task 2); `InsiderAppFramesView`, `InsiderAppFramesViewListener`, `InsiderAppFramesError` (Task 1 SDK); `InsiderGradientButton`, theme colors, `Figtree`.
- Produces: `@Composable fun AppFramesScreen(viewModel: AppFramesViewModel, onBack: () -> Unit)`.

- [ ] **Step 1: Create `AppFramesScreen.kt`**

```kotlin
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
import com.useinsider.kotlindemo.ui.theme.InsiderOrangeStart
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
) {
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
```

- [ ] **Step 2: Verify it compiles**

```bash
cd /Users/bedir.aktas/Documents/development/insider/Kotlin-Demo-appframes-demo
./gradlew :example:compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`. If the compiler reports `onFrameActionTriggered` does not override anything, the built aar is an older 5-callback SDK — rebuild Task 1 from `feature/appframes-action-listener`. If it reports `error.code`/`error.message` unresolved, check the actual getter names with `unzip -p .../insider-appframes.aar classes.jar > /tmp/c.jar && javap -classpath /tmp/c.jar com.useinsider.insider.InsiderAppFramesError`.

- [ ] **Step 3: Commit**

```bash
git add example/src/main/java/com/useinsider/kotlindemo/screen/AppFramesScreen.kt
git commit -m "feat(MOB-27871): add App Frames Compose screen with all callbacks"
```

---

## Task 4: Navigation + MainScreen entry point

**Files:**
- Modify: `example/src/main/java/com/useinsider/kotlindemo/navigation/NavGraph.kt`
- Modify: `example/src/main/java/com/useinsider/kotlindemo/screen/MainScreen.kt`

**Interfaces:**
- Consumes: `AppFramesScreen`, `AppFramesViewModel` (Tasks 2–3).
- Produces: a navigable `Routes.APP_FRAMES` destination; `MainScreen` gains an `onNavigateToAppFrames: () -> Unit` parameter.

- [ ] **Step 1: Edit `NavGraph.kt`**

Add the import:
```kotlin
import com.useinsider.kotlindemo.screen.AppFramesScreen
import com.useinsider.kotlindemo.viewmodel.AppFramesViewModel
```

Add the route constant inside `object Routes`:
```kotlin
    public const val APP_FRAMES: String = "app_frames"
```

In the `MainScreen(...)` call, add the new navigation lambda (alongside `onNavigateToAppCards`):
```kotlin
                onNavigateToAppFrames = { navController.navigate(Routes.APP_FRAMES) }
```

Add the composable destination inside the `NavHost` (after the `Routes.APP_CARDS` block):
```kotlin
        composable(Routes.APP_FRAMES) {
            val appFramesViewModel: AppFramesViewModel = viewModel()
            AppFramesScreen(
                viewModel = appFramesViewModel,
                onBack = { navController.popBackStack() }
            )
        }
```

- [ ] **Step 2: Edit `MainScreen.kt`**

Add the parameter to the `MainScreen` signature (after `onNavigateToAppCards`):
```kotlin
    onNavigateToAppCards: () -> Unit,
    onNavigateToAppFrames: () -> Unit
```

In the `-- Core --` `ButtonGrid`, add the button after `ButtonItem("App Cards") { onNavigateToAppCards() }`:
```kotlin
                    ButtonItem("App Frames") { onNavigateToAppFrames() }
```

- [ ] **Step 3: Build the debug APK**

```bash
cd /Users/bedir.aktas/Documents/development/insider/Kotlin-Demo-appframes-demo
./gradlew :example:assembleDebug
```
Expected: `BUILD SUCCESSFUL`, producing `example/build/outputs/apk/debug/example-debug.apk`.

- [ ] **Step 4: Commit**

```bash
git add example/src/main/java/com/useinsider/kotlindemo/navigation/NavGraph.kt \
        example/src/main/java/com/useinsider/kotlindemo/screen/MainScreen.kt
git commit -m "feat(MOB-27871): wire App Frames screen into navigation and Main menu"
```

---

## Task 5: README documentation

**Files:**
- Modify: `README.md` (repo root)

**Interfaces:** none (docs only).

- [ ] **Step 1: Add an App Frames section to `README.md`**

Locate the section that lists demo screens/features (near the App Cards / Project Structure area) and add:

```markdown
### App Frames

The **App Frames** screen (`screen/AppFramesScreen.kt`) demonstrates integrating the SDK's
`InsiderAppFramesView` in a Jetpack Compose app via `AndroidView` interop. It renders five
placements (`home_page`, `placement_1`–`placement_4`) and shows:

- **Drop-in with `wrap_content` height** — `home_page` and `placement_1`–`placement_3`; the view
  self-sizes to its content.
- **Drop-in with a fixed height** — `placement_4`, constrained to 200dp.
- **Full listener coverage** — one `InsiderAppFramesViewListener` per placement drives a status
  chip, surfacing every callback: `onLoadStarted`, `onLoadFinished`, `onLoadFailed`,
  `onHeightUpdateRequested`, `onFrameActionTriggered`, and `onDismissRequested`.
- **Dismiss persistence (recommended pattern)** — on `onDismissRequested` the placement id is saved
  to `SharedPreferences` (`util/DismissedFramesStore.kt`) and the frame is removed; it stays hidden
  across relaunches. "Reset dismissed frames" clears the store.

Open it from the **Core** section of the main screen ("App Frames").

> The App Frames API ships in a newer Insider SDK build than the placeholder version pinned here;
> point `insider_sdk` at an App-Frames-capable release to run this screen.
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs(MOB-27871): document App Frames demo screen in README"
```

---

## Task 6: Emulator verification (all callbacks, fixed height, dismiss persistence)

**Files:** none committed (local partner override is reverted).

**Interfaces:** none.

- [ ] **Step 1: Confirm an emulator/device is connected**

```bash
adb devices
```
Expected: at least one `device` line. If none, start an emulator (`emulator -list-avds` then `emulator -avd <name> &`) before continuing.

- [ ] **Step 2: Apply the local test partner (NOT committed)**

In `example/build.gradle.kts`, temporarily change `val partnerName = "partnername"` to:
```kotlin
        val partnerName = "qaautomatin1"
```

- [ ] **Step 3: Build, install, launch**

```bash
cd /Users/bedir.aktas/Documents/development/insider/Kotlin-Demo-appframes-demo
./gradlew :example:installDebug
adb shell monkey -p com.useinsider.ecommerce -c android.intent.category.LAUNCHER 1
```
Expected: app launches to the main screen.

- [ ] **Step 4: Verify on-device**

Navigate **Core → App Frames** and confirm:
- Each placement's status chip leaves `Idle` and reaches `Loading…` → `Loaded` (or `Failed — <CODE>` if `qaautomatin1` has no campaign for that placement — still a valid callback demonstration).
- `placement_4` is bounded to ~200dp; the others size to their content.
- If a frame exposes a dismiss control, tapping it hides that placement and shows "Dismissed (persisted)"; kill + relaunch the app (`adb shell am force-stop com.useinsider.ecommerce` then relaunch) and confirm it stays hidden; "Reset dismissed frames" restores it.

Capture logs as evidence:
```bash
adb logcat -d -s AppFramesDemo Insider | tail -50
```

- [ ] **Step 5: Revert the local partner override**

In `example/build.gradle.kts`, change `val partnerName = "qaautomatin1"` back to `"partnername"`.

```bash
cd /Users/bedir.aktas/Documents/development/insider/Kotlin-Demo-appframes-demo
git diff --name-only   # expect NO changes (partner reverted; nothing else uncommitted)
```
Expected: empty output. If `example/build.gradle.kts` still shows as modified, the revert was incomplete — fix it. Do NOT commit `qaautomatin1`.

---

## Self-Review

**Spec coverage:**
- §1 wrap_content drop-in → Task 3 (`home_page`, `placement_1..3`, `WRAP_CONTENT`). ✓
- §1 fixed-height drop-in → Task 3 (`placement_4`, 200dp). ✓
- §1 all callbacks → Task 3 (`frameListener` overrides all 6) + Task 6 verification. ✓
- §1 dismiss-persistence → Task 2 (`DismissedFramesStore`, `persistDismiss`) + Task 3 (`onDismissRequested`, Reset button). ✓
- §2 SDK sourcing (local aar, flatDir, transitive deps, verify class) → Task 1. ✓
- §2 test partner `qaautomatin1` local-only → Task 6 (apply + revert). ✓
- Entry point (MainScreen Core button, NavGraph route) → Task 4. ✓
- README → Task 5. ✓
- Java variant skip → not a task (documented in spec §8). ✓

**Placeholder scan:** no TBD/TODO; every code step shows full content. ✓

**Type consistency:** `FrameStatus` variants (`Idle/Loading/Loaded/HeightUpdated/ActionTriggered/Dismissed/Failed`) defined in Task 2 are exactly those consumed by `StatusChip` in Task 3. `AppFramesViewModel` method names (`statusFor`, `updateStatus`, `isDismissed`, `persistDismiss`, `resetDismissed`, `dismissed`) match between Task 2 (definition) and Task 3 (use). `DismissedFramesStore` (`dismissed/add/clear`) consistent between Tasks 2. Listener method names/params (`onLoadStarted`, `onLoadFinished`, `onLoadFailed(_, InsiderAppFramesError)`, `onHeightUpdateRequested(_, Int)`, `onFrameActionTriggered(_, JSONObject)`, `onDismissRequested`) match the verified SDK interface. ✓
