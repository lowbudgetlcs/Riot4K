import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    // Swift-facing ergonomics (sealed classes as exhaustive Swift enums, structured
    // concurrency for suspend functions). Applies only to Apple framework linking;
    // remove this single line to ship a plain framework.
    alias(libs.plugins.skie)
}

kotlin {
    jvm()
    androidLibrary {
        namespace = "com.lowbudgetlcs.riot4k.api"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }
    // The framework module is named Riot4KSDK (not Riot4K) so the Riot4K entry
    // class stays directly referencable from Swift; a module and a type sharing
    // a name forces consumers to fully qualify every use.
    val xcf = XCFramework("Riot4KSDK")
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosArm64(),
        macosX64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Riot4KSDK"
            isStatic = true
            // Consumers see the full API surface (routes, config, RiotResult, DTOs).
            export(project(":riot4k-core"))
            export(project(":riot4k-models"))
            xcf.add(this)
        }
    }
    linuxX64()
    js(IR) {
        outputModuleName.set("riot4k-api")
        useEsModules()
        browser()
        nodejs()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonMain.dependencies {
            // Models and core types (routes, config, RiotResult) appear in
            // public signatures, so consumers need to see both.
            api(project(":riot4k-models"))
            api(project(":riot4k-core"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
    }

    explicitApi()
}
