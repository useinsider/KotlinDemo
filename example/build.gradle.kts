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
        // every demo build at a real account (and would revert 11c3979). The MOB-28339 load
        // harness passes a real one for its run only:
        //   ./gradlew :example:connectedDebugAndroidTest -PinsiderPartnerName=qaautomation1
        val partnerName = (project.findProperty("insiderPartnerName") as String?) ?: "partnername"
        manifestPlaceholders["partner"] = partnerName
        buildConfigField("String", "PARTNER_NAME", "\"$partnerName\"")
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
    // MOB-28339: prefer a locally built, R8-MINIFIED AAR when one is present, so the load
    // harness exercises the same bytes we ship rather than a source build. The file is
    // gitignored (*.aar), so this falls back to the published artifact for everyone who has
    // not built one — the demo keeps working unchanged. Transitive deps are declared below
    // either way, which is why no POM is needed for the local path.
    //
    //   ../mobileandroid $ ./gradlew :insider:assembleRelease
    //   cp insider/build/outputs/aar/insider-release.aar <here>/example/libs/
    val localMinifiedSdk = file("libs/insider-release.aar")
    if (localMinifiedSdk.exists()) implementation(files(localMinifiedSdk))
    else implementation(libs.insider.sdk)
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

    // MOB-28339 load harness. Declared literally: the version catalog carries no test
    // entries and this is the first test source set in this repo.
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
}