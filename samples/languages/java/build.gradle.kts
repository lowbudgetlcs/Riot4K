plugins {
    java
    application
    // All sources in this sample are pure Java. The Kotlin plugin is applied
    // only so Gradle's variant-aware resolution picks the SDK's JVM variant
    // when substituting the included build; consumers resolving the SDK from
    // Maven need nothing but the java plugin.
    alias(libs.plugins.kotlinJvm)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("com.lowbudgetlcs:riot4k-api:0.1.0-SNAPSHOT")

    testImplementation(libs.junit.jupiter)
    testImplementation(project(":mock-riot-server"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "com.lowbudgetlcs.riot4k.samples.java.Main"
}

tasks.test {
    useJUnitPlatform()
    systemProperty("fixturesDir", rootDir.resolve("fixtures").absolutePath)
}
