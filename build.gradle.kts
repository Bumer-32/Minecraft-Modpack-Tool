import java.util.Properties

plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.17")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.21")
    // https://mvnrepository.com/artifact/org.codehaus.janino/janino
    implementation("org.codehaus.janino:janino:3.1.12")
    // https://mvnrepository.com/artifact/org.slf4j/jul-to-slf4j
    implementation("org.slf4j:jul-to-slf4j:2.0.17")

    // https://ktor.io/docs/client-create-new-application.html#add-dependencies
    implementation("io.ktor:ktor-client-core:3.3.2")
    implementation("io.ktor:ktor-client-cio:3.3.2")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    // ttps://github.com/charleskorn/kaml/releases/latest
    implementation("com.charleskorn.kaml:kaml:0.103.0")

//    implementation("org.jline:jline:3.30.0")

    // https://picocli.info/
    implementation("info.picocli:picocli:4.7.7")
}

kotlin {
    jvmToolchain(21)
}

tasks.processResources {
    dependsOn("packageSampleProject")
    dependsOn("generateVersionProperties")
}

tasks.register("packageSampleProject", Zip::class) {
    group = "build"

    archiveFileName.set("sample.zip")
    destinationDirectory.set(file("build/resources/main/"))
    from("src/main/resources/sample")
}


val generatedVersionDir = layout.buildDirectory.dir("generated-version")

sourceSets {
    named("main") {
        output.dir(mapOf("builtBy" to "generateVersionProperties"), generatedVersionDir.get().asFile)
    }
}

tasks.register("generateVersionProperties") {
    doLast {
        val propertiesFile = generatedVersionDir.get().file("version.properties").asFile
        propertiesFile.parentFile.mkdirs()
        val properties = Properties().apply {
            setProperty("version", rootProject.version.toString())
        }
        propertiesFile.outputStream().use { out -> properties.store(out, null) }
    }
}
