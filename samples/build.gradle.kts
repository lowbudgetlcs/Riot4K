import io.gitlab.arturbosch.detekt.Detekt

// Declares every plugin used by the sample subprojects so they all load in a
// single classloader; sibling projects sharing Kotlin/Native and Android build
// services fail otherwise.
plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.detekt)
}

allprojects {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.file("../config/detekt/detekt.yml"))
        source.setFrom(
            "src/main/kotlin",
            "src/test/kotlin",
            "src/jsMain/kotlin",
            "src/jsTest/kotlin",
            "src/linuxX64Main/kotlin",
            "src/linuxX64Test/kotlin",
            "src/iosMain/kotlin",
        )
    }

    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
    }

    tasks.withType<Detekt>().configureEach {
        reports {
            html.required.set(true)
        }
    }
}
