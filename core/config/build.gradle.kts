import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.ksp) version Versions.ksp
}

android {
    namespace = "com.bluetoothchat.core.config"
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

    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Kotlin.Coroutines.core)

    implementation(platform(Libs.Firebase.bom))
    implementation(Libs.Firebase.config)
    implementation(Libs.Firebase.analytics)

    implementation(project(Module.Core.dispatcher))
}
