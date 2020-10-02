import java.io.*
import java.util.*

plugins {
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.serialization") version "1.4.0"
    kotlin("kapt") version "1.4.0"
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "io.github.Rosemoe"
version = "2.0.2"

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
}

val miraiCoreVersion = "1.3.1"
val miraiConsoleVersion = "1.0-RC-dev-29"

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("net.mamoe:mirai-core:$miraiCoreVersion")
    compileOnly("net.mamoe:mirai-console:$miraiConsoleVersion")

    val autoService = "1.0-rc7"
    kapt("com.google.auto.service", "auto-service", autoService)
    compileOnly("com.google.auto.service", "auto-service-annotations", autoService)

    testImplementation(kotlin("stdlib-jdk8"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {

    val assemblePlugin by creating(DefaultTask::class.java) {
        group = "plugin"
        dependsOn(classes)
        dependsOn(shadowJar)

        val targetDirectory = "release"

        doFirst {
            fun removeOldVersions() {
                File("$targetDirectory/").walk()
                    .filter { it.name.matches(Regex("""(.|[.])*.jar""")) }
                    .forEach {
                        it.delete()
                        println("deleting old files: ${it.name}")
                    }
            }

            fun copyBuildOutput() {
                File("build/libs/").walk()
                    .filter { it.name.contains("-all") }
                    .maxBy { it.lastModified() }
                    ?.let {
                        println("Coping ${it.name}")
                        it.inputStream()
                            .transferTo1(File("$targetDirectory/${it.name}").apply { createNewFile() }
                                .outputStream())
                        println("Copied ${it.name}")
                    }
            }

            File("$targetDirectory/").mkdirs();
            removeOldVersions()
            copyBuildOutput()
        }
    }
}

kotlin.target.compilations.all {
    kotlinOptions.freeCompilerArgs += "-Xjvm-default=enable"
    kotlinOptions.jvmTarget = "1.8"
}

@Throws(IOException::class)
fun InputStream.transferTo1(out: OutputStream): Long {
    Objects.requireNonNull(out, "out")
    var transferred: Long = 0
    val buffer = ByteArray(8192)
    var read: Int
    while (this.read(buffer, 0, 8192).also { read = it } >= 0) {
        out.write(buffer, 0, read)
        transferred += read.toLong()
    }
    close()
    out.flush()
    out.close()
    return transferred
}