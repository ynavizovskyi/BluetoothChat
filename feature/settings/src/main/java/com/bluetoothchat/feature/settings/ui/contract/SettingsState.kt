package com.bluetoothchat.feature.settings.ui.contract

import com.bluetoothchat.core.prefs.settings.model.ChatAppTheme
import com.bluetoothchat.core.ui.mvi.contract.ViewState

internal data class SettingsState(
    val appearanceSection: AppearanceSection? = null,
    val aboutSection: AboutSection? = null,
    val debugSection: DebugSection? = null,
) : ViewState

internal data class AppearanceSection(val theme: ChatAppTheme)

internal data class AboutSection(
    val appVersion: String,
    val protocolVersion: String,
)

internal data object DebugSection
