package com.bluetoothchat.app

import android.content.Context
import com.bluetoothchat.core.bluetooth.message.model.ProtocolVersion
import com.bluetoothchat.core.domain.AppInfoProvider
import com.bluetoothchat.core.ui.R as CoreUiR

class AppInfoProviderImpl(
    private val context: Context,
) : AppInfoProvider {

    override fun getAppName() = context.getString(CoreUiR.string.app_name)

    override fun getDebugMenuAvailable() = DebugMenuLauncher.isAvailable

    override fun getAppVersion(): String = BuildConfig.VERSION_NAME

    override fun getProtocolVersion(): String = ProtocolVersion.toString()
}
