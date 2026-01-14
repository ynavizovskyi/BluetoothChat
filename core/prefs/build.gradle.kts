import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.ksp) version Versions.ksp
    id(Plugins.parcelize)
}

android {
    namespace = "com.bluetoothchat.core.prefs"
    compileSdk = SdkConfig.compileSdk

    defaultConfig {
        minSdk = SdkConfig.minSdk
    }

    buildTypes {
        debug {}
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
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

    implementation(Libs.flowPreferences)

    implementation(project(Module.Core.dispatcher))
}
