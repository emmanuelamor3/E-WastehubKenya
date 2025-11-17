pluginManagement {
    repositories {
        // This block is simplified to remove the restrictive content filter
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

rootProject.name = "E-WastehubKenya"
include(":app")
// include(":common") // Module no longer needed after migration to Firebase
