plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    runtimeOnly(libs.slf4j.simple)
}

application {
    mainClass = "com.lowbudgetlcs.riot4k.mockserver.MainKt"
}

kotlin {
    jvmToolchain(21)
}
