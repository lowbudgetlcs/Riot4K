pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "riot4k-samples"

// Samples consume the SDK exactly like an external project would; the included
// build substitutes com.lowbudgetlcs:riot4k-* dependencies with the local modules.
includeBuild("..")

include(":mock-riot-server")

// Platform samples: the same Kotlin consumer on every platform the SDK ships to.
include(":jvm")
include(":android")
include(":nodejs")
include(":linux")
include(":ios")
project(":jvm").projectDir = file("platforms/jvm")
project(":android").projectDir = file("platforms/android")
project(":nodejs").projectDir = file("platforms/nodejs")
project(":linux").projectDir = file("platforms/linux")
project(":ios").projectDir = file("platforms/ios")

// Language samples: other languages consuming the SDK's distribution artifacts.
// TypeScript (languages/typescript) and Swift (languages/swift) are not Gradle
// projects; they build with npm and SwiftPM respectively.
include(":java")
project(":java").projectDir = file("languages/java")
