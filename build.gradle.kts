plugins {
    java
    val kotlinVersion = "1.4.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "1.0-RC-1"
}

group = "io.github.Rosemoe"
version = "2.0.4"

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
}

val miraiCoreVersion = "1.3.3"
val miraiConsoleVersion = "1.0.0"

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("net.mamoe:mirai-core:$miraiCoreVersion")
    compileOnly("net.mamoe:mirai-console:$miraiConsoleVersion")
    testImplementation(kotlin("stdlib-jdk8"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}