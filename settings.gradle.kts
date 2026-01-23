enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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

rootProject.name = "BuyOrNot"
include(":app")
include(":domain")
include(":core:data")
include(":core:network")
include(":core:datastore")
include(":core:ui")
include(":core:designsystem")
include(":feature:auth")
include(":feature:home")
include(":feature:upload")
include(":feature:mypage")

includeBuild("build-logic")
 
