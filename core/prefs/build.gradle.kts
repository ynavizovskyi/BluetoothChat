plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinParcelize)
}

android {
    namespace = "com.bluetoothchat.core.prefs"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
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
    implementation(libs.koin.core)

    implementation(libs.flow.preferences)

    implementation(projects.core.dispatcher)
}
