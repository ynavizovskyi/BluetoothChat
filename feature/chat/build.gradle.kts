plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinCompose)
    id(Plugins.kotlinSerialization)
}

android {
    namespace = "com.bluetoothchat.feature.chat"
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
}

dependencies {
    compose()
    koinCompose()

    implementation(Libs.Kotlin.serialization)

    implementation(Libs.AndroidX.Lifecycle.runtumeCompose)
    implementation(Libs.AndroidX.Compose.materialIcons)
    implementation(Libs.Accompanist.permissions)
    implementation(Libs.coilCompose)

    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.db))
    implementation(project(Module.Core.bluetooth))
    implementation(project(Module.Core.session))
    implementation(project(Module.Core.ui))
    implementation(project(Module.Core.fileManager))
    implementation(project(Module.Core.analytics))
    implementation(project(Module.Core.dispatcher))
    implementation(project(Module.Core.permission))
}
