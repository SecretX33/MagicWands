import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.0.1") {
            exclude("com.android.tools.build")
        }
    }
}

plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.github.secretx33"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
    maven { url = uri("https://plugins.gradle.org/m2/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT") // Spigot API dependency
    compileOnly(fileTree("libs"))      // Spigot server dependency
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    val koin_version = "2.2.2"
    implementation("org.koin:koin-core:$koin_version")
    testCompileOnly("org.koin:koin-test:$koin_version")
    implementation("com.zaxxer:HikariCP:4.0.3")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.4")
}

// Disables the normal jar task
tasks.jar { enabled = false }

// And enables shadowJar task
artifacts.archives(tasks.shadowJar)

tasks.shadowJar {
    archiveFileName.set(rootProject.name + ".jar")
    val dependencyPackage = "${rootProject.group}.dependencies.${rootProject.name.toLowerCase()}"
    relocate("com.zaxxer.hikari", "${dependencyPackage}.hikari")
    relocate("okio", ".${dependencyPackage}.moshi.okio")
    relocate("org.koin", "${dependencyPackage}.koin")
    relocate("org.slf4j", "${dependencyPackage}.slf4j")
    relocate("kotlin", "${dependencyPackage}.kotlin")
    relocate("kotlinx", "${dependencyPackage}.kotlinx")
    relocate("org.jetbrains", "${dependencyPackage}.jetbrains")
    relocate("org.intellij", "${dependencyPackage}.jetbrains.intellij")
    exclude("DebugProbesKt.bin")
    exclude("META-INF/**")
}

tasks.register<proguard.gradle.ProGuardTask>("proguard") {
    configuration("proguard-rules.pro")
}

//tasks.build.get().finalizedBy(tasks.getByName("proguard"))

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

tasks.processResources {
    expand("name" to rootProject.name, "version" to project.version)
}
