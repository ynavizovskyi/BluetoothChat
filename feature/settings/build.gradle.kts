plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinCompose)
    id(Plugins.parcelize)
}

android {
    namespace = "com.bluetoothchat.feature.settings"
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

    implementation(Libs.AndroidX.Lifecycle.runtumeCompose)

    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.analytics))
    implementation(project(Module.Core.ui))
    implementation(project(Module.Core.prefs))
    implementation(project(Module.Core.config))
    implementation(project(Module.Core.dispatcher))
    implementation(project(Module.Core.session))
}
