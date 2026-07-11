// Declares every plugin used by the sample subprojects so they all load in a
// single classloader; sibling projects sharing Kotlin/Native and Android build
// services fail otherwise.
plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.android.application) apply false
}
