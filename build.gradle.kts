plugins {
    val kotlinVersion = "1.4.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    java
    id("net.mamoe.mirai-console") version "2.2.1"
}

group = "io.github.Rosemoe"
version = "2.2.2"

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    implementation("org.json:org.json:2.0")

    testImplementation(kotlin("stdlib-jdk8"))
}