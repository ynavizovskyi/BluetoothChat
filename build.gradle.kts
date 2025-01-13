// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id(Plugins.androidApplication) version BuildPlugins.gradleVersion apply false
    id(Plugins.androidLibrary) version BuildPlugins.gradleVersion apply false
    id(Plugins.kotlinAndroid) version Versions.kotlinVersion apply false
    id(Plugins.ksp) version Versions.ksp apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(BuildPlugins.androidGradle)
        classpath(BuildPlugins.kotlinGradlePlugin)
        classpath(BuildPlugins.hiltGradlePlugin)
        classpath(BuildPlugins.crashlytics)
        classpath(BuildPlugins.googleServicesPlugin)
    }
}

