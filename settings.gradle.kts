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

rootProject.name = "ImageForge"

include(":app")
include(":core:common")
include(":core:designsystem")
include(":core:imageprocessor")
include(":core:media")
include(":core:storage")
include(":core:ocr")
include(":feature:dashboard")
include(":feature:editor")
include(":feature:documents")
