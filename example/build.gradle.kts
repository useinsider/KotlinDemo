plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.useinsider.kotlindemo"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.useinsider.ecommerce"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        // TODO: Please change with your partner name.
        // Default stays the placeholder: committing a live partner here would silently point
        // every demo build at a real account (and would revert 11c3979).
        //
        // Keep the declaration below as a bare string literal. mobileandroid's
        // build-demo-application.yml rewrites it in place with a sed substitution, and sed
        // exits 0 when it matches nothing — so any other shape here disables that step
        // silently and ships a demo APK pointing at the placeholder account.
        val partnerName = "partnername"
        // Local override for the MOB-28339 load harness, which needs a real partner for one run:
        //   ./gradlew :example:connectedDebugAndroidTest -PinsiderPartnerName=qaautomation1
        // takeIf(isNotBlank) mirrors :benchmark — `-PinsiderPartnerName=$VAR` with VAR unset
        // expands to an EMPTY string, not an absent property, so a bare `?:` never fires.
        val resolvedPartnerName =
            (project.findProperty("insiderPartnerName") as String?)?.takeIf { it.isNotBlank() }
                ?: partnerName
        manifestPlaceholders["partner"] = resolvedPartnerName
        buildConfigField("String", "PARTNER_NAME", "\"$resolvedPartnerName\"")
        buildConfigField("String", "PLACEHOLDER_PARTNER_NAME", "\"$partnerName\"")
        manifestPlaceholders["googleAdsAppId"] = project.findProperty("GOOGLE_ADS_APP_ID") ?: ""
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "insider.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEYSTORE_KEY_ALIAS")
            keyPassword = System.getenv("KEYSTORE_KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    //Required
    // MOB-28339: the load harness wants the R8-MINIFIED AAR we actually ship, not a source
    // build. That is opt-in ON PURPOSE and must NOT key off mere file existence: this is
    // `implementation`, so it reshapes EVERY variant including the signed release APK that
    // ./build.sh produces. insider-release.aar is gitignored (*.aar), so a file-existence
    // check would make the same commit ship different bytes depending on whether someone had
    // hand-copied an AAR — with nothing in the build output saying which.
    //
    //   ../mobileandroid $ ./gradlew :insider:assembleRelease
    //   cp insider/build/outputs/aar/insider-release.aar <here>/example/libs/
    //   ./gradlew :example:connectedDebugAndroidTest -PuseLocalInsiderAar -PinsiderPartnerName=<partner>
    val localMinifiedSdk = file("libs/insider-release.aar")
    val useLocalInsiderAar = project.hasProperty("useLocalInsiderAar")

    if (useLocalInsiderAar) {
        require(localMinifiedSdk.exists()) {
            "-PuseLocalInsiderAar was passed but ${localMinifiedSdk.path} does not exist"
        }
        logger.lifecycle("MOB-28339: linking LOCAL minified SDK ${localMinifiedSdk.path}")
        implementation(files(localMinifiedSdk))
    } else {
        implementation(libs.insider.sdk)
    }

    implementation(libs.insider.webview)
    implementation(libs.webkit)
    implementation(libs.firebase.messaging)
    implementation(libs.lifecycle.process)
    implementation(libs.security.crypto)

    implementation(libs.huawei.push)
    implementation(libs.huawei.ads)
    implementation(libs.huawei.location)

    //Optional for Geofence
    implementation(libs.play.services.location)

    debugImplementation(libs.androidx.ui.tooling)

    // MOB-28339 load harness; declared via the version catalog like every other dependency.
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
}