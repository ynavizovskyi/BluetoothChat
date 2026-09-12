plugins {
    id(Plugins.androidLibrary)
    id(Plugins.parcelize)
}

android {
    namespace = "com.bluetoothchat.core.filemanager"
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

    implementation(Libs.AndroidX.coreKtx)
    implementation(Libs.coilCompose)
    implementation(Libs.AndroidX.paletteKtx)

    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.dispatcher))
    
}
