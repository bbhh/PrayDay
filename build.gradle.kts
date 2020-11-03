import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"

    application
}

group = "com.biblefoundry.prayday"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "com.biblefoundry.prayday.AppKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.apache.logging.log4j:log4j-api:2.13.3")
    implementation("org.apache.logging.log4j:log4j-core:2.13.3")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.0.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")

    implementation(platform("software.amazon.awssdk:bom:2.15.18"))
    implementation("software.amazon.awssdk:dynamodb")
    implementation("software.amazon.awssdk:pinpoint")

    implementation("com.github.ajalt:clikt:2.8.0")

    implementation("com.uchuhimo:konf:0.23.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

    testImplementation(kotlin("test-junit"))
}

distributions {
    main {
        contents {
            from("src/main/resources") {
                into("src/main/resources")
            }
        }
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}