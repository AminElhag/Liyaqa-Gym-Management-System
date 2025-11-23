plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Internal dependencies
    implementation(project(":backend:backend-domain"))
    implementation(project(":backend:backend-common"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Spring
    implementation("org.springframework:spring-context:6.2.1")
    implementation("org.springframework:spring-tx:6.2.1")
    implementation("org.springframework.security:spring-security-core:6.4.2")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.retry:spring-retry")

    // Logging
    implementation("org.slf4j:slf4j-api")

    // Validation
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.mockk:mockk:${rootProject.extra["mockkVersion"]}")
    testImplementation("org.springframework:spring-test:6.2.1")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
