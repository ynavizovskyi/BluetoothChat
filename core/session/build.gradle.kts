import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.ksp) version Versions.ksp
}

android {
    namespace = "com.bluetoothchat.core.session"
    compileSdk = SdkConfig.compileSdk

    defaultConfig {
        minSdk = SdkConfig.minSdk
    }

    buildTypes {
        debug {}
        release {}
        create("releaseDebuggable") {}
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
}

dependencies {
    hiltAndroid()

    implementation(Libs.Firebase.crashlytics)

    implementation(project(Module.Core.analytics))
    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.dispatcher))
    implementation(project(Module.Core.db))
    implementation(project(Module.Core.prefs))
    implementation(project(Module.Core.config))
}
