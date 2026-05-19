# Insider Android SDKs Example

[![Maven Version](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fmobilesdk.useinsider.com%2Fandroid%2Fcom%2Fuseinsider%2Finsider%2Fmaven-metadata.xml&query=%2F%2Fmetadata%2Fversioning%2Frelease&label=insider&color=blue)](https://mobilesdk.useinsider.com/android/com/useinsider/insider/)
[![Maven Version](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Fmobilesdk.useinsider.com%2Fandroid%2Fcom%2Fuseinsider%2Finsiderwebview%2Fmaven-metadata.xml&query=%2F%2Fmetadata%2Fversioning%2Frelease&label=insiderwebview&color=blue)](https://mobilesdk.useinsider.com/android/com/useinsider/insider/)

<p align="center">
  <img src="https://github.com/user-attachments/assets/4b33cb82-5896-4b0a-b2bd-a294c421d8d8" width="400">
</p>

<p align="center">
  <a href="https://insiderone.com/">Insider</a> &bull;
  <a href="https://academy.insiderone.com/docs/android-integration">Documentation</a> &bull;
  <a href="LICENSE">MIT License</a>
</p>

This project demonstrates how to integrate the [Insider Android SDK](https://academy.insiderone.com/docs/android-integration) into a Kotlin application. It ships **two app modules** — one for the Native SDK (`insider`) and one for the WebView SDK (`insiderwebview`) — each fully wired with push notifications, geofence tracking, in-app messaging and the full event surface.

## Requirements

| Requirement | Minimum |
|---|---|
| Android | API 24 (Android 7.0) |
| Kotlin | 2.3+ |
| Android Gradle Plugin | 9.0+ |
| JDK | 21 (required by AGP 9) |

## Project Structure

The project ships **two flavors** of the example app (**`example`**) — app using `insider` library and a webview-based app (**`examplewebview`**) using `insiderwebview` library — as independent gradle modules sharing a single root build.

```
KotlinDemo/
├── example/                                 
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/useinsider/kotlindemo/
│       │   ├── ExampleApplication.kt # SDK initialization & callbacks
│       │   ├── MainActivity.kt              
│       │   ├── InsiderFirebaseMessagingService.kt
│       │   ├── action/             # SDK feature implementations         
│       │   ├── callback/, component/, model/, navigation/, screen/, ui/, viewmodel/
│       └── res/
│           └── values/{colors,strings,themes}.xml
│
├── examplewebview/                          
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/useinsider/examplewebview/
│       │   ├── ExampleWebViewApplication.kt # SDK initialization & callbacks
│       │   ├── MainActivity.kt              
│       │   └── InsiderFirebaseMessagingService.kt
│       ├── assets/index.html
│       └── res/
│           └── values/{colors,strings,themes}.xml
│
├── settings.gradle.kts                 
├── build.gradle.kts
└── gradle/libs.versions.toml                # Version Catalog
```

| Module | Flavor | SDKs |
|---|---|---|
| `example` | Native | `insider` |
| `examplewebview` | WebView | `insider` + `insiderwebview` |

## Getting Started

### 1. Clone the Repository

```bash
git clone git@github.com:useinsider/KotlinDemo.git
cd KotlinDemo
```

### 2. Configure Your App

Before running, update the following values in **each module's `build.gradle.kts`** (`example/build.gradle.kts` and/or `examplewebview/build.gradle.kts`):

- **Partner Name** — used by the SDK at runtime and exposed as `BuildConfig.PARTNER_NAME`:

```kotlin
val partnerName = "YOUR_PARTNER_NAME"
```

- **Application ID** — must match the package registered in your `google-services.json`:

```kotlin
applicationId = "com.your.package.name"
```

- **Firebase** — drop your `google-services.json` into the module root (`example/` and/or `examplewebview/`). The file is git-ignored.

- **Huawei** *(optional)* — drop `agconnect-services.json` into the same module root if you target Huawei devices.

### 3. SDK Dependencies

The Insider Maven repository is already declared in [`settings.gradle.kts`](settings.gradle.kts):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://mobilesdk.useinsider.com/android") }
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}
```

Versions are pinned in `gradle/libs.versions.toml`:

```toml
[versions]
insider_sdk     = "+"  # e.g.: 16.0.3
insider_webview = "+"  # e.g.: 1.0.0

[libraries]
insider_sdk     = { module = "com.useinsider:insider",        version.ref = "insider_sdk" }
insider_webview = { module = "com.useinsider:insiderwebview", version.ref = "insider_webview" }
```

And consumed from each module:

```kotlin
dependencies {
    // Native SDK (required by both modules)
    implementation(libs.insider.sdk)

    // WebView SDK (only in :examplewebview)
    // implementation(libs.insider.webview)

    // Use the -nh variant of insider if you do not target Huawei devices
    // implementation("com.useinsider:insider:16.0.3-nh")
}
```

> **JDK note:** AGP 9 requires JDK 21. If your `JAVA_HOME` is older, point Gradle at Android Studio's bundled JBR:
> ```bash
> export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
> ```

## SDK Initialization

The SDK is initialized in your `Application` subclass. Both modules follow the same pattern — see `ExampleApplication.kt` and `ExampleWebViewApplication.kt`:

```kotlin
import android.app.Application
import com.useinsider.insider.Insider
import com.useinsider.insider.InsiderCallbackType

public class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Insider.Instance.init(this, BuildConfig.PARTNER_NAME)

        // Optional: register a callback for SDK events
        Insider.Instance.registerInsiderCallback { data, callbackType ->
            when (callbackType) {
                InsiderCallbackType.NOTIFICATION_OPEN       -> { /* push tapped */ }
                InsiderCallbackType.INAPP_SEEN              -> { /* in-app impression */ }
                InsiderCallbackType.INAPP_BUTTON_CLICK      -> { /* in-app button */ }
                InsiderCallbackType.TEMP_STORE_CUSTOM_ACTION-> { /* custom action */ }
                InsiderCallbackType.TEMP_STORE_PURCHASE     -> { /* purchase */ }
                InsiderCallbackType.TEMP_STORE_ADDED_TO_CART-> { /* add to cart */ }
                InsiderCallbackType.SESSION_STARTED         -> { /* session start */ }
            }
        }
    }
}
```

Register the `Application` class in the module's `AndroidManifest.xml`:

```xml
<application
    android:name=".ExampleApplication"
    ... >
```

## Push Notifications

### Firebase Cloud Messaging

Both modules include a `FirebaseMessagingService` that hands Insider-tagged messages to the SDK:

```kotlin
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.useinsider.insider.Insider

public class InsiderFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data["source"] == "Insider") {
            Insider.Instance.handleFCMNotification(applicationContext, remoteMessage)
        }
    }
}
```

Registered in `AndroidManifest.xml`:

```xml
<service
    android:name=".InsiderFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

Required dependency (already wired in both modules):

```kotlin
implementation(libs.firebase.messaging)
```

The Google Services plugin is applied per-module — it reads `google-services.json` at build time:

```kotlin
plugins {
    alias(libs.plugins.google.services)
}
```

### Huawei Messaging Service (Optional)

For Huawei devices, both modules include the HMS push, ads, and location dependencies:

```kotlin
implementation(libs.huawei.push)
implementation(libs.huawei.ads)
implementation(libs.huawei.location)
```

The Insider SDK handles HMS push internally — no extra service registration needed beyond dropping `agconnect-services.json` into the module root.

## Geofence Tracking (Optional)

Available in both `example` and `examplewebview` modules. To enable, add the location dependency and start tracking from your application:

```kotlin
// build.gradle.kts
implementation(libs.play.services.location)
```

```kotlin
// Application.onCreate()
Insider.Instance.startTrackingGeofence()
```

> **Note:** `startTrackingGeofence()` requires `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` at runtime — see the **Runtime Permissions** section below.

## Runtime Permissions

Insider features that touch OS-protected resources require both a `<uses-permission>` entry in `AndroidManifest.xml` **and** a runtime request on Android 6.0+ (API 23+). Adding the permission to the manifest alone is **not** sufficient — without a runtime request the OS returns `PERMISSION_DENIED` and the feature silently fails.

| Feature | Permission(s) | Required from |
|---|---|---|
| Push notifications (FCM/HMS) | `POST_NOTIFICATIONS` | Android 13 (API 33) |
| Geofence tracking | `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Android 6 (API 23) |
| Background geofence | `ACCESS_BACKGROUND_LOCATION` | Android 10 (API 29) — separate flow |

> The SDK's own consent flags (`setPushOptin`, `setLocationOptin`, `enableLocationCollection`) do **not** require any OS permission — they only record the user's consent inside the SDK. OS permissions are needed when an action actually touches the platform (delivering a notification, reading location).

Both modules declare these permissions in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```

### Native (Compose) — `example`

The `example` module ships a small Compose helper at [`permission/RuntimePermissions.kt`](example/src/main/java/com/useinsider/kotlindemo/permission/RuntimePermissions.kt) that wraps `ActivityResultContracts.RequestMultiplePermissions`:

```kotlin
val permissions = rememberRuntimePermissionRequester()

InsiderGradientButton(
    text = "Start Tracking Geofence",
    onClick = {
        permissions.withLocationPermission { granted ->
            if (granted) Insider.Instance.startTrackingGeofence()
        }
    }
)
```

The helper checks `ContextCompat.checkSelfPermission` first and only launches the request dialog when the permission is not already granted. `withPushPermission` follows the same pattern for `POST_NOTIFICATIONS` (no-op on pre-13 devices).

### WebView — `examplewebview`

The `examplewebview` module exposes a JavaScript bridge so the page running inside the WebView can trigger native permission dialogs. `MainActivity` registers an `ActivityResultLauncher` and injects a [`PermissionBridge`](examplewebview/src/main/java/com/useinsider/examplewebview/permission/PermissionBridge.kt) into the WebView under the name `Permissions`:

```kotlin
webView.addJavascriptInterface(
    PermissionBridge(activity = this, webView = webView, requestPush = ..., requestLocation = ...),
    "Permissions"
)
```

[`index.html`](examplewebview/src/main/assets/index.html) exposes a `Promise`-returning `requestPermission(kind)` helper that wraps the bridge and is called from the relevant buttons:

```js
async function didTouchStartTrackingGeofence() {
    const granted = await requestPermission('location');
    if (!granted) {
        logInfo('Geofence: location permission denied');
        return;
    }
    await window.insider.startTrackingGeofence();
}
```

When the page is opened in a regular browser (no native bridge), `requestPermission` falls back to resolving `true` so the demo page stays usable for HTML/JS iteration.

### Where to request `POST_NOTIFICATIONS`

The demo does not bind `POST_NOTIFICATIONS` to a single button — pick the UX that fits your app:
- **On first launch**, before the user reaches a screen that depends on notifications.
- **On a "Register for Push" / onboarding step**, where it makes sense in context.
- **Right before** the first notification-driven feature is enabled.

Use the same `withPushPermission` helper (Native) or `requestPermission('push')` bridge call (WebView) — the manifest entry is already in place.

## InsiderWebView

The `examplewebview` module demonstrates the `insiderwebview` SDK. The `MainActivity` hands the `WebView` to the SDK via a single call, then loads the demo page from the APK assets:

```kotlin
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import com.useinsider.insiderwebview.InsiderWebView

public class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView = findViewById<WebView>(R.id.web_view)
        InsiderWebView.setupWebViewSDK(webView)
        webView.loadUrl("file:///android_asset/index.html")
    }
}
```

The demo ships two ways of feeding [`index.html`](examplewebview/src/main/assets/index.html) to the web view — pick whichever fits your workflow.

### Loading the page from the APK assets

The [`index.html`](examplewebview/src/main/assets/index.html) file is packaged into the APK at build time, so is available via the `file:///android_asset/` URL scheme:

```kotlin
webView.loadUrl("file:///android_asset/index.html")
```

### Loading the page from a local HTTP server

While iterating on [`index.html`](examplewebview/src/main/assets/index.html), it is faster to serve the file over HTTP so you can edit and refresh without rebuilding the app. From the repository root, run:

```bash
python3 -m http.server 8080 --directory examplewebview/src/main/assets
```

Then replace the `loadUrl(...)` call with:

```kotlin
webView.loadUrl("http://localhost:8080/index.html") // emulator → host
```

> **Note:** Android blocks plaintext HTTP traffic by default starting with API 28. To load `http://...` URLs you must add `android:usesCleartextTraffic="true"` to the `<application>` element in `AndroidManifest.xml`, or use a Network Security Config that allows the specific dev host.

### Using the SDK from TypeScript

When the page loaded in the WebView is part of a TypeScript codebase, you can get full type-safety and autocompletion for the JavaScript bridge that `InsiderWebView.setupWebViewSDK` injects. The [`InsiderWebViewScript.d.ts`](examplewebview/src/main/assets/InsiderWebViewScript.d.ts) file (shipped with the iOS demo) declares ambient types for the bridge — there is no runtime code in it; it only describes the API that the native SDK exposes at runtime as `window.insider`.

It declares:

- A global `window.insider` of type `Insider` (the bridge entry point).
- Classes / enums you can construct in TypeScript:
  - `InsiderEvent`
  - `InsiderProduct`
  - `InsiderIdentifiers`
  - `InsiderUser`
- Supporting types: 
  - `Insider`
  - `InsiderIDListener`
  - `InsiderListenerRegistration`
  - `MessageCenterMessage`

#### Wire it into your project

Copy [`InsiderWebViewScript.d.ts`](examplewebview/src/main/assets/InsiderWebViewScript.d.ts) into your web app's source tree (e.g. `src/types/`) and reference it in `tsconfig.json`:

```json
{
  "include": ["src/**/*", "src/types/InsiderWebViewScript.d.ts"]
}
```

Alternatively, put a triple-slash reference at the top of your entry file:

```ts
/// <reference path="./types/InsiderWebViewScript.d.ts" />
```

That is enough to make `window.insider` strongly typed everywhere — no `import` needed because the file augments the global `Window` interface.

#### Use the typed bridge

All calls are checked at compile time — wrong parameter shapes or types will fail `tsc` before they ever reach the device. Note that event and parameter keys are typed as plain `string`, so they are not constrained at compile time; follow the SDK's naming rules (lowercase, starts with a letter, only `a–z`, `0–9`, `_`).

```ts
async function onPurchase() {
    const product = window.insider
        .createNewProduct(
          'prod-123',
          'Headphones',
          ['Audio', 'Headphones'],
          'https://cdn.example.com/p/123.jpg',
          199.99,
          'USD'
        )
        .setBrand('Acme')
        .setSalePrice(149.99)
        .setStock(42);

    await window.insider.itemPurchased('sale-789', product, { campaign: 'spring_sale' });
}
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
