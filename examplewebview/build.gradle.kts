plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.useinsider.examplewebview"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.useinsider.ecommerce"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        // TODO: Please change with your partner name.
        val partnerName = "partnername"
        manifestPlaceholders["partner"] = partnerName
        buildConfigField("String", "PARTNER_NAME", "\"$partnerName\"")
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
    implementation(libs.androidx.activity.ktx)

    implementation(libs.insider.sdk)
    implementation(libs.insider.webview)
    implementation(libs.webkit)
    implementation(libs.firebase.messaging)
    implementation(libs.lifecycle.process)
    implementation(libs.security.crypto)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.huawei.push)
    implementation(libs.huawei.ads)
    implementation(libs.huawei.location)
}
