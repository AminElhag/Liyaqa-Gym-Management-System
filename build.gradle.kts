plugins {
    kotlin("jvm") version "2.1.0" apply false
    kotlin("plugin.spring") version "2.1.0" apply false
    kotlin("plugin.jpa") version "2.1.0" apply false
    kotlin("multiplatform") version "2.1.0" apply false
    kotlin("android") version "2.1.0" apply false
    kotlin("kapt") version "2.1.0" apply false
    kotlin("plugin.compose") version "2.1.0" apply false
    id("org.springframework.boot") version "3.5.6" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("com.android.application") version "8.7.3" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

group = "com.liyaqa"
version = "0.0.1-SNAPSHOT"

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    // Only apply Kotlin JVM plugin to backend and web modules, not mobile
    if (!project.path.startsWith(":mobile")) {
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Version catalog defined inline
ext {
    set("springBootVersion", "3.5.6")
    set("kotlinVersion", "2.1.0")
    set("springCloudVersion", "2024.0.0")

    // Backend dependencies
    set("postgresqlVersion", "42.7.4")
    set("redisVersion", "3.5.6")
    set("kafkaVersion", "3.5.6")
    set("jjwtVersion", "0.12.6")
    set("springdocVersion", "2.8.0")

    // Testing
    set("mockkVersion", "1.13.13")
    set("testcontainersVersion", "1.20.4")

    // Kotlin Multiplatform
    set("ktorVersion", "3.0.3")
    set("coroutinesVersion", "1.10.1")
    set("serializationVersion", "1.8.0")

    // Frontend
    set("reactVersion", "18.3.1")
    set("typescriptVersion", "5.7.2")
}
