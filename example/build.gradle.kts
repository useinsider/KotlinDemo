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
        val partnerName = "orkunbites"
        manifestPlaceholders["partner"] = partnerName
        buildConfigField("String", "PARTNER_NAME", "\"$partnerName\"")
        manifestPlaceholders["googleAdsAppId"] = project.findProperty("GOOGLE_ADS_APP_ID") ?: ""
    }
    buildTypes {
        release {
            isMinifyEnabled = false
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
//    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    //Required
    implementation(libs.insider.sdk)
    implementation(libs.insider.webview)
    // insiderwebview declares webkit as compileOnly, so the consumer must still
    // provide it explicitly — it is not carried by the SDK POM.
    implementation(libs.webkit)
//    implementation(libs.firebase.messaging)
//    implementation(libs.lifecycle.process)
//    implementation(libs.security.crypto)

    // MOB-27585 embed-deps validation: with insider:16.1.0-rc1 the 7 lines below
    // are resolved transitively from the SDK POM, so a partner no longer declares
    // them manually.
    // implementation(libs.firebase.messaging)
    // implementation(libs.lifecycle.process)
    // implementation(libs.security.crypto)
    // implementation(libs.huawei.push)
    // implementation(libs.huawei.ads)
    // implementation(libs.huawei.location)
    // implementation(libs.play.services.location) // (was: //Optional for Geofence)

    debugImplementation(libs.androidx.ui.tooling)
}