plugins {
    base
    kotlin("jvm")
}

buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${Versions.springBoot}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:${Versions.kotlin}")
        classpath("org.jetbrains.kotlin:kotlin-noarg:${Versions.kotlin}")
        classpath("com.github.jengelman.gradle.plugins:shadow:5.2.0")
        classpath("gradle.plugin.de.fuerstenau:BuildConfigPlugin:1.1.8")
    }
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }
}
