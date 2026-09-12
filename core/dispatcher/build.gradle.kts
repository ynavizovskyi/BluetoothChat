plugins {
    id(Plugins.androidLibrary)
    id(Plugins.ksp)
}

android {
    namespace = "com.bluetoothchat.core.dispatcher"
    compileSdk = SdkConfig.compileSdk

    defaultConfig {
        minSdk = SdkConfig.minSdk
    }

    buildTypes {
        debug {}
        release {}
        create("releaseDebuggable") {}
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    hiltAndroid()

}

