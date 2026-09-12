plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinCompose)
}

android {
    namespace = "com.bluetoothchat.core.permission"
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
    koin()
    compose()

    implementation(Libs.AndroidX.coreKtx)
    implementation(Libs.Accompanist.permissions)
}
