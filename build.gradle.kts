import com.vanniktech.maven.publish.MavenPublishBaseExtension
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

    plugins.withId("com.vanniktech.maven.publish") {
        configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            // Signing credentials are provided via ORG_GRADLE_PROJECT_signingInMemoryKey*
            // environment variables in CI; local publishing skips signing when absent.
            if (providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey").isPresent) {
                signAllPublications()
            }
            coordinates(group.toString(), project.name, version.toString())

            pom {
                name = project.name
                description = "Kotlin Multiplatform SDK for the Riot Games API"
                inceptionYear = "2025"
                url = "https://github.com/lowbudgetlcs/Riot4K"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        distribution = "repo"
                    }
                }
                developers {
                    developer {
                        id = "zainaraza43"
                        name = "Zain Raza"
                        url = "https://github.com/zainaraza43"
                    }
                }
                scm {
                    url = "https://github.com/lowbudgetlcs/Riot4K"
                    connection = "scm:git:git://github.com/lowbudgetlcs/Riot4K.git"
                    developerConnection = "scm:git:ssh://git@github.com/lowbudgetlcs/Riot4K.git"
                }
            }
        }
    }
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
