import java.io.*
import java.util.*

plugins {
    kotlin("jvm") version "1.4.0"
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "io.github.Rosemoe"
version = "2.0.1"

repositories {
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    maven(url = "https://mirrors.huaweicloud.com/repository/maven")
    mavenCentral()
}

val miraiCoreVersion = "1.3.1"
val miraiConsoleVersion = "1.0-M4"

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("net.mamoe:mirai-core:$miraiCoreVersion")
    compileOnly("net.mamoe:mirai-console:$miraiConsoleVersion")

    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("net.mamoe:mirai-core:$miraiCoreVersion")
    testImplementation("net.mamoe:mirai-core-qqandroid:$miraiCoreVersion")
    testImplementation("net.mamoe:mirai-console:$miraiConsoleVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    val assemblePlugin by creating(DefaultTask::class.java) {
        group = "plugin"
        dependsOn(classes)
        dependsOn(shadowJar)

        val targetDirectory = "release"

        doFirst {
            fun removeOldVersions() {
                File("$targetDirectory/").walk()
                    .filter { it.name.matches(Regex("""Rose-.*-all.jar""")) }
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
    out.close()
    return transferred
}