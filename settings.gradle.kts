pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Mozic"

include(":app")

// Core modules (shared foundation)
include(":core:common")
include(":core:domain")
include(":core:designsystem")
include(":core:ui")
include(":core:data")
include(":core:network")
include(":core:media")

// Feature modules (one vertical slice each)
include(":feature:home")
include(":feature:search")
include(":feature:playlists")
include(":feature:downloads")
include(":feature:library")
include(":feature:profile")
include(":feature:settings")
include(":feature:player")
include(":feature:chat")
include(":feature:social")
