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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MeterScan"
include(":app")
include(":feature:auth")

include(":core:designsystem")
include(":core:network")
include(":core:common")
include(":core:domain")
include(":core:feature-api")
include(":feature:main")
include(":feature:meters")
include(":feature:settings")
