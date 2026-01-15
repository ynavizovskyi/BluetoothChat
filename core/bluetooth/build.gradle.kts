import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.ksp) version Versions.ksp
    id(Plugins.hiltAndroid)
    id(Plugins.kotlinSerialization) version Versions.kotlinVersion
    id(Plugins.parcelize)
}

android {
    namespace = "com.bluetoothchat.core.bluetooth"
    compileSdk = SdkConfig.compileSdk

    defaultConfig {
        minSdk = SdkConfig.minSdk
    }

    buildTypes {
        debug {
            buildConfigField("String", "APP_UUID", "\"5c8da380-5978-47e3-9299-29bf7d9ae728\"")
        }
        release {
            buildConfigField("String", "APP_UUID", "\"de2131d2-a6e9-457a-bfd5-5258762810bc\"")
        }
        create("releaseDebuggable") { initWith(getByName("release")) }
    }
    buildFeatures {
        buildConfig = true
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

    implementation(Libs.Kotlin.serialization)
    implementation(Libs.Kotlin.Coroutines.core)
    
    implementation(project(Module.Core.dispatcher))
    implementation(project(Module.Core.db))
    implementation(project(Module.Core.prefs))
    implementation(project(Module.Core.session))
    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.fileManager))
    implementation(project(Module.Core.permission))

}
