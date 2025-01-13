package com.bluetoothchat.feature.settings.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluetoothchat.core.ui.SimpleChatAppToolbar
import com.bluetoothchat.core.ui.components.ScreenContainer
import com.bluetoothchat.core.ui.components.button.CustomButton
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.theme.settingsSectionDivider
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.bluetoothchat.core.ui.util.safeOpenUri
import com.bluetoothchat.feature.settings.SettingsNavigator
import com.bluetoothchat.feature.settings.nameResId
import com.bluetoothchat.feature.settings.ui.contract.AboutSection
import com.bluetoothchat.feature.settings.ui.contract.AppearanceSection
import com.bluetoothchat.feature.settings.ui.contract.DebugSection
import com.bluetoothchat.feature.settings.ui.contract.SettingsAction
import com.bluetoothchat.feature.settings.ui.contract.SettingsEvent
import com.bluetoothchat.feature.settings.ui.contract.SettingsState
import com.ramcosta.composedestinations.annotation.Destination
import com.bluetoothchat.core.ui.R as CoreUiR

private val SectionTitleHorizontalPadding = 16.dp
private val SectionContentHorizontalPadding = 24.dp

@Destination(start = true)
@Composable
fun SettingsScreen(navigator: SettingsNavigator) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ObserveOneTimeEvents(viewModel = viewModel, navigator = navigator)
    ScreenContent(state = viewState, actionListener = { viewModel.handleAction(it) })
}

@Composable
private fun ObserveOneTimeEvents(
    viewModel: SettingsViewModel,
    navigator: SettingsNavigator,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is SettingsEvent.NavigateBack -> navigator.navigateBack()
            is SettingsEvent.ShowDialog -> navigator.showDialog(event.params)
            is SettingsEvent.OpenUrl -> uriHandler.safeOpenUri(event.url)
            is SettingsEvent.OpenEmailClient -> {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:") // only email apps should handle this
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(event.emailAddress))
                    putExtra(Intent.EXTRA_SUBJECT, event.emailSubject)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    viewModel.handleAction(SettingsAction.OnContactSupportClientResolved)
                    context.startActivity(intent)
                } else {
                    viewModel.handleAction(SettingsAction.OnContactSupportClientNotFound)
                }
            }
        }
    }

    navigator.OnDialogResult {
        viewModel.handleAction(SettingsAction.OnDialogResult(it))
    }
}

@Composable
private fun ScreenContent(state: SettingsState, actionListener: (SettingsAction) -> Unit) {
    ScreenContainer(
        topAppBar = {
            SimpleChatAppToolbar(
                title = stringResource(id = CoreUiR.string.screen_title_settings),
                navigationListener = { actionListener(SettingsAction.BackClicked) }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                state.appearanceSection?.let {
                    AppearanceSection(section = it, actionListener = actionListener)
                }

                state.aboutSection?.let {
                    AboutSection(section = it, actionListener = actionListener)
                }

                state.debugSection?.let {
                    DebugSection(section = it, actionListener = actionListener)
                }
            }
        }
    }
}

@Composable
private fun AppearanceSection(
    section: AppearanceSection,
    actionListener: (SettingsAction) -> Unit,
) {
    Section(
        name = stringResource(id = CoreUiR.string.settings_section_appearance)
    ) {
        SelectableValueSectionItem(
            name = stringResource(id = CoreUiR.string.settings_theme),
            value = stringResource(id = section.theme.nameResId()),
            clickListener = { actionListener(SettingsAction.ThemeClicked(section.theme)) },
        )
    }
}

@Composable
private fun AboutSection(
    section: AboutSection,
    actionListener: (SettingsAction) -> Unit,
) {
    Section(
        name = stringResource(id = CoreUiR.string.settings_section_about)
    ) {
        SimpleLinkSectionItem(
            itemName = stringResource(id = CoreUiR.string.privacy_policy),
            onClickListener = { actionListener.invoke(SettingsAction.PrivacyPolicyClicked) }
        )
        SimpleLinkSectionItem(
            itemName = stringResource(id = CoreUiR.string.terms_of_use),
            onClickListener = { actionListener.invoke(SettingsAction.TermsOfUseClicked) }
        )
        SimpleLinkSectionItem(
            itemName = stringResource(id = CoreUiR.string.contact_support),
            onClickListener = { actionListener.invoke(SettingsAction.ContactSupportClicked) }
        )
        SimpleInfoSectionItem(
            itemName = stringResource(id = CoreUiR.string.app_version),
            itemValue = section.appVersion,
        )
        SimpleInfoSectionItem(
            itemName = stringResource(id = CoreUiR.string.chat_protocol_version),
            itemValue = section.protocolVersion,
        )
    }
}

@Composable
private fun DebugSection(
    section: DebugSection,
    actionListener: (SettingsAction) -> Unit,
) {
    Section(
        header = {
            SectionHeader(color = Color.Red, name = "Debug")
        }
    ) {

    }
}


@Composable
private fun Section(name: String, content: @Composable ColumnScope.() -> Unit) {
    Section(header = { SectionHeader(name = name) }, content = content)
}

@Composable
private fun Section(header: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        header()

        Spacer(modifier = Modifier.height(8.dp))
        content()

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SectionHeader(
    modifier: Modifier = Modifier,
    color: Color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.5f),
    name: String,
) {
    Text(
        modifier = modifier.padding(horizontal = SectionTitleHorizontalPadding),
        color = color,
        text = name,
        style = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif,
            color = color,
        ),
    )
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = SectionTitleHorizontalPadding)
            .fillMaxWidth()
            .height(1.dp)
            .background(color = LocalChatAppColorScheme.current.settingsSectionDivider),
    )
}

@Composable
private fun SelectableValueSectionItem(
    name: String,
    value: String,
    clickListener: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable { clickListener() }
            .padding(horizontal = SectionContentHorizontalPadding, vertical = 8.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = name,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = LocalChatAppColorScheme.current.onScreenBackground,
            ),
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.5f),
            ),
        )
    }
}

@Composable
private fun SimpleLinkSectionItem(itemName: String, onClickListener: () -> Unit) {
    SimpleItem(modifier = Modifier.clickable {
        onClickListener.invoke()
    }) {
        SectionItemText(
            modifier = Modifier.align(alignment = Alignment.CenterStart),
            text = itemName,
        )
    }
}

@Composable
private fun SimpleInfoSectionItem(itemName: String, itemValue: String) {
    SimpleItem {
        SectionItemText(
            modifier = Modifier.align(alignment = Alignment.CenterStart),
            text = itemName,
        )
        SectionItemText(
            modifier = Modifier.align(alignment = Alignment.CenterEnd),
            text = itemValue,
            colorAlpha = 0.5f,
        )
    }
}

@Composable
private fun SimpleButtonSectionItem(
    itemName: String,
    buttonValue: String,
    buttonClickListener: () -> Unit,
    enabled: Boolean = true
) {
    SimpleItem {
        SectionItemText(
            modifier = Modifier.align(alignment = Alignment.CenterStart),
            text = itemName,
        )
        CustomButton(
            modifier = Modifier
                .align(alignment = Alignment.CenterEnd)
                .height(32.dp),
            enabled = enabled,
            onClick = buttonClickListener
        ) {
            Text(text = buttonValue)
        }
    }
}

@Composable
private fun SimpleItem(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .padding(horizontal = SectionContentHorizontalPadding)
            .fillMaxWidth()
            .height(40.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
    }
}

@Composable
private fun SectionItemText(modifier: Modifier, text: String, colorAlpha: Float = 1f) {
    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = colorAlpha),
        ),
    )
}
