pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "liyaqa-gym-management"

// Backend modules
include("backend")
include("backend:backend-domain")
include("backend:backend-application")
include("backend:backend-infrastructure")
include("backend:backend-presentation")
include("backend:backend-common")

// Mobile modules
include("mobile:shared")
include("mobile:androidApp")
include("mobile:iosApp")

// Web module
include("web")

// Enable Gradle version catalogs
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
