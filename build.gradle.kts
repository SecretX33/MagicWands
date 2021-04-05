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
    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "codemc-repo"
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
    maven {
        name = "gradle-repo"
        url = uri("https://plugins.gradle.org/m2/")
    }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT") // Spigot API dependency
    compileOnly(fileTree("libs"))      // Spigot server dependency
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    val koin_version = "2.2.2"
    implementation("org.koin:koin-core:$koin_version")
    testCompileOnly("org.koin:koin-test:$koin_version")
    implementation("com.zaxxer:HikariCP:4.0.2")
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
    relocate("com.zaxxer.hikari", "com.github.secretx33.dependencies.hikari")
    relocate("okio", "com.github.secretx33.dependencies.moshi.okio")
    relocate("org.koin", "com.github.secretx33.dependencies.koin")
    relocate("org.slf4j", "com.github.secretx33.dependencies.slf4j")
    relocate("kotlin", "com.github.secretx33.dependencies.kotlin")
    relocate("kotlinx", "com.github.secretx33.dependencies.kotlinx")
    relocate("org.jetbrains", "com.github.secretx33.dependencies.jetbrains")
    relocate("org.intellij", "com.github.secretx33.dependencies.jetbrains.intellij")
    exclude("DebugProbesKt.bin")
    exclude("META-INF/**")
}

tasks.register<proguard.gradle.ProGuardTask>("proguard") {
    configuration("proguard-rules.pro")
}

tasks.build.get().finalizedBy(tasks.getByName("proguard"))

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

tasks.processResources {
    expand("name" to rootProject.name, "version" to project.version)
}
