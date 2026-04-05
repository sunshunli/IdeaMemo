pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/gradle-plugin/")
        maven("https://www.jitpack.io/")
        google()
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
        maven("https://maven.aliyun.com/repository/public/")
        maven("https://maven.aliyun.com/repository/google/")
        maven("https://jitpack.io")
        google()
        mavenCentral()
    }
}

rootProject.name = "Note"
include(":app")
