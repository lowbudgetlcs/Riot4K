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
include(":jvm-sample")
include(":java-sample")
include(":android-sample")
include(":js-sample")
include(":linux-sample")
include(":ios-sample")
