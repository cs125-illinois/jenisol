import java.io.File
import java.io.StringWriter
import java.util.Properties

group = "com.github.cs125-illinois"
version = "2021.5.1"

plugins {
    kotlin("jvm") version "1.5.0"
    java
    `maven-publish`

    id("org.jmailen.kotlinter") version "3.4.4"
    checkstyle
    id("com.github.sherter.google-java-format") version "0.9"

    id("com.github.ben-manes.versions") version "0.38.0"
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
}
repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}
dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("io.github.classgraph:classgraph:4.8.105")
    implementation("io.github.kostaskougios:cloning:1.10.3")

    testImplementation("io.kotest:kotest-runner-junit5:4.5.0")
    testImplementation("org.slf4j:slf4j-simple:1.7.30")
}
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
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
configurations.all {
    resolutionStrategy {
        force(
            "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.0",
            "org.jetbrains.kotlin:kotlin-script-runtime:1.5.0"
        )
    }
}
tasks.dependencyUpdates {
    fun String.isNonStable() = !(
        listOf("RELEASE", "FINAL", "GA").any { toUpperCase().contains(it) }
            || "^[0-9,.v-]+(-r)?$".toRegex().matches(this)
        )
    rejectVersionIf { candidate.version.isNonStable() }
    gradleReleaseChannel = "current"
}
detekt {
    buildUponDefaultConfig = true
}
tasks.check {
    dependsOn("detekt")
}
googleJavaFormat {
    toolVersion = "1.10.0"
}
tasks.compileKotlin {
    dependsOn("createProperties")
}
tasks.compileTestKotlin {
    kotlinOptions {
        javaParameters = true
    }
}
task("createProperties") {
    dependsOn(tasks.processResources)
    doLast {
        val properties = Properties().also {
            it["version"] = project.version.toString()
        }
        File(projectDir, "src/main/resources/edu.illinois.cs.cs125.jenisol.core.version")
            .printWriter().use { printWriter ->
                printWriter.print(
                    StringWriter().also { properties.store(it, null) }.buffer.toString()
                        .lines().drop(1).joinToString(separator = "\n").trim()
                )
            }
    }
}
java {
    withSourcesJar()
}
publishing {
    publications {
        create<MavenPublication>("jenisol") {
            from(components["java"])
        }
    }
}
