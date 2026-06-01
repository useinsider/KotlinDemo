pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // RC validation: resolves the Insider SDK RC build
        // (com.useinsider:insider:16.1.0-rc1 etc.) from the partner-invisible
        // RC S3 path. Listed before the production repo so the RC build wins.
        maven { url = uri("https://mobilesdk.useinsider.com/android/rc") }
        google()
        mavenCentral()
//        maven { url = uri("https://mobilesdk.useinsider.com/android") }
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

rootProject.name = "KotlinDemo"
include(":example")
include(":examplewebview")
 