import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.kotlinCompose)
    id(Plugins.ksp) version Versions.ksp
}

android {
    namespace = "com.bluetoothchat.feature.main"
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

    buildFeatures {
        compose = true
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

// Need for module nav graph generation
ksp {
    arg("compose-destinations.mode", "navgraphs")
    arg("compose-destinations.moduleName", "main")
}

dependencies {
    compose()
    hiltAndroid()
    destinationsNavigation()

    implementation(Libs.AndroidX.Lifecycle.runtumeCompose)
    implementation(Libs.AndroidX.Compose.materialIcons)
    implementation(Libs.Accompanist.permissions)
    implementation(Libs.coilCompose)

    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.db))
    implementation(project(Module.Core.ui))
    implementation(project(Module.Core.bluetooth))
    implementation(project(Module.Core.fileManager))
    implementation(project(Module.Core.session))
    implementation(project(Module.Core.dispatcher))
    implementation(project(Module.Core.permission))
    implementation(project(Module.Core.config))
    implementation(project(Module.Core.prefs))
    implementation(project(Module.Core.analytics))

}
