import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import java.io.FileInputStream
import java.util.Properties

private const val implementation = "implementation"
private const val debugImplementation = "debugImplementation"
private const val kapt = "kapt"
private const val ksp = "ksp"

fun Properties.fromFile(project: Project, path: String): Properties {
    load(FileInputStream(project.file(path)))
    return this
}

fun DependencyHandler.compose() {
    add(implementation, Libs.AndroidX.Compose.bom)
    add(implementation, Libs.AndroidX.Compose.ui)
    add(implementation, Libs.AndroidX.Compose.uiUtil)
    add(implementation, Libs.AndroidX.Compose.uiToolingPreview)
    add(debugImplementation, Libs.AndroidX.Compose.uiTooling)
//    add(implementation, Libs.AndroidX.Compose.material)
    add(implementation, Libs.AndroidX.Compose.material3)
    add(implementation, Libs.AndroidX.Compose.runtime)
    add(implementation, Libs.AndroidX.Compose.liveData)
}

fun DependencyHandler.hiltAndroid() {
    add(implementation, Libs.AndroidX.Hilt.compose)
    add(implementation, Libs.Hilt.android)
    add(ksp, Libs.Hilt.androidCompiler)

    //https://github.com/google/dagger/issues/5059
    add(ksp, "org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.0")
}

fun DependencyHandler.destinationsNavigation() {
    add(implementation, Libs.Destinations.core)
    add(implementation, Libs.Destinations.animationsCore)
    add(ksp, Libs.Destinations.ksp)
}
