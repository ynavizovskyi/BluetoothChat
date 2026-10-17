import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinParcelize)
}

fun signingProperties(path: String) = Properties().apply { load(FileInputStream(project.file(path))) }

android {
    namespace = "com.bluetoothchat.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.bluetoothchat.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 10
        versionName = "1.2.0"
    }

    signingConfigs {
        val signingDebugProperties = "../signing/signing_debug.properties"
        if (project.file(signingDebugProperties).exists()) {
            val signingDebug = signingProperties(signingDebugProperties)

            getByName("debug") {
                storeFile = project.file(signingDebug.getProperty("STORE_FILE"))
                storePassword = signingDebug.getProperty("STORE_PASSWORD")
                keyAlias = signingDebug.getProperty("KEY_ALIAS")
                keyPassword = signingDebug.getProperty("KEY_PASSWORD")
            }
        }
        val signingReleaseProperties = "../signing/signing_release.properties"
        if (project.file(signingReleaseProperties).exists()) {
            val signingRelease = signingProperties(signingReleaseProperties)

            create("release") {
                storeFile = project.file(signingRelease.getProperty("STORE_FILE"))
                storePassword = signingRelease.getProperty("STORE_PASSWORD")
                keyAlias = signingRelease.getProperty("KEY_ALIAS")
                keyPassword = signingRelease.getProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
//            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-dev"
            //For Google Play upload
//            isDebuggable = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true

//            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        create("releaseDebuggable") {
            initWith(getByName("release"))

            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            versionNameSuffix = ".release-debuggable"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.koin.core)
    implementation(libs.bundles.koin.compose)
    implementation(libs.bundles.navigation3)

    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    implementation(projects.core.domain)
    implementation(projects.core.dispatcher)
    implementation(projects.core.session)
    implementation(projects.core.db)
    implementation(projects.core.bluetooth)
    implementation(projects.core.ui)
    implementation(projects.core.config)
    implementation(projects.core.analytics)
    implementation(projects.core.prefs)
    implementation(projects.core.permission)
    implementation(projects.core.fileManager)

    implementation(projects.feature.main)
    implementation(projects.feature.chat)
    implementation(projects.feature.settings)
    implementation(projects.feature.profile)
    implementation(projects.feature.connect)
}
