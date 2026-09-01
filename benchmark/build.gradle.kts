plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.benchmark)
}

android {
    namespace = "com.useinsider.kotlindemo.benchmark"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        // AndroidBenchmarkRunner performs the warmup and reports the distribution; the plain
        // AndroidJUnitRunner does neither.
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"

        // Committed default stays a placeholder so no real partner is baked into the repo:
        //   ./gradlew :benchmark:connectedReleaseAndroidTest -PinsiderPartnerName=<partner>
        val partnerName = "partnername"
        // takeIf(isNotBlank): `-PinsiderPartnerName=$VAR` with VAR unset expands to an EMPTY
        // string, not an absent property, so a plain `?:` would leave PARTNER_NAME="".
        val resolvedPartnerName =
            (project.findProperty("insiderPartnerName") as String?)?.takeIf { it.isNotBlank() }
                ?: partnerName
        buildConfigField("String", "PARTNER_NAME", "\"$resolvedPartnerName\"")
        // The SDK manifest declares a deep-link scheme via ${'$'}{partner}; the androidTest
        // manifest merge fails without it.
        manifestPlaceholders["partner"] = resolvedPartnerName
        // Single source for the guard in InsiderEventBenchmark, so the two cannot drift.
        buildConfigField("String", "PLACEHOLDER_PARTNER_NAME", "\"$partnerName\"")
    }

    buildFeatures {
        buildConfig = true
    }

    // Microbenchmarks must run against a non-debuggable, minified build; a debuggable app is
    // interpreted rather than JIT-compiled in places.
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
    // Same opt-in as :example: keying off file existence would make the measured artefact
    // depend on what someone had copied in, with nothing in the report saying which bytes ran.
    val localMinifiedSdk = file("../example/libs/insider-release.aar")

    if (project.hasProperty("useLocalInsiderAar")) {
        require(localMinifiedSdk.exists()) {
            "-PuseLocalInsiderAar was passed but ${localMinifiedSdk.path} does not exist"
        }
        logger.lifecycle("Benchmarking LOCAL minified SDK ${localMinifiedSdk.path}")
        androidTestImplementation(files(localMinifiedSdk))
    } else {
        androidTestImplementation(libs.insider.sdk)
    }

    // The local AAR carries no POM, so the SDK's own dependencies are declared here explicitly.
    androidTestImplementation(libs.androidx.core.ktx)
    androidTestImplementation(libs.lifecycle.process)
    androidTestImplementation(libs.androidx.work.runtime)
    androidTestImplementation(libs.security.crypto)

    androidTestImplementation(libs.benchmark.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
