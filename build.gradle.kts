import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.binaryCompatibilityValidator)
    alias(libs.plugins.dokka)
}

apiValidation {
    // klib ABI validation also covers the native/js targets once stable;
    // the JVM/Android dumps in each module's api/ directory are the gate.
}

dependencies {
    dokka(project(":riot4k-core"))
    dokka(project(":riot4k-models"))
    dokka(project(":riot4k-api"))
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
}

allprojects {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        source.setFrom(
            "src/commonMain/kotlin",
            "src/commonTest/kotlin",
            "src/jvmMain/kotlin",
            "src/jvmTest/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin",
            "src/linuxX64Main/kotlin",
            "src/jsMain/kotlin",
        )
    }

    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
    }

    tasks.withType<Detekt>().configureEach {
        reports {
            html.required.set(true)
            sarif.required.set(true)
        }
    }
}
