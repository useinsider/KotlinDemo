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

    implementation(libs.insider.sdk)
    implementation(libs.insider.webview)
    // insiderwebview declares webkit as compileOnly, so the consumer must still
    // provide it explicitly — it is not carried by the SDK POM.
    implementation(libs.webkit)

    // MOB-27585 embed-deps validation: with insider:16.1.0-rc1 these are now
    // resolved transitively from the SDK POM, so a partner no longer declares
    // them manually. Uncomment to revert to the pre-16.1.0 manual setup.
    // implementation(libs.firebase.messaging)
    // implementation(libs.lifecycle.process)
    // implementation(libs.security.crypto)
    // implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)

    // implementation(libs.huawei.push)
    // implementation(libs.huawei.ads)
    // implementation(libs.huawei.location)
}
