package com.bluetoothchat.app

import com.bluetoothchat.app.splash.splashModule
import com.bluetoothchat.core.analytics.coreAnalyticsModule
import com.bluetoothchat.core.bluetooth.coreBluetoothModule
import com.bluetoothchat.core.config.coreConfigModule
import com.bluetoothchat.core.db.coreDbModule
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.filemanager.coreFileManagerModule
import com.bluetoothchat.core.permission.corePermissionModule
import com.bluetoothchat.core.prefs.corePrefsModule
import com.bluetoothchat.core.session.coreSessionModule
import com.bluetoothchat.core.ui.coreUiModule
import com.bluetoothchat.feature.chat.featureChatModule
import com.bluetoothchat.feature.chat.group.chatinfo.featureGroupChatInfoModule
import com.bluetoothchat.feature.chat.group.featureGroupChatModule
import com.bluetoothchat.feature.chat.image.featureViewImageModule
import com.bluetoothchat.feature.chat.privat.featurePrivateChatModule
import com.bluetoothchat.feature.connect.group.addusers.featureAddUsersModule
import com.bluetoothchat.feature.connect.group.create.featureCreateGroupModule
import com.bluetoothchat.feature.connect.main.featureConnectModule
import com.bluetoothchat.feature.main.featureMainModule
import com.bluetoothchat.feature.profile.featureProfileModule
import com.bluetoothchat.feature.settings.featureSettingsModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

private val coreDispatcherModule = module {
    single { DispatcherManager() }
    single { ApplicationScope(get()) }
}

internal val appModules = listOf(
    appModule,
    coreDispatcherModule,
    corePrefsModule,
    coreConfigModule,
    coreDbModule,
    coreFileManagerModule,
    coreBluetoothModule,
    coreAnalyticsModule,
    coreSessionModule,
    corePermissionModule,
    coreUiModule,
    splashModule,
    featureMainModule,
    featureChatModule,
    featurePrivateChatModule,
    featureGroupChatModule,
    featureGroupChatInfoModule,
    featureViewImageModule,
    featureConnectModule,
    featureCreateGroupModule,
    featureAddUsersModule,
    featureProfileModule,
    featureSettingsModule,
)

fun initKoin(appModule: Module = module { }) {
    startKoin {
        modules(appModules + appModule)
    }
}
