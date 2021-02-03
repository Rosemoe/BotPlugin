plugins {
    val kotlinVersion = "1.4.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    java
    id("net.mamoe.mirai-console") version "2.3.1"
}

group = "io.github.Rosemoe"
version = "2.3.1"

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    implementation("org.json:org.json:2.0")
    implementation(files("libs/rhino-1.7.13.jar"))
    testImplementation(kotlin("stdlib-jdk8"))
}