plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinParcelize)
}

android {
    namespace = "com.bluetoothchat.core.filemanager"
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
    implementation(libs.androidx.palette.ktx)

    implementation(projects.core.domain)
    implementation(projects.core.dispatcher)
    
}
