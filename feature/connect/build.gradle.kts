plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.bluetoothchat.feature.connect"
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

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.compose.material.icons.core)

    implementation(libs.coil.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(projects.core.domain)
    implementation(projects.core.ui)
    implementation(projects.core.db)
    implementation(projects.core.session)
    implementation(projects.core.fileManager)
    implementation(projects.core.bluetooth)
    implementation(projects.core.dispatcher)
    implementation(projects.core.permission)
    implementation(projects.core.analytics)
}
