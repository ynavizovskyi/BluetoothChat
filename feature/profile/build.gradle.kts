import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.kotlinCompose)
    id(Plugins.ksp) version Versions.ksp
    id(Plugins.parcelize)
}

android {
    namespace = "com.bluetoothchat.feature.profile"
    compileSdk = SdkConfig.compileSdk

    defaultConfig {
        minSdk = SdkConfig.minSdk
    }

    buildTypes {
        debug {}
        release {}
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
    arg("compose-destinations.moduleName", "profile")
}

dependencies {
    compose()
    hiltAndroid()
    destinationsNavigation()

    implementation(Libs.AndroidX.Lifecycle.runtumeCompose)
    implementation(Libs.AndroidX.Compose.materialIcons)
    implementation(Libs.coilCompose)

    implementation(project(Module.Core.ui))
    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.db))
    implementation(project(Module.Core.bluetooth))
    implementation(project(Module.Core.session))
    implementation(project(Module.Core.fileManager))
    implementation(project(Module.Core.analytics))
    implementation(project(Module.Core.dispatcher))
}
