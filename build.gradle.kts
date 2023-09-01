import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "com.neelkamath"

repositories { mavenCentral() }

dependencies { implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2") }

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "16" }

application { mainClass.set("MainKt") }
