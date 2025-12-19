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
}

rootProject.name = "riot4k"
include(":riot4k-core")
include(":riot4k-models")
include(":riot4k-api")
