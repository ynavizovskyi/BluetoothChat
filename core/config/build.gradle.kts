plugins {
    id(Plugins.androidLibrary)
}

android {
    namespace = "com.bluetoothchat.core.config"
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
    koin()

    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Kotlin.Coroutines.core)

    implementation(platform(Libs.Firebase.bom))
    implementation(Libs.Firebase.config)
    implementation(Libs.Firebase.analytics)

    implementation(project(Module.Core.dispatcher))
}
