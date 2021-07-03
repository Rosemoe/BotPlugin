plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    java
    id("net.mamoe.mirai-console") version "2.6.6"
}

group = "io.github.Rosemoe"
version = "2.4.2"

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    compileOnly("net.mamoe:mirai-core-all:2.6.6")
    compileOnly(kotlin("stdlib-jdk8"))
    implementation("org.json:org.json:2.0")
    implementation(files("libs/rhino-1.7.13.jar"))
    testImplementation(kotlin("stdlib-jdk8"))
}