import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.4.32"
    kotlin("jvm") version kotlinVersion apply false
    id("org.jmailen.kotlinter") version "3.4.0" apply false
    id("com.github.sherter.google-java-format") version "0.9"
    id("com.github.ben-manes.versions") version "0.38.0"
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
}
allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven(url = "https://jitpack.io")
    }
}
subprojects {
    group = "com.github.cs125-illinois.jenisol"
    version = "2021.3.6"
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }
    tasks.withType<KotlinCompile> {
        val javaVersion = JavaVersion.VERSION_1_8.toString()
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        kotlinOptions {
            jvmTarget = javaVersion
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
        enableAssertions = true
        if (JavaVersion.current() >= JavaVersion.VERSION_11) {
            jvmArgs("-ea", "-Xmx1G", "-Xss256k", "--enable-preview")
        } else {
            jvmArgs("-ea", "-Xmx1G", "-Xss256k")
        }
    }
}
tasks.dependencyUpdates {
    resolutionStrategy {
        componentSelection {
            all {
                if (listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea", "eap", "release").any { qualifier ->
                        candidate.version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
                    }) {
                    reject("Release candidate")
                }
            }
        }
    }
    gradleReleaseChannel = "current"
}
detekt {
    input = files("core/src/main/kotlin")
    buildUponDefaultConfig = true
}
tasks.register("check") {
    dependsOn("detekt")
}
