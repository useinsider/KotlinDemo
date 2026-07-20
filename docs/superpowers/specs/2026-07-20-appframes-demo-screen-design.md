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
source in the `mobileandroid` App Frames branches. Resolution:

- Build the `insider` aar from `mobileandroid` branch **`feature/appframes-action-listener`** (this
  branch carries the final 5-callback `InsiderAppFramesViewListener`) and publish it to
  **`mavenLocal`** under a pinned local version.
- In the demo worktree: add `mavenLocal()` to `settings.gradle.kts`
  `dependencyResolutionManagement`, keep the RC repo (needed for `insiderwebview:1.1.0-rc1` and
  transitive deps), and pin `insider_sdk` in `libs.versions.toml` to the built version.
- **Verify** the resolved aar exposes `com.useinsider.insider.InsiderAppFramesView` before wiring UI.

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
- The Android listener has **5** callbacks (no `didTriggerAction`; that callback is iOS-only).

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
  (`Idle`, `Loading`, `Loaded`, `HeightUpdated`, `Failed(message)`, `Dismissed`) mapped to chip
  text + color.
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
| 3 | Listener — every callback | One `InsiderAppFramesViewListener` per view. Each callback maps to a `FrameStatus` and updates that placement's chip: `onLoadStarted`→Loading, `onLoadFinished`→Loaded, `onLoadFailed`→Failed(code[: message]), `onHeightUpdateRequested`→HeightUpdated, `onDismissRequested`→Dismissed. The chip is the visible proof each callback fired. |
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

1. Build `insider` aar (App Frames branch) → `mavenLocal`; confirm `InsiderAppFramesView` present.
2. Wire the demo worktree to it; set partner `qaautomatin1` locally (not committed).
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

- **RC/mavenLocal wiring:** `insider_sdk` must resolve to the App-Frames build without pulling the
  App-Frames-less `16.1.0-rc1`. Mitigation: pin an explicit local version and verify the resolved
  aar contains `InsiderAppFramesView` before building UI.
- **Transitive deps:** `main` uses `insider_sdk = "+"` with several manual deps; the App Frames
  build may or may not embed them. Mitigation: reconcile against the `feature/MOB-27585-test`
  embed-deps config; restore any manual deps the local build doesn't provide so the app links.
- **Campaign availability on `qaautomatin1`:** if placements aren't configured, content won't
  render (callbacks still fire). Not a code defect.
