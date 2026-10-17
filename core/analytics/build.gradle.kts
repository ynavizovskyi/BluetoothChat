import java.util.Properties
plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "com.bluetoothchat.core.analytics"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        val keystoreFile = project.rootProject.file("local.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())

        debug {
            buildConfigField("String", "AMPLITUDE_API_KEY", properties.getProperty("AMPLITUDE_API_KEY_DEBUG") ?: "\"\"")
        }
        release {
            buildConfigField("String", "AMPLITUDE_API_KEY", properties.getProperty("AMPLITUDE_API_KEY_RELEASE") ?: "\"\"")
        }
        create("releaseDebuggable") { initWith(getByName("release")) }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.koin.core)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    //For some reason amplitude client crashes without explicit okhttp dependency
    implementation(libs.okhttp)
    implementation(libs.amplitude.android.sdk)

    implementation(projects.core.prefs)
    implementation(projects.core.domain)
    implementation(projects.core.config)
    implementation(projects.core.dispatcher)
}
