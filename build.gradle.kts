val miraiCoreVersion = "2.0-M1"
val miraiConsoleVersion = "2.0-M1"

plugins {
    val kotlinVersion = "1.4.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    java
    id("net.mamoe.mirai-console") version "2.0-M1"
}

group = "io.github.Rosemoe"
version = "2.1.2"

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("net.mamoe:mirai-core-api:$miraiCoreVersion")
    compileOnly("net.mamoe:mirai-console:$miraiConsoleVersion")
    implementation("com.squareup:gifencoder:0.10.1")
    implementation("org.json:org.json:2.0")

    testImplementation(kotlin("stdlib-jdk8"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}