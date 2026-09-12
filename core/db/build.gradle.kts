plugins {
    id(Plugins.androidLibrary)
    id(Plugins.ksp)
}

android {
    namespace = "com.bluetoothchat.core.db"
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

    implementation(Libs.Room.runtime)
    implementation(Libs.Room.ktx)
    ksp(Libs.Room.compiler)

    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.dispatcher))

}
