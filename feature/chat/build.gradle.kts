plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id(Plugins.kapt)
    id(Plugins.ksp) version Versions.ksp
}

android {
    namespace = "com.bluetoothchat.feature.chat"
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Libs.AndroidX.Compose.versionCompiler
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

// Need for module nav graph generation
ksp {
    arg("compose-destinations.mode", "navgraphs")
    arg("compose-destinations.moduleName", "chat")
}

dependencies {
    compose()
    hiltAndroid()
    destinationsNavigation()

    implementation(Libs.AndroidX.Lifecycle.runtumeCompose)
    implementation(Libs.Accompanist.permissions)
    implementation(Libs.coilCompose)

    implementation(project(Module.Core.domain))
    implementation(project(Module.Core.db))
    implementation(project(Module.Core.bluetooth))
    implementation(project(Module.Core.session))
    implementation(project(Module.Core.ui))
    implementation(project(Module.Core.fileManager))
    implementation(project(Module.Core.analytics))
    implementation(project(Module.Core.dispatcher))
    implementation(project(Module.Core.permission))
}
