plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.1.0"
    id("com.android.library")
    id("app.cash.sqldelight") version "2.0.2"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "21"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.extra["coroutinesVersion"]}")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${rootProject.extra["serializationVersion"]}")

                // Ktor for networking
                implementation("io.ktor:ktor-client-core:${rootProject.extra["ktorVersion"]}")
                implementation("io.ktor:ktor-client-content-negotiation:${rootProject.extra["ktorVersion"]}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${rootProject.extra["ktorVersion"]}")
                implementation("io.ktor:ktor-client-logging:${rootProject.extra["ktorVersion"]}")
                implementation("io.ktor:ktor-client-auth:${rootProject.extra["ktorVersion"]}")

                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

                // SQLDelight
                implementation("app.cash.sqldelight:runtime:2.0.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")

                // Koin for dependency injection
                implementation("io.insert-koin:koin-core:3.5.3")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${rootProject.extra["coroutinesVersion"]}")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:${rootProject.extra["ktorVersion"]}")
                implementation("app.cash.sqldelight:android-driver:2.0.2")
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("io.ktor:ktor-client-darwin:${rootProject.extra["ktorVersion"]}")
                implementation("app.cash.sqldelight:native-driver:2.0.2")
            }
        }
    }
}

sqldelight {
    databases {
        create("LiyaqaDatabase") {
            packageName.set("com.liyaqa.gym.database")
        }
    }
}

android {
    namespace = "com.liyaqa.gym.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
