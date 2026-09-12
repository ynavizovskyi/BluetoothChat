plugins {
    id(Plugins.androidLibrary)
    id(Plugins.ksp)
    id(Plugins.parcelize)
}

android {
    namespace = "com.bluetoothchat.core.prefs"
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

    implementation(Libs.flowPreferences)

    implementation(project(Module.Core.dispatcher))
}
