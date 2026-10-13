plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinCompose)
    id(Plugins.kotlinSerialization)
}

android {
    namespace = "com.bluetoothchat.feature.connect"
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

    implementation(Libs.AndroidX.Compose.materialIcons)

    implementation(Libs.coilCompose)
    implementation(Libs.Accompanist.permissions)
    implementation(Libs.AndroidX.Lifecycle.runtumeCompose)

    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.ui))
    implementation(project(Module.Core.db))
    implementation(project(Module.Core.session))
    implementation(project(Module.Core.fileManager))
    implementation(project(Module.Core.bluetooth))
    implementation(project(Module.Core.dispatcher))
    implementation(project(Module.Core.permission))
    implementation(project(Module.Core.analytics))
}
