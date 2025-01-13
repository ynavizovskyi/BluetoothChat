package com.bluetoothchat.feature.chat.image

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.permission.isGranted
import com.bluetoothchat.core.permission.rememberWriteStoragePermissionState
import com.bluetoothchat.core.ui.SimpleChatAppToolbar
import com.bluetoothchat.core.ui.ToolbarHeight
import com.bluetoothchat.core.ui.components.ChatAppActionSnackbar
import com.bluetoothchat.core.ui.components.ContentCrossFade
import com.bluetoothchat.core.ui.theme.ChatAppTheme
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.bluetoothchat.core.ui.util.showImageSavedSnackbar
import com.bluetoothchat.feature.chat.image.contract.ViewImageAction
import com.bluetoothchat.feature.chat.image.contract.ViewImageEvent
import com.bluetoothchat.feature.chat.image.contract.ViewImageState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.ramcosta.composedestinations.annotation.Destination
import java.io.File
import com.bluetoothchat.core.ui.R as CoreUiR

private val SaveActionSize = 56.dp
private val SaveActionPadding = 16.dp

@Destination(navArgsDelegate = ViewImageInputParams::class)
@Composable
fun ViewImageScreen(navigator: ViewImageNavigator) {
    val viewModel: ViewImageViewModel = hiltViewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveOneTimeEvents(
        viewModel = viewModel,
        navigator = navigator,
        snackbarHostState = snackbarHostState,
    )
    ScreenContent(
        state = viewState,
        actionListener = { viewModel.handleAction(it) },
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ObserveOneTimeEvents(
    viewModel: ViewImageViewModel,
    navigator: ViewImageNavigator,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val writeStoragePermissionState = rememberWriteStoragePermissionState(
        onStatusChanged = { permissionStatus ->
            viewModel.handleAction(ViewImageAction.OnWriteStoragePermissionResult(granted = permissionStatus.isGranted()))
        }
    )

    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is ViewImageEvent.NavigateBack -> navigator.navigateBack()
            is ViewImageEvent.ShowImageSavedSnackbar -> {
                snackbarHostState.showImageSavedSnackbar(
                    coroutineScope = coroutineScope,
                    context = context,
                )
            }

            is ViewImageEvent.RequestWriteStoragePermission -> writeStoragePermissionState?.launchPermissionRequest()
        }
    }
}

@Composable
private fun ScreenContent(
    state: ViewImageState,
    actionListener: (ViewImageAction) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    //Forcing dark theme on this screen
    ChatAppTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            val activity = LocalContext.current as Activity
            val statusBarHeight = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
            val navigationBarHeight = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

            Box(
                modifier = Modifier
                    .background(color = LocalChatAppColorScheme.current.screenBackground)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
                    .fillMaxSize(),
            ) {
                ContentCrossFade(
                    modifier = Modifier
                        .fillMaxSize(),
                    targetState = state,
                    contentKey = { it::class },
                ) { animatedState ->
                    when (animatedState) {
                        is ViewImageState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize())
                        }

                        is ViewImageState.Loaded -> {
                            LoadedScreenContent(
                                state = animatedState,
                                actionListener = actionListener,
                            )
                        }
                    }
                }
            }

            val systemBarsColor = LocalChatAppColorScheme.current.toolbar
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(statusBarHeight + ToolbarHeight * 1.5f)
                    .background(
                        Brush.verticalGradient(
                            0f to systemBarsColor.copy(alpha = 0.5f),
                            0.5f to systemBarsColor.copy(alpha = 0.33f),
                            0.75f to systemBarsColor.copy(alpha = 0.22f),
                            0.87f to systemBarsColor.copy(alpha = 0.15f),
                            0.93f to systemBarsColor.copy(alpha = 0.10f),
                            0.97f to systemBarsColor.copy(alpha = 0.07f),
                            0.99f to systemBarsColor.copy(alpha = 0.04f),
                            1f to systemBarsColor.copy(alpha = 0f),
                        )
                    )
            )
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(navigationBarHeight + SaveActionPadding + SaveActionSize * 1.5f)
                    .background(
                        Brush.verticalGradient(
                            0f to systemBarsColor.copy(alpha = 0f),
                            0.01f to systemBarsColor.copy(alpha = 0.04f),
                            0.03f to systemBarsColor.copy(alpha = 0.07f),
                            0.07f to systemBarsColor.copy(alpha = 0.1f),
                            0.13f to systemBarsColor.copy(alpha = 0.15f),
                            0.25f to systemBarsColor.copy(alpha = 0.22f),
                            0.5f to systemBarsColor.copy(alpha = 0.33f),
                            1f to systemBarsColor.copy(alpha = 0.5f),
                        )
                    )
            )

            SimpleChatAppToolbar(
                modifier = Modifier.statusBarsPadding(),
                title = "",
                backgroundColor = Color.Transparent,
                navigationListener = { actionListener(ViewImageAction.BackButtonClicked) }
            )

            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = SaveActionPadding)
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .size(SaveActionSize)
                    .clickable { actionListener(ViewImageAction.SaveClicked(activity = activity)) },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                    painter = painterResource(id = CoreUiR.drawable.ic_save),
                    colorFilter = ColorFilter.tint(color = LocalChatAppColorScheme.current.onScreenBackground),
                    contentDescription = "Save",
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
            ) {
                ChatAppActionSnackbar(it)
            }
        }
    }
}

@Composable
private fun LoadedScreenContent(state: ViewImageState.Loaded, actionListener: (ViewImageAction) -> Unit) {
    (state.image.file as? FileState.Downloaded)?.let { file ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxAspectRatio = remember(maxWidth, maxHeight) { maxWidth / maxHeight }
            val sizeModifier = remember(state.image.aspectRatio, maxAspectRatio) {
                val matchWidthFirst = state.image.aspectRatio > maxAspectRatio
                if (matchWidthFirst) {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(state.image.aspectRatio)
                } else {
                    Modifier
                        .fillMaxHeight()
                        .aspectRatio(state.image.aspectRatio)
                }
            }

            val coilFile = remember(file.path) { File(file.path) }
            val imageRequest = ImageRequest.Builder(LocalContext.current)
                .data(coilFile)
                .crossfade(true)
                .build()

            AsyncImage(
                modifier = sizeModifier
                    .align(Alignment.Center),
                model = imageRequest,
                contentScale = ContentScale.FillWidth,
                contentDescription = "Chat image",
            )
        }
    }
}
