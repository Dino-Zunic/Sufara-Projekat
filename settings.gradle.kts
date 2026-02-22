pluginManagement {
    repositories {
        google()
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

rootProject.name = "Sufara Projekat"
include(":app")
include(":core:designsystem")
include(":core:common")
include(":core:database")
include(":core:network")
include(":feature:lesson")
include(":feature:map")
include(":feature:spectrogram")
