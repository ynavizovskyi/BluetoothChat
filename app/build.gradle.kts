import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    id(Plugins.androidApplication)
    id(Plugins.kotlinAndroid)
    id(Plugins.kotlinCompose)
    id(Plugins.googleServices)
    id(Plugins.crashlytics)
    id(Plugins.hiltAndroid)
    id(Plugins.ksp) version Versions.ksp
    id(Plugins.parcelize)
}

android {
    namespace = "com.bluetoothchat.app"
    compileSdk = SdkConfig.compileSdk

    defaultConfig {
        applicationId = SdkConfig.applicationId
        minSdk = SdkConfig.minSdk
        targetSdk = SdkConfig.targetSdk
        versionCode = AppVersion.code
        versionName = AppVersion.name
    }

    signingConfigs {
        val signingDebugProperties = "../signing/signing_debug.properties"
        if (project.file(signingDebugProperties).exists()) {
            val signingDebug = Properties().fromFile(project, signingDebugProperties)

            getByName("debug") {
                storeFile = project.file(signingDebug.getProperty("STORE_FILE"))
                storePassword = signingDebug.getProperty("STORE_PASSWORD")
                keyAlias = signingDebug.getProperty("KEY_ALIAS")
                keyPassword = signingDebug.getProperty("KEY_PASSWORD")
            }
        }
        val signingReleaseProperties = "../signing/signing_release.properties"
        if (project.file(signingReleaseProperties).exists()) {
            val signingRelease = Properties().fromFile(project, signingReleaseProperties)

            create("release") {
                storeFile = project.file(signingRelease.getProperty("STORE_FILE"))
                storePassword = signingRelease.getProperty("STORE_PASSWORD")
                keyAlias = signingRelease.getProperty("KEY_ALIAS")
                keyPassword = signingRelease.getProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
//            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-dev"
            //For Google Play upload
//            isDebuggable = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true

//            signingConfig = signingConfigs.getByName("release")

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        create("releaseDebuggable") {
            initWith(getByName("release"))

            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            versionNameSuffix = ".release-debuggable"
        }
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Need for module nav graph generation
ksp {
    arg("compose-destinations.mode", "navgraphs")
    arg("compose-destinations.moduleName", "app")
}

dependencies {
    compose()
    hiltAndroid()
    destinationsNavigation()

    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Destinations.core)
    implementation(Libs.Destinations.animationsCore)
    implementation(Libs.AndroidX.appcompat)
    implementation(Libs.AndroidX.coreSplashScreen)
    implementation(Libs.AndroidX.Compose.material)

    implementation(platform(Libs.Firebase.bom))
    implementation(Libs.Firebase.analytics)
    implementation(Libs.Firebase.crashlytics)

    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.dispatcher))
    implementation(project(Module.Core.session))
    implementation(project(Module.Core.db))
    implementation(project(Module.Core.bluetooth))
    implementation(project(Module.Core.ui))
    implementation(project(Module.Core.config))
    implementation(project(Module.Core.analytics))
    implementation(project(Module.Core.prefs))
    implementation(project(Module.Core.permission))
    implementation(project(Module.Core.fileManager))

    implementation(project(Module.Feature.main))
    implementation(project(Module.Feature.chat))
    implementation(project(Module.Feature.settings))
    implementation(project(Module.Feature.profile))
    implementation(project(Module.Feature.connect))

}
