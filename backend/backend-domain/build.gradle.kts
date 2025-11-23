plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // JPA/Hibernate
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    // Spring (needed for annotations and Spring Data types)
    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-commons")

    // Logging
    implementation("org.slf4j:slf4j-api")

    // Common utilities
    implementation(project(":backend:backend-common"))

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.mockk:mockk:${rootProject.extra["mockkVersion"]}")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
