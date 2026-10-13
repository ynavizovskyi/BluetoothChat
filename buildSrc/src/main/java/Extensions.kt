import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import java.io.FileInputStream
import java.util.Properties

private const val implementation = "implementation"
private const val debugImplementation = "debugImplementation"

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

fun DependencyHandler.koin() {
    add(implementation, Libs.Koin.core)
}

fun DependencyHandler.koinCompose() {
    add(implementation, Libs.Koin.compose)
    add(implementation, Libs.Koin.composeViewModel)
}

fun DependencyHandler.navigation3() {
    add(implementation, Libs.Navigation3.runtime)
    add(implementation, Libs.Navigation3.ui)
    add(implementation, Libs.Navigation3.lifecycleViewModel) // Only :composeApp needs this dependency; clean up later
}
