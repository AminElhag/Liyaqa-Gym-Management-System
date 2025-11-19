plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

dependencies {
    // Internal dependencies
    implementation(project(":backend:backend-domain"))
    implementation(project(":backend:backend-application"))
    implementation(project(":backend:backend-common"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Spring Data
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka")

    // Database
    implementation("org.postgresql:postgresql:${rootProject.extra["postgresqlVersion"]}")

    // Jackson for serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.mockk:mockk:${rootProject.extra["mockkVersion"]}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql:${rootProject.extra["testcontainersVersion"]}")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
