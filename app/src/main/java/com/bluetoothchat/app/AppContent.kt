package com.bluetoothchat.app

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign
import com.bluetoothchat.app.deeplink.DeeplinkEvent
import com.bluetoothchat.app.deeplink.DeeplinkManager
import com.bluetoothchat.app.deeplink.DeeplinkNavigatorImpl
import com.bluetoothchat.app.navigation.AddUsersNavigatorImpl
import com.bluetoothchat.app.navigation.ConnectNavigatorImpl
import com.bluetoothchat.app.navigation.CreateGroupNavigatorImpl
import com.bluetoothchat.app.navigation.GroupChatInfoNavigatorImpl
import com.bluetoothchat.app.navigation.GroupChatNavigatorImpl
import com.bluetoothchat.app.navigation.MainNavigatorImpl
import com.bluetoothchat.app.navigation.PrivateChatNavigatorImpl
import com.bluetoothchat.app.navigation.ProfileNavigatorImpl
import com.bluetoothchat.app.navigation.RootNavGraph
import com.bluetoothchat.app.navigation.SettingsNavigatorImpl
import com.bluetoothchat.app.navigation.ViewImageNavigatorImpl
import com.bluetoothchat.app.splash.SplashScreenNavigatorImpl
import com.bluetoothchat.app.splash.destinations.SplashScreenDestination
import com.bluetoothchat.core.ui.theme.ChatAppTheme
import com.bluetoothchat.app.ui.shouldUseDarkColors
import com.bluetoothchat.core.prefs.settings.AppSettingsPrefs
import com.bluetoothchat.core.ui.components.dialog.model.DialogResult
import com.bluetoothchat.core.ui.destinations.DialogDestination
import com.bluetoothchat.core.ui.navigation.screenEnterTransition
import com.bluetoothchat.core.ui.navigation.screenExitTransition
import com.bluetoothchat.core.ui.navigation.screenPopEnterTransition
import com.bluetoothchat.core.ui.navigation.screenPopExitTransition
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.bluetoothchat.feature.chat.destinations.GroupChatInfoScreenDestination
import com.bluetoothchat.feature.chat.destinations.GroupChatScreenDestination
import com.bluetoothchat.feature.chat.destinations.PrivateChatScreenDestination
import com.bluetoothchat.feature.chat.destinations.ViewImageScreenDestination
import com.bluetoothchat.feature.connect.destinations.AddUsersScreenDestination
import com.bluetoothchat.feature.connect.destinations.ConnectScreenDestination
import com.bluetoothchat.feature.connect.destinations.CreateGroupScreenDestination
import com.bluetoothchat.feature.main.destinations.MainScreenDestination
import com.bluetoothchat.feature.profile.destinations.ProfileScreenDestination
import com.bluetoothchat.feature.settings.ui.destinations.SettingsScreenDestination
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.scope.resultRecipient
import com.ramcosta.composedestinations.spec.NavHostEngine

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterialNavigationApi::class,
    ExperimentalMaterialApi::class,
)
@Composable
internal fun AppContent(
    appSettingsPrefs: AppSettingsPrefs,
    deeplinkManager: DeeplinkManager,
    navControllerListener: NavController.OnDestinationChangedListener,
) {
    val navHostEngine: NavHostEngine = rememberAnimatedNavHostEngine(
        rootDefaultAnimations = RootNavGraphDefaultAnimations(
            enterTransition = { screenEnterTransition() },
            exitTransition = { screenExitTransition() },
            popEnterTransition = { screenPopEnterTransition() },
            popExitTransition = { screenPopExitTransition() }
        )
    )
    val navController = navHostEngine.rememberNavController()
    navController.addOnDestinationChangedListener(navControllerListener)

    val sheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        animationSpec = SwipeableDefaults.AnimationSpec,
        skipHalfExpanded = true,
    )
    val bottomSheetNavigator = remember { BottomSheetNavigator(sheetState) }
    navController.navigatorProvider += bottomSheetNavigator

    ChatAppTheme(
        darkTheme = appSettingsPrefs.shouldUseDarkColors()
    ) {

//        val width = LocalConfiguration.current.screenWidthDp.dp
//        val navigationBarHeight = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
//        val height = width / 0.556f + navigationBarHeight
//        val screenshotModifier = Modifier.fillMaxWidth().height(height)

        ModalBottomSheetLayout(
            bottomSheetNavigator = bottomSheetNavigator,
            sheetShape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp),
            sheetBackgroundColor = LocalChatAppColorScheme.current.screenBackground,
            scrimColor = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.25f),
            modifier = Modifier.fillMaxSize()
//            modifier = screenshotModifier
        ) {
            DestinationsNavHost(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = LocalChatAppColorScheme.current.screenBackground),
                navGraph = RootNavGraph,
                engine = navHostEngine,
                navController = navController,
                dependenciesContainerBuilder = { this.AddDestinationDependencies() }
            )
        }

        ObserveEvents(navController = navController, deeplinkManager = deeplinkManager)
    }
}

@Composable
private fun DependenciesContainerBuilder<*>.AddDestinationDependencies() {
    dependency(SplashScreenDestination) {
        SplashScreenNavigatorImpl(
            navController = navController,
            dialogResultRecipient = resultRecipient<DialogDestination, DialogResult>(),
        )
    }
    dependency(PrivateChatScreenDestination) {
        PrivateChatNavigatorImpl(
            navController = navController,
            dialogResultRecipient = resultRecipient<DialogDestination, DialogResult>(),
        )
    }
    dependency(GroupChatScreenDestination) {
        GroupChatNavigatorImpl(
            navController = navController,
            dialogResultRecipient = resultRecipient<DialogDestination, DialogResult>(),
        )
    }
    dependency(GroupChatInfoScreenDestination) {
        GroupChatInfoNavigatorImpl(
            navController = navController,
            dialogResultRecipient = resultRecipient<DialogDestination, DialogResult>(),
        )
    }
    dependency(ViewImageScreenDestination) {
        ViewImageNavigatorImpl(
            navController = navController,
            dialogResultRecipient = resultRecipient<DialogDestination, DialogResult>(),
        )
    }
    dependency(MainScreenDestination) {
        MainNavigatorImpl(
            navController = navController,
            dialogResultRecipient = resultRecipient<DialogDestination, DialogResult>(),
        )
    }
    dependency(ConnectScreenDestination) {
        ConnectNavigatorImpl(
            navController = navController,
            dialogResultRecipient = resultRecipient<DialogDestination, DialogResult>(),
        )
    }
    dependency(AddUsersScreenDestination) {
        AddUsersNavigatorImpl(
            navController = navController,
            dialogResultRecipient = resultRecipient<DialogDestination, DialogResult>(),
        )
    }
    dependency(CreateGroupScreenDestination) {
        CreateGroupNavigatorImpl(
            navController = navController,
            dialogResultRecipient = resultRecipient<DialogDestination, DialogResult>(),
        )
    }
    dependency(ProfileScreenDestination) {
        ProfileNavigatorImpl(
            navController = navController,
            dialogResultRecipient = resultRecipient<DialogDestination, DialogResult>(),
        )
    }
    dependency(SettingsScreenDestination) {
        SettingsNavigatorImpl(
            navController = navController,
            dialogResultRecipient = resultRecipient<DialogDestination, DialogResult>(),
        )
    }
}

@Composable
private fun ObserveEvents(
    navController: NavHostController,
    deeplinkManager: DeeplinkManager,
) {
    val navigator = remember(navController) {
        DeeplinkNavigatorImpl(navController)
    }

    deeplinkManager.deeplinkEvents.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is DeeplinkEvent.NavigateToConnectScreen -> navigator.navigateToConnectScreen()
            is DeeplinkEvent.NavigateToPrivateChat -> navigator.navigateToPrivateChat(event.chatId)
            is DeeplinkEvent.NavigateToGroupChat -> navigator.navigateToGroupChat(event.chatId)
        }
    }
}
