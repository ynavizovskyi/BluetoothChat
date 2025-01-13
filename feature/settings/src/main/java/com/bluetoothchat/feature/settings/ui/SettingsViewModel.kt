package com.bluetoothchat.feature.settings.ui

import androidx.lifecycle.viewModelScope
import com.bluetoothchat.core.config.RemoteConfig
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.AppInfoProvider
import com.bluetoothchat.core.prefs.settings.AppSettingsPrefs
import com.bluetoothchat.core.prefs.settings.model.ChatAppTheme
import com.bluetoothchat.core.session.Session
import com.bluetoothchat.core.ui.components.dialog.model.DialogInputParams
import com.bluetoothchat.core.ui.components.dialog.model.DialogOption
import com.bluetoothchat.core.ui.components.dialog.model.DialogRadioButton
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.mvi.MviViewModel
import com.bluetoothchat.core.ui.util.PRIVACY_POLICY_URL
import com.bluetoothchat.core.ui.util.TERMS_OF_USE_URL
import com.bluetoothchat.feature.settings.SettingsAnalyticsClient
import com.bluetoothchat.feature.settings.nameResId
import com.bluetoothchat.feature.settings.ui.contract.AboutSection
import com.bluetoothchat.feature.settings.ui.contract.AppearanceSection
import com.bluetoothchat.feature.settings.ui.contract.DebugSection
import com.bluetoothchat.feature.settings.ui.contract.SettingsAction
import com.bluetoothchat.feature.settings.ui.contract.SettingsEvent
import com.bluetoothchat.feature.settings.ui.contract.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bluetoothchat.core.ui.R as CoreUiR

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val session: Session,
    private val settingsPrefs: AppSettingsPrefs,
    private val config: RemoteConfig,
    private val dispatcherManager: DispatcherManager,
    private val appInfoProvider: AppInfoProvider,
    private val analyticsClient: SettingsAnalyticsClient,
) : MviViewModel<SettingsState, SettingsAction, SettingsEvent>(
    SettingsState()
) {

    init {
        observeState()

        viewModelScope.launch {
            analyticsClient.reportScreenShown()
        }
    }

    private fun observeState() {
        settingsPrefs.observeAppTheme()
            .onEach { theme ->
                val appearanceSection = AppearanceSection(theme = theme)

                val aboutSection = AboutSection(
                    appVersion = appInfoProvider.getAppVersion(),
                    protocolVersion = appInfoProvider.getProtocolVersion(),
                )

                val debugSection = DebugSection
                setState {
                    copy(
                        appearanceSection = appearanceSection,
                        aboutSection = aboutSection,
                        //Nothing to show there for now
//                    debugSection = debugSection,
                    )
                }
            }
            .flowOn(dispatcherManager.default)
            .launchIn(viewModelScope)
    }

    override fun handleAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.BackClicked -> handleBackClicked()
            is SettingsAction.ThemeClicked -> handleThemeClicked(action)
            is SettingsAction.OnDialogResult -> handleDialogResult(action)
            is SettingsAction.PrivacyPolicyClicked -> handlePrivacyPolicyClicked(action)
            is SettingsAction.TermsOfUseClicked -> handleTermsOfUseClicked(action)
            is SettingsAction.ContactSupportClicked -> handleContactSupportClicked(action)
            is SettingsAction.OnContactSupportClientResolved -> handleOnContactSupportClientResolved(action)
            is SettingsAction.OnContactSupportClientNotFound -> handleOnContactSupportClientNotFound(action)
        }
    }

    private fun handleBackClicked() {
        sendEvent { SettingsEvent.NavigateBack }
    }

    private fun handleThemeClicked(action: SettingsAction.ThemeClicked) {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportChangeThemeClicked()

            val currentTheme = settingsPrefs.getAppTheme()
            val dialogParams = DialogInputParams.RadioGroupDialog(
                id = SELECT_THEME_DIALOG_ID,
                title = UiText.Resource(resId = CoreUiR.string.settings_theme),
                radioButtons = ChatAppTheme.values().map {
                    DialogRadioButton(
                        text = UiText.Resource(resId = it.nameResId()),
                        data = it,
                        isSelected = it == currentTheme
                    )
                },
            )

            sendEvent { SettingsEvent.ShowDialog(params = dialogParams) }
        }
    }

    private fun handleDialogResult(action: SettingsAction.OnDialogResult) {
        viewModelScope.launch(dispatcherManager.default) {
            when (action.result.dialogId) {
                SELECT_THEME_DIALOG_ID -> {
                    when (val option = action.result.option) {
                        is DialogOption.RadioButton -> {
                            val selectedTheme = option.data as ChatAppTheme
                            analyticsClient.reportThemeChanged(theme = selectedTheme)

                            settingsPrefs.setAppTheme(selectedTheme)
                        }

                        else -> Unit //Do nothing for now
                    }
                }
            }
        }
    }

    private fun handlePrivacyPolicyClicked(action: SettingsAction.PrivacyPolicyClicked) {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportPrivacyPolicyClicked()
            sendEvent { SettingsEvent.OpenUrl(PRIVACY_POLICY_URL) }
        }
    }

    private fun handleTermsOfUseClicked(action: SettingsAction.TermsOfUseClicked) {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportTermsOfUseClicked()
            sendEvent { SettingsEvent.OpenUrl(TERMS_OF_USE_URL) }
        }
    }

    private fun handleContactSupportClicked(action: SettingsAction.ContactSupportClicked) {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportContactSupportCLicked()

            val analyticsUserId = session.getAnalyticsUserId()
            sendEvent {
                SettingsEvent.OpenEmailClient(
                    emailAddress = SUPPORT_EMAIL,
                    emailSubject = "${appInfoProvider.getAppName()} support query $analyticsUserId"
                )
            }
        }
    }

    private fun handleOnContactSupportClientResolved(action: SettingsAction.OnContactSupportClientResolved) {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportContactSupportClientResolved()
        }
    }

    private fun handleOnContactSupportClientNotFound(action: SettingsAction.OnContactSupportClientNotFound) {
        viewModelScope.launch(dispatcherManager.default) {
            analyticsClient.reportContactSupportClientNotFound()
        }
    }

    companion object {
        private const val SELECT_THEME_DIALOG_ID = 1

        private const val SUPPORT_EMAIL = "sunflowerapplications.help@gmail.com"
    }
}
