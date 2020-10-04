import org.jetbrains.kotlin.util.parseSpaceSeparatedArgs

plugins {
    application
    kotlin("jvm")
    id("kotlin-spring")
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.noarg")
}

group = "lt.markmerkk"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("http://repo1.maven.org/maven2")
    maven("http://myrepo.net/repo")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    implementation("org.jetbrains.kotlin:kotlin-noarg:${Versions.kotlin}")
    implementation("org.jetbrains.kotlin:kotlin-maven-noarg:${Versions.kotlin}")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-data-rest:${Versions.springBoot}")

    // Other
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.7")
    implementation("commons-io:commons-io:2.5")
    implementation("com.google.guava:guava:21.0")
    implementation("io.reactivex.rxjava3:rxjava:3.0.6")

    // Monitoring
    implementation("org.slf4j:slf4j-api:1.7.27")

    testImplementation("junit:junit:4.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}")
    testImplementation("org.hamcrest:hamcrest-library:1.3")
    testImplementation("org.mockito:mockito-core:2.23.0")
    testImplementation("org.assertj:assertj-core:3.8.0")
    testImplementation("org.springframework:spring-test:4.3.5.RELEASE")
    testImplementation("org.springframework.boot:spring-boot-test:1.4.3.RELEASE")
    testImplementation("com.nhaarman:mockito-kotlin:1.5.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "lt.markmerkk.ReginaApp"
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    launchScript()
}

