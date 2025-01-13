package com.bluetoothchat.app

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.bluetoothchat.app.deeplink.DeeplinkManager
import com.bluetoothchat.app.splash.destinations.SplashScreenDestination
import com.bluetoothchat.core.bluetooth.notification.ActivityKiller
import com.bluetoothchat.core.bluetooth.notification.ChatAppDeeplink
import com.bluetoothchat.core.prefs.settings.AppSettingsPrefs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var deeplinkManager: DeeplinkManager

    @Inject
    lateinit var appSettingsPrefs: AppSettingsPrefs

    @Inject
    lateinit var activityKiller: ActivityKiller

    private var displaySplashScreen = true

    private val navControllerListener = object : NavController.OnDestinationChangedListener {
        override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
            if (destination.route != SplashScreenDestination.route) {
                displaySplashScreen = false
                controller.removeOnDestinationChangedListener(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setUpSplashScreen()
        super.onCreate(savedInstanceState)
        activityKiller.setActivity(this)
        setupSystemBars()

        if (intent != null && (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0)) {
            val deeplink = intent.getParcelableExtra<ChatAppDeeplink>(INPUT_PARAMS)
            if (deeplink != null) {
                deeplinkManager.onNewDeeplink(deeplink = deeplink)
            }
        }

        setContent {
            AppContent(
                appSettingsPrefs = appSettingsPrefs,
                deeplinkManager = deeplinkManager,
                navControllerListener = navControllerListener,
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent != null) {
            val deeplink = intent.getParcelableExtra<ChatAppDeeplink>(INPUT_PARAMS)
            if (deeplink != null) {
                deeplinkManager.onNewDeeplink(deeplink = deeplink)
            }
        }
    }

    private fun setupSystemBars() {
        window.apply {
            WindowCompat.setDecorFitsSystemWindows(this, false)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            } else {
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            }
        }
    }

    private fun setUpSplashScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            installSplashScreen()
            val content = findViewById<View>(android.R.id.content)
            content.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        return if (displaySplashScreen) {
                            false
                        } else {
                            content.viewTreeObserver.removeOnPreDrawListener(this)
                            true
                        }
                    }
                }
            )
        }
    }

    companion object {
        const val INPUT_PARAMS = "INPUT_PARAMS"

        fun createIntent(context: Context, inputParams: ChatAppDeeplink): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(INPUT_PARAMS, inputParams)
            return intent
        }
    }
}

