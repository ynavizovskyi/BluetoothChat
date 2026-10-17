plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinParcelize)
}

android {
    namespace = "com.bluetoothchat.feature.settings"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.bundles.koin.compose)

    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(projects.core.domain)
    implementation(projects.core.analytics)
    implementation(projects.core.ui)
    implementation(projects.core.prefs)
    implementation(projects.core.config)
    implementation(projects.core.dispatcher)
    implementation(projects.core.session)
}
