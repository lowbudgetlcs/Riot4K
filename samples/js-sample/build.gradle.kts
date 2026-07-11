plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    js(IR) {
        nodejs()
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation("com.lowbudgetlcs:riot4k-api:0.1.0-SNAPSHOT")
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
