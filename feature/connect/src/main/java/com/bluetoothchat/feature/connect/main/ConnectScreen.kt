package com.bluetoothchat.feature.connect.main

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluetoothchat.core.permission.rememberBluetoothPermissionsState
import com.bluetoothchat.core.ui.SimpleChatAppToolbar
import com.bluetoothchat.core.ui.components.ContentCrossFade
import com.bluetoothchat.core.ui.components.ObserveViewResumedState
import com.bluetoothchat.core.ui.components.ScreenContainer
import com.bluetoothchat.core.ui.components.button.NoBgButton
import com.bluetoothchat.core.ui.components.button.defaultCustomButtonPaddings
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.theme.ScreenContentHorizontalPadding
import com.bluetoothchat.core.ui.theme.ScreenContentVerticalPadding
import com.bluetoothchat.core.ui.theme.onBackgroundInfoText
import com.bluetoothchat.core.ui.util.createEnableBluetoothIntent
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.bluetoothchat.core.ui.util.rememberLauncherEnableBluetooth
import com.bluetoothchat.feature.connect.R
import com.bluetoothchat.feature.connect.common.BluetoothDisabled
import com.bluetoothchat.feature.connect.common.BtDevice
import com.bluetoothchat.feature.connect.common.ConnectingProgressOverlay
import com.bluetoothchat.feature.connect.common.Divider
import com.bluetoothchat.feature.connect.common.EmptyListText
import com.bluetoothchat.feature.connect.common.NoBluetoothPermission
import com.bluetoothchat.feature.connect.common.SectionTitleText
import com.bluetoothchat.feature.connect.main.contract.ConnectAction
import com.bluetoothchat.feature.connect.main.contract.ConnectEvent
import com.bluetoothchat.feature.connect.main.contract.ConnectState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ramcosta.composedestinations.annotation.Destination
import com.bluetoothchat.core.ui.R as CoreUiR

@Destination(navArgsDelegate = ConnectInputParams::class)
@Composable
fun ConnectScreen(navigator: ConnectNavigator) {
    val viewModel: ConnectViewModel = hiltViewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ObserveOneTimeEvents(viewModel = viewModel, navigator = navigator)
    ConnectScreen(state = viewState, actionListener = { viewModel.handleAction(it) })
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ObserveOneTimeEvents(
    viewModel: ConnectViewModel,
    navigator: ConnectNavigator,
) {
    val activity = LocalContext.current as Activity

    val bluetoothPermissionsState = rememberBluetoothPermissionsState(
        onStatusChanged = { permissionStatus ->
            viewModel.handleAction(ConnectAction.OnBluetoothPermissionResult(status = permissionStatus))
        },
    )

    val enableBluetoothLauncher = rememberLauncherEnableBluetooth { enabled ->
        viewModel.handleAction(ConnectAction.OnEnableBluetoothResult(enabled = enabled))
    }

    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is ConnectEvent.NavigateBack -> navigator.navigateBack()
            is ConnectEvent.NavigateToCreateGroupScreen -> navigator.navigateToCreateGroupScreen()
            is ConnectEvent.RequestBluetoothPermission -> bluetoothPermissionsState.launchMultiplePermissionRequest()
            is ConnectEvent.MakeDeviceDiscoverable -> makeDiscoverable(activity)
            is ConnectEvent.RequestEnabledBluetooth -> enableBluetoothLauncher.launch(createEnableBluetoothIntent())
            is ConnectEvent.ShowErrorDialog -> navigator.showDialog(params = event.params)
            is ConnectEvent.NavigateToPrivateChatScreen -> navigator.navigateToPrivateChatScreen(chatId = event.chatId)
            is ConnectEvent.NavigateToGroupChatScreen -> navigator.navigateToGroupChatScreen(chatId = event.chatId)
            is ConnectEvent.ShareApk -> shareApk(activity = activity, uri = event.uri)
        }
    }

    ObserveViewResumedState { viewModel.handleAction(ConnectAction.OnResumedStateChanged(it)) }
}

@Composable
private fun ConnectScreen(state: ConnectState, actionListener: (ConnectAction) -> Unit) {
    ScreenContainer(
        topAppBar = {
            SimpleChatAppToolbar(
                title = stringResource(id = CoreUiR.string.screen_title_connect),
                navigationListener = { actionListener(ConnectAction.BackButtonClicked) },
            )
        }
    ) {
        ContentCrossFade(
            modifier = Modifier.fillMaxSize(),
            targetState = state,
            contentKey = { it::class },
        ) { animatedState ->
            when (animatedState) {
                is ConnectState.None -> {}
                is ConnectState.NoBluetoothPermission -> NoBluetoothPermission(actionListener = {
                    actionListener(ConnectAction.GrantBluetoothPermissionsClicked)
                })

                is ConnectState.BluetoothDisabled -> BluetoothDisabled(actionListener = {
                    actionListener(ConnectAction.EnableBluetoothClicked)
                })

                is ConnectState.Discovering -> Discovering(state = animatedState, actionListener = actionListener)
            }
        }
    }
}

@Composable
private fun Discovering(state: ConnectState.Discovering, actionListener: (ConnectAction) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            CreateGroupButton(
                modifier = Modifier.fillMaxWidth(),
                clickListener = { actionListener(ConnectAction.CreateGroupClicked) },
            )

            Divider(modifier = Modifier.padding(horizontal = ScreenContentHorizontalPadding))

            Spacer(modifier = Modifier.height(ScreenContentVerticalPadding))
            Text(
                modifier = Modifier.padding(horizontal = ScreenContentHorizontalPadding),
                text = stringResource(id = CoreUiR.string.connect_info_header),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = LocalChatAppColorScheme.current.onBackgroundInfoText,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
            )

            Spacer(modifier = Modifier.height(ScreenContentVerticalPadding))
            Row(
                modifier = Modifier
                    .padding(horizontal = ScreenContentHorizontalPadding)
                    .fillMaxWidth(),
            ) {
                NoBgButton(
                    modifier = Modifier.weight(1f),
                    enabled = !state.isScanning,
                    text = stringResource(id = if (state.isScanning) CoreUiR.string.connect_scanning else CoreUiR.string.connect_scan_for_devices),
                    contentPadding = defaultCustomButtonPaddings(horizontal = 8.dp),
                    onClick = { actionListener(ConnectAction.ScanForDevicesClicked) },
                )
                NoBgButton(
                    modifier = Modifier.weight(1f),
                    enabled = !state.isDeviceDiscoverable,
                    text = stringResource(id = if (state.isDeviceDiscoverable) CoreUiR.string.connect_discoverable else CoreUiR.string.connect_make_discoverable),
                    contentPadding = defaultCustomButtonPaddings(horizontal = 8.dp),
                    onClick = { actionListener(ConnectAction.MakeDiscoverableClicked) },
                )
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
                SectionTitleText(text = stringResource(id = CoreUiR.string.connect_paired_devices))
                if (state.pairedDevices.isNotEmpty()) {
                    state.pairedDevices.forEach { device ->
                        BtDevice(
                            modifier = Modifier.fillMaxWidth(),
                            device = device,
                            clickListener = { actionListener(ConnectAction.DeviceClicked(device = device.device)) },
                        )
                    }
                } else {
                    EmptyListText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = CoreUiR.string.connect_devices_empty),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                SectionTitleText(text = stringResource(id = CoreUiR.string.connect_found_devices))
                if (state.foundDevices.isNotEmpty()) {
                    state.foundDevices.forEach { device ->
                        BtDevice(
                            modifier = Modifier.fillMaxWidth(),
                            device = device,
                            clickListener = { actionListener(ConnectAction.DeviceClicked(device = device.device)) },
                        )
                    }
                } else {
                    EmptyListText(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = CoreUiR.string.connect_devices_empty),
                    )
                }
            }
        }

        if (state.displayProgressOverlay) {
            ConnectingProgressOverlay()
        }
    }
}

@Composable
private fun CreateGroupButton(clickListener: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clickable { clickListener() }
            .padding(horizontal = ScreenContentHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(24.dp),
            painter = painterResource(id = R.drawable.ic_group),
            contentDescription = "Group",
            tint = LocalChatAppColorScheme.current.onScreenBackground,
        )

        Spacer(modifier = Modifier.width(ScreenContentHorizontalPadding))
        Text(
            text = stringResource(id = CoreUiR.string.connect_create_group),
            style = TextStyle(
                fontSize = 17.sp,
                fontFamily = FontFamily.SansSerif,
                color = LocalChatAppColorScheme.current.onScreenBackground,
                fontWeight = FontWeight.Medium,
            )
        )

    }
}

fun makeDiscoverable(activity: Activity) {
    val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60)
    try {
        ActivityCompat.startActivityForResult(activity, discoverableIntent, 1, null)
    } catch (e: ActivityNotFoundException) {

    }
}

fun shareApk(activity: Activity, uri: Uri) {
    val sharingIntent = Intent(Intent.ACTION_SEND).apply {
        type = "*/*"
        `package` = "com.android.bluetooth"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_STREAM, uri)
    }

    try {
        activity.startActivity(
            Intent.createChooser(
                sharingIntent,
                getString(activity, CoreUiR.string.connect_share_apk)
            )
        )
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
//        Toast.makeText(this, getString(R.string.scan__unable_to_share_apk), Toast.LENGTH_LONG).show()
    }
}
