plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    linuxX64 {
        binaries {
            executable {
                entryPoint = "com.lowbudgetlcs.riot4k.samples.linux.main"
            }
        }
    }

    sourceSets {
        linuxX64Main.dependencies {
            implementation("com.lowbudgetlcs:riot4k-api:0.1.0-SNAPSHOT")
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
