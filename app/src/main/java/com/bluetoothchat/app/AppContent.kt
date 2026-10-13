package com.bluetoothchat.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.bluetoothchat.app.deeplink.DeeplinkEvent
import com.bluetoothchat.app.deeplink.DeeplinkManager
import com.bluetoothchat.app.deeplink.DeeplinkNavigatorImpl
import com.bluetoothchat.app.navigation.AddUsersNavigatorImpl
import com.bluetoothchat.app.navigation.ConnectNavigatorImpl
import com.bluetoothchat.app.navigation.CreateGroupNavigatorImpl
import com.bluetoothchat.app.navigation.GroupChatInfoNavigatorImpl
import com.bluetoothchat.app.navigation.GroupChatNavigatorImpl
import com.bluetoothchat.app.navigation.MainNavigatorImpl
import com.bluetoothchat.app.navigation.NavRoutes
import com.bluetoothchat.app.navigation.PrivateChatNavigatorImpl
import com.bluetoothchat.app.navigation.ProfileNavigatorImpl
import com.bluetoothchat.app.navigation.ScreenTransition
import com.bluetoothchat.app.navigation.SettingsNavigatorImpl
import com.bluetoothchat.app.navigation.ViewImageNavigatorImpl
import com.bluetoothchat.app.splash.LocalSplashScreenNavigator
import com.bluetoothchat.app.splash.SplashScreen
import com.bluetoothchat.app.splash.SplashScreenNavigatorImpl
import com.bluetoothchat.app.ui.shouldUseDarkColors
import com.bluetoothchat.core.prefs.settings.AppSettingsPrefs
import com.bluetoothchat.core.ui.navigation.createNoAnimationMetadata
import com.bluetoothchat.core.ui.navigation.createPredictiveSlideInExitTransitionSpec
import com.bluetoothchat.core.ui.navigation.createSlideInEnterTransitionSpec
import com.bluetoothchat.core.ui.navigation.createSlideInExitTransitionSpec
import com.bluetoothchat.core.ui.theme.ChatAppTheme
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.bluetoothchat.feature.chat.group.GroupChatScreen
import com.bluetoothchat.feature.chat.group.LocalGroupChatNavigator
import com.bluetoothchat.feature.chat.group.chatinfo.GroupChatInfoScreen
import com.bluetoothchat.feature.chat.group.chatinfo.LocalGroupChatInfoNavigator
import com.bluetoothchat.feature.chat.image.LocalViewImageNavigator
import com.bluetoothchat.feature.chat.image.ViewImageScreen
import com.bluetoothchat.feature.chat.privat.LocalPrivateChatNavigator
import com.bluetoothchat.feature.chat.privat.PrivateChatScreen
import com.bluetoothchat.feature.connect.group.addusers.AddUsersScreen
import com.bluetoothchat.feature.connect.group.addusers.LocalAddUsersNavigator
import com.bluetoothchat.feature.connect.group.create.CreateGroupScreen
import com.bluetoothchat.feature.connect.group.create.LocalCreateGroupNavigator
import com.bluetoothchat.feature.connect.main.ConnectScreen
import com.bluetoothchat.feature.connect.main.LocalConnectNavigator
import com.bluetoothchat.feature.main.LocalMainNavigator
import com.bluetoothchat.feature.main.MainScreen
import com.bluetoothchat.feature.profile.LocalProfileNavigator
import com.bluetoothchat.feature.profile.ProfileScreen
import com.bluetoothchat.feature.settings.LocalSettingsNavigator
import com.bluetoothchat.feature.settings.ui.SettingsScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
internal fun AppContent(
    appSettingsPrefs: AppSettingsPrefs,
    deeplinkManager: DeeplinkManager,
    onSplashScreenClosed: () -> Unit,
) {
    ChatAppTheme(
        darkTheme = appSettingsPrefs.shouldUseDarkColors()
    ) {

//        val width = LocalConfiguration.current.screenWidthDp.dp
//        val navigationBarHeight = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
//        val height = width / 0.556f + navigationBarHeight
//        val screenshotModifier = Modifier.fillMaxWidth().height(height)

        val backStack = rememberNavBackStack(
            configuration = SavedStateConfiguration {
                serializersModule = SerializersModule {
                    polymorphic(NavKey::class) {
                        subclass(NavRoutes.SplashScreen::class, NavRoutes.SplashScreen.serializer())
                        subclass(NavRoutes.Main::class, NavRoutes.Main.serializer())
                        subclass(NavRoutes.Connect::class, NavRoutes.Connect.serializer())
                        subclass(NavRoutes.CreateGroup::class, NavRoutes.CreateGroup.serializer())
                        subclass(NavRoutes.AddUsers::class, NavRoutes.AddUsers.serializer())
                        subclass(NavRoutes.PrivateChat::class, NavRoutes.PrivateChat.serializer())
                        subclass(NavRoutes.GroupChat::class, NavRoutes.GroupChat.serializer())
                        subclass(NavRoutes.GroupChatInfo::class, NavRoutes.GroupChatInfo.serializer())
                        subclass(NavRoutes.ViewImage::class, NavRoutes.ViewImage.serializer())
                        subclass(NavRoutes.Profile::class, NavRoutes.Profile.serializer())
                        subclass(NavRoutes.Settings::class, NavRoutes.Settings.serializer())
                    }
                }
            },
            NavRoutes.SplashScreen,
        )

        val splashScreenClosed = remember(backStack.lastOrNull()) {
            backStack.lastOrNull() != null && backStack.lastOrNull() !is NavRoutes.SplashScreen
        }
        LaunchedEffect(splashScreenClosed) {
            if (splashScreenClosed) {
                onSplashScreenClosed()
            }
        }

        CompositionLocalProvider(
            LocalSplashScreenNavigator provides SplashScreenNavigatorImpl(backStack),
            LocalMainNavigator provides MainNavigatorImpl(backStack),
            LocalConnectNavigator provides ConnectNavigatorImpl(backStack),
            LocalCreateGroupNavigator provides CreateGroupNavigatorImpl(backStack),
            LocalAddUsersNavigator provides AddUsersNavigatorImpl(backStack),
            LocalPrivateChatNavigator provides PrivateChatNavigatorImpl(backStack),
            LocalGroupChatNavigator provides GroupChatNavigatorImpl(backStack),
            LocalGroupChatInfoNavigator provides GroupChatInfoNavigatorImpl(backStack),
            LocalViewImageNavigator provides ViewImageNavigatorImpl(backStack),
            LocalProfileNavigator provides ProfileNavigatorImpl(backStack),
            LocalSettingsNavigator provides SettingsNavigatorImpl(backStack),
        ) {
            NavDisplay(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = LocalChatAppColorScheme.current.screenBackground),
//                modifier = screenshotModifier
//                    .background(color = LocalChatAppColorScheme.current.screenBackground),
                backStack = backStack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                transitionSpec = createSlideInEnterTransitionSpec(),
                popTransitionSpec = createSlideInExitTransitionSpec(),
                predictivePopTransitionSpec = createPredictiveSlideInExitTransitionSpec(),
                entryProvider = { key ->
                    when (key) {
                        is NavRoutes.SplashScreen -> NavEntry(key = key, metadata = createNoAnimationMetadata()) {
                            SplashScreen()
                        }

                        is NavRoutes.Main -> NavEntry(
                            key = key,
                            metadata = when (key.screenTransition) {
                                ScreenTransition.DEFAULT -> emptyMap()
                                ScreenTransition.NONE -> createNoAnimationMetadata()
                            },
                        ) {
                            MainScreen()
                        }

                        is NavRoutes.Connect -> NavEntry(key) {
                            ConnectScreen(params = key.params)
                        }

                        is NavRoutes.CreateGroup -> NavEntry(key) {
                            CreateGroupScreen()
                        }

                        is NavRoutes.AddUsers -> NavEntry(key) {
                            AddUsersScreen(params = key.params)
                        }

                        is NavRoutes.PrivateChat -> NavEntry(key) {
                            PrivateChatScreen(params = key.params)
                        }

                        is NavRoutes.GroupChat -> NavEntry(key) {
                            GroupChatScreen(params = key.params)
                        }

                        is NavRoutes.GroupChatInfo -> NavEntry(key) {
                            GroupChatInfoScreen(params = key.params)
                        }

                        is NavRoutes.ViewImage -> NavEntry(key) {
                            ViewImageScreen(params = key.params)
                        }

                        is NavRoutes.Profile -> NavEntry(key) {
                            ProfileScreen(params = key.params)
                        }

                        is NavRoutes.Settings -> NavEntry(key) {
                            SettingsScreen()
                        }

                        else -> error("Unknown NavRoute: $key")
                    }
                },
            )
        }

        ObserveEvents(backStack = backStack, deeplinkManager = deeplinkManager)
    }
}

@Composable
private fun ObserveEvents(
    backStack: NavBackStack<NavKey>,
    deeplinkManager: DeeplinkManager,
) {
    val navigator = remember(backStack) {
        DeeplinkNavigatorImpl(backStack)
    }

    deeplinkManager.deeplinkEvents.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is DeeplinkEvent.NavigateToConnectScreen -> navigator.navigateToConnectScreen()
            is DeeplinkEvent.NavigateToPrivateChat -> navigator.navigateToPrivateChat(event.chatId)
            is DeeplinkEvent.NavigateToGroupChat -> navigator.navigateToGroupChat(event.chatId)
        }
    }
}
