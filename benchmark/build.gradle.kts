plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.benchmark)
}

android {
    namespace = "com.useinsider.kotlindemo.benchmark"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        // AndroidBenchmarkRunner, not the plain AndroidJUnitRunner: it is what performs the
        // warmup, locks clocks where possible, and reports the per-iteration distribution.
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"

        // Same override the load harness uses; the committed default stays a placeholder so a
        // real partner is never baked into the repo:
        //   ./gradlew :benchmark:connectedReleaseAndroidTest -PinsiderPartnerName=qaautomation1
        val partnerName = "partnername"
        // takeIf(isNotBlank): `-PinsiderPartnerName=$VAR` with VAR unset expands to an EMPTY
        // string, not an absent property, so a plain `?:` would leave PARTNER_NAME="" and the
        // benchmark would measure a degraded init while every guard stayed green.
        val resolvedPartnerName =
            (project.findProperty("insiderPartnerName") as String?)?.takeIf { it.isNotBlank() }
                ?: partnerName
        buildConfigField("String", "PARTNER_NAME", "\"$resolvedPartnerName\"")
        // The SDK manifest declares a deep-link scheme via ${'$'}{partner}; without this the
        // androidTest manifest merge fails.
        manifestPlaceholders["partner"] = resolvedPartnerName
        // Single source for the guard in InsiderEventBenchmark: re-hardcoding the literal there
        // would let the two drift and silently re-admit the placeholder build.
        buildConfigField("String", "PLACEHOLDER_PARTNER_NAME", "\"$partnerName\"")
    }

    buildFeatures {
        buildConfig = true
    }

    // Microbenchmarks must run against a non-debuggable, minified build. A debuggable app is
    // interpreted rather than JIT-compiled in places, which is exactly the distortion the
    // MOB-28339 harness suffered from: its ~27% run-to-run spread is what hand-timing a
    // debuggable loop looks like.
    testBuildType = "release"

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Same local-AAR escape hatch the example module uses. The artefact is minified because the
    // AAR arrives that way from upstream — NOT because of this module's isMinifyEnabled, which
    // only affects this module's own (empty) library variant. Verified in build/intermediates:
    // there is no minifyReleaseAndroidTestWithR8 task.
    val localMinifiedSdk = file("../example/libs/insider-release.aar")

    if (localMinifiedSdk.exists()) {
        androidTestImplementation(files(localMinifiedSdk))
    } else {
        androidTestImplementation(libs.insider.sdk)
    }

    // The local AAR carries no POM, so the SDK's own dependencies must be declared here the
    // same way :example declares them. Measured: without androidx.core the benchmark APK dies
    // on NoClassDefFoundError for androidx/core/util/Supplier before any measurement runs.
    androidTestImplementation(libs.androidx.core.ktx)
    androidTestImplementation(libs.lifecycle.process)
    androidTestImplementation(libs.androidx.work.runtime)
    androidTestImplementation(libs.security.crypto)

    androidTestImplementation(libs.benchmark.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
