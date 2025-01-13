plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.kapt)
}

android {
    namespace = "com.bluetoothchat.core.session"
    compileSdk = SdkConfig.compileSdk

    defaultConfig {
        minSdk = SdkConfig.minSdk
    }

    buildTypes {
        debug {}
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        create("releaseDebuggable") {}
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    hiltAndroid()

    implementation(Libs.Firebase.crashlytics)

    implementation(project(Module.Core.analytics))
    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.dispatcher))
    implementation(project(Module.Core.db))
    implementation(project(Module.Core.prefs))
    implementation(project(Module.Core.config))
}
