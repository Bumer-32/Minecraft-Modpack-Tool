plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("application")
    id("maven-publish")
}

group = "ua.pp.lumivoid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass = "ua.pp.lumivoid.MainKt"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "ua.pp.lumivoid.MainKt"
    }

    from("LICENSE")
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("org.slf4j:jul-to-slf4j:2.0.17")

    implementation("io.ktor:ktor-client-core:3.2.3")
    implementation("io.ktor:ktor-client-cio:3.2.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}

kotlin {
    jvmToolchain(21)
}