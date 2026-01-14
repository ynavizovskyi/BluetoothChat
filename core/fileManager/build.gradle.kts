import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.parcelize)
    id(Plugins.ksp) version Versions.ksp
}

android {
    namespace = "com.bluetoothchat.core.filemanager"
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

    implementation(Libs.coilCompose)
    implementation(Libs.AndroidX.paletteKtx)

    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.dispatcher))
    
}
