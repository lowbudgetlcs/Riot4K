plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

dependencies {
    implementation("com.lowbudgetlcs:riot4k-api:0.1.0-SNAPSHOT")
    implementation(libs.kotlinx.coroutines.core)
}

application {
    mainClass = "com.lowbudgetlcs.riot4k.samples.jvm.MainKt"
}

kotlin {
    jvmToolchain(21)
}
