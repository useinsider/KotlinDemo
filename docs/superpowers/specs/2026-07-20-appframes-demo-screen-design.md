# App Frames Demo Screen — Design Spec

- **Ticket:** MOB-27871 (Android sample app — App Frames example)
- **Repo:** `Kotlin-Demo` (partner-facing Insider Android SDK demo)
- **Branch / worktree:** `feature/MOB-27871-appframes-demo` (worktree `Kotlin-Demo-appframes-demo`, off `origin/main`)
- **Date:** 2026-07-20

## 1. Goal

Add an **App Frames** example screen to the Kotlin demo app, demonstrating integration of the
Insider SDK's `InsiderAppFramesView` for partners. The screen must cover:

1. Drop-in `InsiderAppFramesView` with `wrap_content` height.
2. Drop-in with a fixed height.
3. A listener implementation exercising **every** callback.
4. A recommended **dismiss-persistence** pattern.

Plus: update the demo README, and skip the Java variant (no Java sample exists here).

### Acceptance criteria

- Sample app builds and runs with the new screen.
- All 5 `InsiderAppFramesViewListener` callbacks are demonstrated.
- Both a `wrap_content` and a fixed-height drop-in are shown.
- Dismiss persists across app relaunch, with a reset affordance.
- README documents the new screen.

## 2. Context & constraints

- **The demo is 100% Jetpack Compose** (Screens + `NavGraph` + ViewModels + `AndroidView` where a
  platform `View` is needed). The Android SDK reference sample (`ActivityAppFrames.java`, an XML
  Activity) is a *behavioral* reference only — it is **not** copied verbatim. The closest local
  precedent is `screen/AppCardsScreen.kt`, which this screen mirrors.
- `explicitApi()` is enabled in the `example` module — new public declarations need explicit
  visibility/return types (follow existing files).
- Theme/components to reuse: `InsiderBeige`, `InsiderOrangeStart`, `InsiderTextDark`,
  `InsiderTextGray`, `Figtree`, `InsiderGradientButton`, `SectionHeader`.

### SDK dependency (blocker resolved)

The pinned `com.useinsider:insider:16.1.0-rc1` (RC S3 repo) contains **no** App Frames classes
(verified: the aar has zero `Frame` classes and no `placementId` attr). App Frames exists only as
source in the `mobileandroid` App Frames branches. There is **no** maven-publish task on the insider
module and **no `mvn`** on the machine, so `publishToMavenLocal` / `install:install-file` are not
available. Resolution — consume a **locally-built aar** via a Gradle `flatDir` repo:

- Build the `insider` aar from `mobileandroid` branch **`feature/appframes-action-listener`**
  (versionName `16.0.7`; carries the final 6-callback `InsiderAppFramesViewListener`) in an isolated
  worktree: `./gradlew :insider:assembleRelease`.
- Copy the produced `insider/build/outputs/aar/insider-release.aar` into the demo worktree at
  `example/libs/insider-appframes.aar` (git-ignored — a local, unpublished build).
- In the demo worktree: add a `flatDir { dirs("libs") }` repo to `settings.gradle.kts`
  `dependencyResolutionManagement`, keep the RC repo (needed for `insiderwebview:1.1.0-rc1`), replace
  `implementation(libs.insider.sdk)` with `implementation(files("libs/insider-appframes.aar"))`, and
  **declare the SDK's runtime transitive deps manually** (a bare aar carries no POM). Main already
  declares most; add the two missing ones: `androidx.legacy:legacy-support-v4:1.0.0` and
  `com.google.android.play:review:2.0.2`. Full runtime set the SDK needs: legacy-support-v4,
  lifecycle-process, work-runtime, security-crypto, firebase-messaging, play-services-location,
  play:review, huawei push/ads/location, webkit.
- **Verify** the aar exposes `com.useinsider.insider.InsiderAppFramesView` (`unzip -l` the
  `classes.jar`) before wiring UI.
- The `example/build.gradle.kts` dependency change is committed with a comment explaining the aar is
  a local unpublished App Frames build; reviewers/CI cannot compile until the SDK ships a published
  App-Frames version (then the `files(...)` line is swapped back to a pinned `libs.insider.sdk`).

### Test partner

Emulator testing uses partner **`qaautomatin1`** (placements `home_page`, `placement_1..4` assumed
configured there). This is set **locally only** in the worktree build config — the **committed**
code keeps the `"partnername"` placeholder, per the repo convention (`chore: reset demo partner
name to placeholder`).

## 3. Public SDK API used

From `com.useinsider.insider`:

```
final class InsiderAppFramesView extends FrameLayout {
    InsiderAppFramesView(Context)
    InsiderAppFramesView(Context, AttributeSet)
    InsiderAppFramesView(Context, AttributeSet, int)
    void setPlacementId(@Nullable String)     // XML: app:placementId
    @Nullable String getPlacementId()
    void setAppFramesListener(@Nullable InsiderAppFramesViewListener)
}

interface InsiderAppFramesViewListener {      // callbacks on main thread
    void onLoadStarted(InsiderAppFramesView view)
    void onLoadFinished(InsiderAppFramesView view)
    void onLoadFailed(InsiderAppFramesView view, InsiderAppFramesError error)
    void onHeightUpdateRequested(InsiderAppFramesView view, int heightPx)
    void onDismissRequested(InsiderAppFramesView view)
    default void onFrameActionTriggered(InsiderAppFramesView view, JSONObject data)  // optional
}

final class InsiderAppFramesError extends Exception {
    InsiderAppFramesErrorCode getCode()       // UNKNOWN, NO_FRAME_FOR_PLACEMENT,
    int getDismissCode()                      //   HTML_LOAD_FAILED, PAGE_REPORTED_ERROR,
    String getMessage()                       //   PLACEMENTS_FETCH_FAILED
}
```

- There is **no** `Insider.Instance.getAppFramesView(...)` factory — the view is instantiated
  directly (constructor in Compose `AndroidView`, or XML tag).
- The SDK never mutates the view's visibility / height / alpha — **the integrator owns dismiss and
  sizing**. This is why the persistence pattern lives app-side.
- The Android listener (on `feature/appframes-action-listener`, the branch we build from) has **6**
  callbacks: the 5 core ones plus a `default onFrameActionTriggered(view, JSONObject)` — the
  iOS-parity action callback. The demo overrides and demonstrates all 6.

## 4. Architecture

Follows the existing per-feature pattern (Screen + ViewModel), wired through `NavGraph`.

```
MainScreen (Core section)
   └─ "App Frames" button ─▶ Routes.APP_FRAMES
                                   └─ AppFramesScreen(viewModel, onBack)
                                         ├─ AppFramesViewModel      (status state + dismiss orchestration)
                                         │     └─ DismissedFramesStore (SharedPreferences)
                                         └─ per placement:
                                               AndroidView { InsiderAppFramesView }
                                                 + InsiderAppFramesViewListener ─▶ viewModel.updateStatus(...)
```

### New files (`example/src/main/java/com/useinsider/kotlindemo/`)

- **`screen/AppFramesScreen.kt`** — `@Composable AppFramesScreen(viewModel: AppFramesViewModel,
  onBack: () -> Unit)`. Back bar identical in style to `AppCardsScreen`. Scrollable `Column` /
  `LazyColumn` of 5 placement sections + a "Reset dismissed" `InsiderGradientButton`. Each section:
  - section label `"Placement: <id>"`,
  - a `Card` wrapping `AndroidView(factory = { InsiderAppFramesView(it) … })`,
  - a status chip (colored dot + label) bound to `viewModel.statusFor(placementId)`.
  - Sections whose placement is in the persisted-dismissed set render hidden (chip shows
    "Dismissed (persisted)").
- **`viewmodel/AppFramesViewModel.kt`** — `ViewModel` holding
  `statuses: Map<String, FrameStatus>` (Compose state) and `dismissed: Set<String>` (loaded from
  the store). Functions: `updateStatus(placementId, FrameStatus)`, `persistDismiss(placementId)`,
  `resetDismissed()`, `isDismissed(placementId)`. `FrameStatus` is a small enum/sealed type
  (`Idle`, `Loading`, `Loaded`, `HeightUpdated`, `ActionTriggered`, `Failed(message)`, `Dismissed`)
  mapped to chip text + color.
- **`util/DismissedFramesStore.kt`** — thin wrapper over `SharedPreferences`
  (`"insider_app_frames"`, key `"dismissed_placements"` as a `StringSet`): `dismissed(): Set<String>`,
  `add(placementId)`, `clear()`. Plain `SharedPreferences` (not EncryptedSharedPreferences) — the
  dismissed set is non-sensitive and clarity matters for a demo.

### Edited files

- **`navigation/NavGraph.kt`** — add `Routes.APP_FRAMES = "app_frames"`; add a `composable`
  building `AppFramesViewModel` and rendering `AppFramesScreen(..., onBack = popBackStack)`; pass
  `onNavigateToAppFrames` into `MainScreen`.
- **`screen/MainScreen.kt`** — add `onNavigateToAppFrames: () -> Unit` param; add
  `ButtonItem("App Frames") { onNavigateToAppFrames() }` to the **Core** `ButtonGrid` (next to
  "App Cards").
- **`README.md`** (repo root) — document the App Frames screen.

## 5. Demonstrating the four requirements

| # | Requirement | How |
|---|---|---|
| 1 | Drop-in `wrap_content` | `home_page`, `placement_1..3`: `AndroidView` with `LayoutParams.WRAP_CONTENT`; the view self-sizes via its `onMeasure`. |
| 2 | Drop-in fixed height | `placement_4`: the `AndroidView` is constrained to a fixed `200.dp` (`Modifier.height(200.dp)` / fixed `LayoutParams`). A code comment marks it as the fixed-height variant. |
| 3 | Listener — every callback | One `InsiderAppFramesViewListener` per view. Each of the **6** callbacks maps to a `FrameStatus` and updates that placement's chip: `onLoadStarted`→Loading, `onLoadFinished`→Loaded, `onLoadFailed`→Failed(code[: message]), `onHeightUpdateRequested`→HeightUpdated, `onFrameActionTriggered`→ActionTriggered (also logs the `JSONObject`), `onDismissRequested`→Dismissed. The chip is the visible proof each callback fired. |
| 4 | Dismiss-persistence | `onDismissRequested` → `viewModel.persistDismiss(id)` (writes to `DismissedFramesStore`) **and** hide the view. On screen entry, placements in the store start hidden. "Reset dismissed" clears the store and restores all sections. This is the recommended pattern: the SDK re-serves a dismissed frame on next load, so the app persists the user's dismiss and suppresses it. |

## 6. Data flow & Compose interop notes

- The `InsiderAppFramesView` is created once per section in the `AndroidView` `factory`; it is
  `remember`ed (keyed by placement id) so recomposition does not recreate it. `placementId` and the
  listener are set in the factory.
- Callbacks arrive on the main thread → they call `viewModel.updateStatus(...)`, which mutates
  Compose state and recomposes only the chip. The `InsiderAppFramesView` itself is not recomposed.
- The listener knows its placement id via closure capture at creation (no `view.getId()` mapping
  needed, unlike the XML reference).
- Dismiss hides the view by setting `visibility = GONE` on the captured view **and** persisting;
  the section's chip reflects the dismissed state.

## 7. Testing plan (emulator)

1. Build `insider` aar (App Frames branch) → copy to `example/libs/`; confirm `InsiderAppFramesView`
   present in `classes.jar`.
2. Wire the demo worktree to it (flatDir + `files(...)` + transitive deps); set partner
   `qaautomatin1` locally (not committed).
3. `./gradlew :example:assembleDebug`, install on a running emulator.
4. Navigate Main → **App Frames**. Verify:
   - Each chip transitions from Idle → Loading → Loaded (or → Failed with a readable code).
   - `placement_4` renders within the fixed 200.dp bound; the others size to content.
   - Triggering a dismiss (via a frame's dismiss control) hides that placement; relaunching the app
     keeps it hidden; "Reset dismissed" brings it back.
5. If `qaautomatin1` lacks configured campaigns for some placement, that placement exercises the
   `onLoadFailed(NO_FRAME_FOR_PLACEMENT)` path — still a valid callback demonstration.

## 8. Out of scope / decisions

- **Java variant:** the demo has no Java sample module → documented skip (task's "optional" item).
- **`examplewebview` module:** App Frames is a native-SDK feature (`insider`, not
  `insiderwebview`) → screen lives only in the `example` module.
- **Committed partner name:** stays `"partnername"`; `qaautomatin1` is a local-only test override.
- **Encrypted persistence:** not used; the dismissed-placement set is non-sensitive.

## 9. Risks

- **Local-aar wiring:** the `files("libs/insider-appframes.aar")` build must resolve the App-Frames
  aar, not the App-Frames-less `16.1.0-rc1`. Mitigation: replace the `libs.insider.sdk` line
  entirely, and verify the aar's `classes.jar` contains `InsiderAppFramesView` before building UI.
  A bare aar carries no POM, so all runtime transitive deps are declared manually (see §2).
- **Unpublished dependency:** because the aar is a local unpublished build, CI/other machines cannot
  compile until the SDK ships a published App-Frames version. Documented in the build file comment;
  the swap-back to a pinned `libs.insider.sdk` is a one-line change when that release lands.
- **Reviewers/git:** `example/libs/*.aar` is git-ignored (no binary blob committed).
- **Campaign availability on `qaautomatin1`:** if placements aren't configured, content won't
  render (callbacks still fire). Not a code defect.
