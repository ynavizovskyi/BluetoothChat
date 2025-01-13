package com.bluetoothchat.feature.connect.group.addusers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluetoothchat.core.permission.rememberBluetoothPermissionsState
import com.bluetoothchat.core.ui.R
import com.bluetoothchat.core.ui.SimpleChatAppToolbar
import com.bluetoothchat.core.ui.components.ContentCrossFade
import com.bluetoothchat.core.ui.components.ScreenContainer
import com.bluetoothchat.core.ui.components.button.NoBgButton
import com.bluetoothchat.core.ui.components.button.defaultCustomButtonPaddings
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.theme.ScreenContentHorizontalPadding
import com.bluetoothchat.core.ui.theme.ScreenContentVerticalPadding
import com.bluetoothchat.core.ui.util.createEnableBluetoothIntent
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.bluetoothchat.core.ui.util.rememberLauncherEnableBluetooth
import com.bluetoothchat.feature.connect.common.BluetoothDisabled
import com.bluetoothchat.feature.connect.common.BtDevice
import com.bluetoothchat.feature.connect.common.ConnectingProgressOverlay
import com.bluetoothchat.feature.connect.common.Divider
import com.bluetoothchat.feature.connect.common.EmptyListText
import com.bluetoothchat.feature.connect.common.NoBluetoothPermission
import com.bluetoothchat.feature.connect.common.SectionTitleText
import com.bluetoothchat.feature.connect.group.addusers.contract.AddUserEvent
import com.bluetoothchat.feature.connect.group.addusers.contract.AddUserState
import com.bluetoothchat.feature.connect.group.addusers.contract.AddUsersAction
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ramcosta.composedestinations.annotation.Destination

@Destination(navArgsDelegate = AddUsersInputParams::class)
@Composable
fun AddUsersScreen(navigator: AddUsersNavigator) {
    val viewModel: AddUserViewModel = hiltViewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ObserveOneTimeEvents(viewModel = viewModel, navigator = navigator)
    AddUsersScreen(state = viewState, actionListener = { viewModel.handleAction(it) })
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ObserveOneTimeEvents(
    viewModel: AddUserViewModel,
    navigator: AddUsersNavigator,
) {
    val bluetoothPermissionsState = rememberBluetoothPermissionsState(
        onStatusChanged = { permissionStatus ->
            viewModel.handleAction(AddUsersAction.OnBluetoothPermissionResult(status = permissionStatus))
        },
    )

    val enableBluetoothLauncher = rememberLauncherEnableBluetooth { enabled ->
        viewModel.handleAction(AddUsersAction.OnEnableBluetoothResult(enabled = enabled))
    }

    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is AddUserEvent.NavigateBack -> navigator.navigateBack()
            is AddUserEvent.RequestBluetoothPermission -> bluetoothPermissionsState.launchMultiplePermissionRequest()
            is AddUserEvent.RequestEnabledBluetooth -> enableBluetoothLauncher.launch(createEnableBluetoothIntent())
            is AddUserEvent.ShowErrorDialog -> navigator.showDialog(params = event.params)
        }
    }
}

@Composable
private fun AddUsersScreen(state: AddUserState, actionListener: (AddUsersAction) -> Unit) {
    ScreenContainer(
        topAppBar = {
            SimpleChatAppToolbar(
                title = stringResource(id = R.string.screen_title_add_members),
                navigationListener = { actionListener(AddUsersAction.BackButtonClicked) },
            )
        }
    ) {
        ContentCrossFade(
            modifier = Modifier.fillMaxSize(),
            targetState = state,
            contentKey = { it::class },
        ) { animatedState ->
            when (animatedState) {
                is AddUserState.None -> {}
                is AddUserState.NoBluetoothPermission -> NoBluetoothPermission(actionListener = {
                    actionListener(AddUsersAction.GrantBluetoothPermissionsClicked)
                })

                is AddUserState.BluetoothDisabled -> BluetoothDisabled(actionListener = {
                    actionListener(AddUsersAction.EnableBluetoothClicked)
                })

                is AddUserState.Discovering -> Discovering(state = animatedState, actionListener = actionListener)
            }
        }
    }
}

@Composable
private fun Discovering(state: AddUserState.Discovering, actionListener: (AddUsersAction) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.height(ScreenContentVerticalPadding))
            Row(
                modifier = Modifier
                    .padding(horizontal = ScreenContentHorizontalPadding)
                    .fillMaxWidth(),
            ) {
                NoBgButton(
                    modifier = Modifier.weight(1f),
                    enabled = !state.isScanning,
                    text = stringResource(id = if (state.isScanning) R.string.connect_scanning else R.string.connect_scan_for_devices),
                    contentPadding = defaultCustomButtonPaddings(horizontal = 8.dp),
                    onClick = { actionListener(AddUsersAction.ScanForDevicesClicked) },
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(ScreenContentVerticalPadding))

            Divider(modifier = Modifier.padding(horizontal = ScreenContentHorizontalPadding))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(ScreenContentVerticalPadding))
                SectionTitleText(text = stringResource(id = R.string.connect_paired_devices))
                if (state.pairedDevices.isNotEmpty()) {
                    state.pairedDevices.forEach { device ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BtDevice(
                                modifier = Modifier.fillMaxWidth(),
                                device = device.deviceWithUser,
                                clickListener = { actionListener(AddUsersAction.DeviceClicked(device = device.deviceWithUser)) },
                                rightContent = {
                                    if (device.isMember) {
                                        Spacer(
                                            modifier = Modifier
                                                .widthIn(min = 16.dp)
                                                .weight(1f)
                                        )
                                        Text(
                                            text = stringResource(id = R.string.group_member),
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                fontFamily = FontFamily.SansSerif,
                                                color = LocalChatAppColorScheme.current.accent,
                                                fontWeight = FontWeight.Normal,
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                } else {
                    EmptyListText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.connect_devices_empty),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                SectionTitleText(text = stringResource(id = R.string.connect_found_devices))
                if (state.foundDevices.isNotEmpty()) {
                    state.foundDevices.forEach { device ->
                        BtDevice(
                            modifier = Modifier.fillMaxWidth(),
                            device = device.deviceWithUser,
                            clickListener = { actionListener(AddUsersAction.DeviceClicked(device = device.deviceWithUser)) },
                        )
                    }
                } else {
                    EmptyListText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.connect_devices_empty),
                    )
                }
            }
        }

        if (state.displayProgressOverlay) {
            ConnectingProgressOverlay()
        }
    }
}
