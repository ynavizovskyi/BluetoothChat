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

rootProject.name = "Bluetooth Chat"
include(":app")
include(":core:bluetooth")
include(":core:ui")
include(":core:domain")
include(":core:db")
include(":core:session")
include(":core:prefs")
include(":core:dispatcher")
include(":core:fileManager")
include(":core:config")
include(":core:analytics")
include(":core:permission")

include(":feature:main")
include(":feature:chat")
include(":feature:settings")
include(":feature:profile")
include(":feature:connect")
