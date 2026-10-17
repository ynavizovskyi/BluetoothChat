plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinParcelize)
}

android {
    namespace = "com.bluetoothchat.core.bluetooth"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    buildTypes {
        debug {
            buildConfigField("String", "APP_UUID", "\"5c8da380-5978-47e3-9299-29bf7d9ae728\"")
        }
        release {
            buildConfigField("String", "APP_UUID", "\"de2131d2-a6e9-457a-bfd5-5258762810bc\"")
        }
        create("releaseDebuggable") { initWith(getByName("release")) }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.koin.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    
    implementation(projects.core.dispatcher)
    implementation(projects.core.db)
    implementation(projects.core.prefs)
    implementation(projects.core.session)
    implementation(projects.core.domain)
    implementation(projects.core.fileManager)
    implementation(projects.core.permission)

}
