import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.lowbudgetlcs.riot4k.samples.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.lowbudgetlcs.riot4k.samples.android"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.compileSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        // Injected at build time so the key never lives in source control.
        buildConfigField(
            "String",
            "RIOT_API_KEY",
            "\"${System.getenv("RIOT_API_KEY") ?: ""}\"",
        )
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation("com.lowbudgetlcs:riot4k-api:0.1.0-SNAPSHOT")
    implementation(libs.kotlinx.coroutines.android)
}
