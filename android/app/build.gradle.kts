plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appId = providers.gradleProperty("APP_ID").orElse("dev.pocketforge.generated").get()

android {
    namespace = "dev.pocketforge.shell"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = appId
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = providers.gradleProperty("VERSION_NAME").orElse("1.0.0").get()
    }

    buildFeatures { buildConfig = true }
}
