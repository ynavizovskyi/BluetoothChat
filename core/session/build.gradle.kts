plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "com.bluetoothchat.core.session"
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

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    implementation(projects.core.analytics)
    implementation(projects.core.domain)
    implementation(projects.core.dispatcher)
    implementation(projects.core.db)
    implementation(projects.core.prefs)
    implementation(projects.core.config)
}
