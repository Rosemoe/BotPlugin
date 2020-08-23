plugins {
    kotlin("jvm") version "1.3.72"
    java
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "com.rose"
version = "0.1.0"

repositories {
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    maven(url = "https://mirrors.huaweicloud.com/repository/maven")
    mavenCentral()
    jcenter()
}

val miraiCoreVersion = "+"
val miraiConsoleVersion = "0.5.2"

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
    sourceCompatibility = JavaVersion.VERSION_14
    targetCompatibility = JavaVersion.VERSION_14
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
                            .transferTo(File("$targetDirectory/${it.name}").apply { createNewFile() }
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