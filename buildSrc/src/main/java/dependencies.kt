object AppVersion {
    const val code = 8
    const val name = "1.1.4"
}

object SdkConfig {
    //Android 8.0, support 93% of devices as of November 2023
    const val minSdk = 26
    const val targetSdk = 34
    const val compileSdk = 34
    const val applicationId = "com.bluetoothchat.app"
}

object BuildPlugins {
    const val gradleVersion = "8.3.1"
    const val androidGradle = "com.android.tools.build:gradle:$gradleVersion"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Libs.Kotlin.version}"
    const val hiltGradlePlugin = "com.google.dagger:hilt-android-gradle-plugin:${Libs.Hilt.version}"
    const val crashlytics = "com.google.firebase:firebase-crashlytics-gradle:2.9.9"
    const val googleServicesPlugin = "com.google.gms:google-services:4.4.1"
}

object Libs {
    const val coilCompose = "io.coil-kt:coil-compose:2.6.0"
    const val flowPreferences = "com.fredporciuncula:flow-preferences:1.9.1"
    const val okhttp3 = "com.squareup.okhttp3:okhttp:4.12.0"
    const val amplitude = "com.amplitude:android-sdk:2.39.9"
    const val playReview = "com.google.android.play:review-ktx:2.0.1"

    object Kotlin {
        const val version = Versions.kotlinVersion
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
        const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3"

        object Coroutines {
            private const val coroutinesVersion = "1.8.0"
            const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
        }
    }

    object Hilt {
        const val version = "2.51.1"
        const val android = "com.google.dagger:hilt-android:$version"
        const val androidCompiler = "com.google.dagger:hilt-android-compiler:$version"
    }

    object Room {
        private const val version = "2.6.1"
        const val runtime = "androidx.room:room-runtime:$version"
        const val compiler = "androidx.room:room-compiler:$version"
        const val ktx = "androidx.room:room-ktx:$version"
    }

    object Accompanist {
        private const val version = "0.34.0"
        const val permissions = "com.google.accompanist:accompanist-permissions:$version"
    }

    object Destinations {
        private const val version = "1.10.0" //Update to 2
        const val core = "io.github.raamcosta.compose-destinations:core:${version}"
        const val animationsCore = "io.github.raamcosta.compose-destinations:animations-core:${version}"
        const val ksp = "io.github.raamcosta.compose-destinations:ksp:${version}"
    }

    object AndroidX {
        const val appcompat = "androidx.appcompat:appcompat:1.6.1"
        const val coreSplashScreen = "androidx.core:core-splashscreen:1.0.1"
        const val paletteKtx = "androidx.palette:palette-ktx:1.0.0"

        object Compose {
            const val versionCompiler = "1.5.14"

            const val bom = "androidx.compose:compose-bom:2024.10.01"
            const val ui = "androidx.compose.ui:ui"
            const val uiUtil = "androidx.compose.ui:ui-util"
            const val uiTooling = "androidx.compose.ui:ui-tooling"
            const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
            const val material3 = "androidx.compose.material3:material3:1.2.1"

            //TODO: needed for destination bottom sheet; consider removing
            const val material = "androidx.compose.material:material"
            const val runtime = "androidx.compose.runtime:runtime"
            const val liveData = "androidx.compose.runtime:runtime-livedata"
        }

        object Lifecycle {
            private const val version = "2.7.0"
            const val runtumeCompose = "androidx.lifecycle:lifecycle-runtime-compose:$version"
            const val viewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:$version"
        }

        object Hilt {
            const val compose = "androidx.hilt:hilt-navigation-compose:1.2.0"
            const val compiler = "androidx.hilt:compiler:1.2.0"
        }
    }

    object Firebase {
        private const val version = "32.7.0"
        const val bom = "com.google.firebase:firebase-bom:$version"
        const val core = "com.google.firebase:firebase-core"
        const val config = "com.google.firebase:firebase-config-ktx"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
    }

}
