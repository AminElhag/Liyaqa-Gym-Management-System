plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    // Internal dependencies
    implementation(project(":backend:backend-domain"))
    implementation(project(":backend:backend-application"))
    implementation(project(":backend:backend-common"))

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Spring Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:${rootProject.extra["jjwtVersion"]}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${rootProject.extra["jjwtVersion"]}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${rootProject.extra["jjwtVersion"]}")

    // Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${rootProject.extra["springdocVersion"]}")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.mockk:mockk:${rootProject.extra["mockkVersion"]}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
