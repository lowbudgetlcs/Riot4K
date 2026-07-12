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

        linuxX64Test.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
    }
}
