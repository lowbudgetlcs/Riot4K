plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Riot4KSample"
            isStatic = true
        }
    }

    sourceSets {
        iosMain.dependencies {
            implementation("com.lowbudgetlcs:riot4k-api:0.1.0-SNAPSHOT")
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
