plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

dependencies {
    implementation("com.lowbudgetlcs:riot4k-api:0.1.0-SNAPSHOT")
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlin.test)
    testImplementation(project(":mock-riot-server"))
}

application {
    mainClass = "com.lowbudgetlcs.riot4k.samples.jvm.MainKt"
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    systemProperty("fixturesDir", rootDir.resolve("fixtures").absolutePath)
}
