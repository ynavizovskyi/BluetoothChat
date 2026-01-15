import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.ksp) version Versions.ksp
}

android {
    namespace = "com.bluetoothchat.core.analytics"
    compileSdk = SdkConfig.compileSdk

    defaultConfig {
        minSdk = SdkConfig.minSdk
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        val keystoreFile = project.rootProject.file("local.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        debug {
            buildConfigField("String", "AMPLITUDE_API_KEY", properties.getProperty("AMPLITUDE_API_KEY_DEBUG") ?: "\"\"")
        }
        release {
            buildConfigField("String", "AMPLITUDE_API_KEY", properties.getProperty("AMPLITUDE_API_KEY_RELEASE") ?: "\"\"")
        }
        create("releaseDebuggable") { initWith(getByName("release")) }
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

    //For some reason amplitude client crashes without explicit okhttp dependency
    implementation(Libs.okhttp3)
    implementation(Libs.amplitude)

    implementation(project(Module.Core.prefs))
    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.config))
    implementation(project(Module.Core.dispatcher))
}
