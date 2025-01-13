package com.bluetoothchat.feature.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluetoothchat.core.ui.ChatAppToolbar
import com.bluetoothchat.core.ui.ToolbarBackIconButton
import com.bluetoothchat.core.ui.ToolbarIconButton
import com.bluetoothchat.core.ui.ToolbarIconHorizontalPadding
import com.bluetoothchat.core.ui.ToolbarTitleText
import com.bluetoothchat.core.ui.components.ContentCrossFade
import com.bluetoothchat.core.ui.components.CustomTextField
import com.bluetoothchat.core.ui.components.CustomTextFieldValueHeight
import com.bluetoothchat.core.ui.components.ScreenContainer
import com.bluetoothchat.core.ui.components.UserImage
import com.bluetoothchat.core.ui.components.profile.ProfileContentHorizontalPadding
import com.bluetoothchat.core.ui.components.profile.ProfileFieldNameText
import com.bluetoothchat.core.ui.components.profile.ProfileFieldValueText
import com.bluetoothchat.core.ui.components.profile.ProfileImageActionIcon
import com.bluetoothchat.core.ui.components.profile.ProfileImageBottomPadding
import com.bluetoothchat.core.ui.components.profile.ProfileImageSize
import com.bluetoothchat.core.ui.components.profile.ProfileImageTopPadding
import com.bluetoothchat.core.ui.components.profile.ProfileInfoItemVerticalPadding
import com.bluetoothchat.core.ui.theme.Colors
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.theme.onScreenBackground75
import com.bluetoothchat.core.ui.util.createPickImageIntent
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.bluetoothchat.core.ui.util.rememberLauncherForImage
import com.bluetoothchat.feature.profile.contract.EditMode
import com.bluetoothchat.feature.profile.contract.ProfileAction
import com.bluetoothchat.feature.profile.contract.ProfileEvent
import com.bluetoothchat.feature.profile.contract.ProfileState
import com.ramcosta.composedestinations.annotation.Destination
import com.bluetoothchat.core.ui.R as CoreUiR

@Destination(navArgsDelegate = ProfileInputParams::class)
@Composable
fun ProfileScreen(navigator: ProfileNavigator) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ObserveOneTimeEvents(viewModel = viewModel, navigator = navigator)
    ScreenContent(state = viewState, actionListener = { viewModel.handleAction(it) })
}

@Composable
private fun ObserveOneTimeEvents(
    viewModel: ProfileViewModel,
    navigator: ProfileNavigator,
) {
    val mediaPickerLauncher = rememberLauncherForImage { resultUri ->
        viewModel.handleAction(ProfileAction.ExternalGalleryContentSelected(resultUri))
    }

    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is ProfileEvent.NavigateBack -> navigator.navigateBack()
            is ProfileEvent.NavigateToMain -> navigator.navigateToMain()
            is ProfileEvent.OpenExternalGalleryForImage -> mediaPickerLauncher.launch(createPickImageIntent())
            is ProfileEvent.ShowDialog -> navigator.showDialog(event.params)
        }
    }
}

@Composable
private fun ScreenContent(state: ProfileState, actionListener: (ProfileAction) -> Unit) {
    val isInitialSetUp = remember(state) {
        (state as? ProfileState.Loaded.Me)?.isInitialSetUp == true
    }

    //Don't override back logic if this is the start destination
    if (!isInitialSetUp) {
        BackHandler { actionListener(ProfileAction.BackButtonClicked) }
    }

    ScreenContainer(
        topAppBar = {
            ProfileToolbar(state = state, actionListener = actionListener)
        }
    ) {
        ContentCrossFade(
            modifier = Modifier.fillMaxSize(),
            targetState = state,
            contentKey = { it::class },
        ) { animatedState ->
            when (animatedState) {
                is ProfileState.Loading -> Unit
                is ProfileState.Loaded.Me -> LoadedMeScreenContent(
                    state = animatedState,
                    actionListener = actionListener
                )

                is ProfileState.Loaded.Other -> LoadedOtherScreenContent(
                    state = animatedState,
                    actionListener = actionListener
                )
            }
        }
    }
}

@Composable
private fun ProfileToolbar(state: ProfileState, actionListener: (ProfileAction) -> Unit) {
    ChatAppToolbar {
        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))
        ToolbarBackIconButton(navigateUp = { actionListener(ProfileAction.BackButtonClicked) })
        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))

        val toolbarText = when (state) {
            is ProfileState.Loaded.Me -> stringResource(id = CoreUiR.string.screen_title_my_profile)
            is ProfileState.Loaded.Other -> state.user.userName
            else -> null
        }
        ToolbarTitleText(
            modifier = Modifier
                .padding(end = ToolbarIconHorizontalPadding)
                .weight(1f),
            text = toolbarText ?: "",
        )

        (state as? ProfileState.Loaded.Me)?.let { loadedState ->
            ContentCrossFade(
                targetState = state.editMode,
                contentKey = { it::class },
            ) { animatedEditMode ->
                if (animatedEditMode is EditMode.Editing) {
                    ToolbarIconButton(
                        vector = Icons.Default.Done,
                        clickListener = { actionListener(ProfileAction.SaveClicked) },
                    )
                } else {
                    ToolbarIconButton(
                        vector = Icons.Default.Edit,
                        clickListener = { actionListener(ProfileAction.EditClicked) },
                    )
                }
            }
            Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))
        }
    }
}

@Composable
private fun LoadedMeScreenContent(state: ProfileState.Loaded.Me, actionListener: (ProfileAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = ProfileContentHorizontalPadding),
    ) {
        Spacer(modifier = Modifier.height(ProfileImageTopPadding))
        Box(
            modifier = Modifier
                .size(ProfileImageSize)
                .align(Alignment.CenterHorizontally),
        ) {
            ContentCrossFade(
                modifier = Modifier.fillMaxSize(),
                targetState = state.editMode,
                contentKey = { it::class },
            ) { animatedEditMode ->
                when (animatedEditMode) {
                    is EditMode.None -> {
                        UserImage(
                            modifier = Modifier
                                .fillMaxSize(),
                            user = state.user,
                            userDeviceAddress = state.user.deviceAddress,
                            displayConnectedIndicator = false,
                        )
                    }

                    is EditMode.Editing -> {
                        UserImage(
                            modifier = Modifier
                                .fillMaxSize(),
                            user = state.user.copy(
                                pictureFileState = animatedEditMode.updatedPictureFileState,
                                userName = animatedEditMode.updatedName,
                                color = animatedEditMode.updatedColor,
                            ),
                            userDeviceAddress = state.user.deviceAddress,
                            displayConnectedIndicator = false,
                            clickListener = { actionListener(ProfileAction.ChangePhotoClicked) },
                        )

                        if (animatedEditMode.updatedPictureFileState == null) {
                            ProfileImageActionIcon(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                painter = painterResource(id = CoreUiR.drawable.ic_image),
                                clickListener = { actionListener(ProfileAction.ChangePhotoClicked) },
                            )
                        } else {
                            ProfileImageActionIcon(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                painter = rememberVectorPainter(image = Icons.Default.Delete),
                                clickListener = { actionListener(ProfileAction.DeletePhotoClicked) },
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(ProfileImageBottomPadding))
        when (state.editMode) {
            is EditMode.None -> {
                ProfileFieldNameText(text = stringResource(id = CoreUiR.string.profile_name))
                ProfileFieldValueText(
                    modifier = Modifier.height(CustomTextFieldValueHeight),
                    text = state.user.userName ?: "",
                )

                Spacer(modifier = Modifier.height(ProfileInfoItemVerticalPadding))
                ProfileFieldNameText(text = stringResource(id = CoreUiR.string.profile_device))
                ProfileFieldValueText(
                    modifier = Modifier.height(CustomTextFieldValueHeight),
                    text = state.user.deviceName ?: "",
                )
            }

            is EditMode.Editing -> {
                ProfileFieldNameText(text = stringResource(id = CoreUiR.string.profile_name))
                CustomTextField(
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.editMode.displayEmptyUpdatedNameError,
                    hint = stringResource(CoreUiR.string.profile_name_hint),
                    value = state.user.userName ?: "",
                    onValueChange = { actionListener(ProfileAction.OnUsernameChanged(it)) },
                )

                Spacer(modifier = Modifier.height(ProfileInfoItemVerticalPadding))
                ProfileFieldNameText(text = stringResource(id = CoreUiR.string.profile_color))
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                ) {
                    val colors = remember { Colors.UserColors.map { it.toArgb() } }
                    val radius = 4.dp
                    val strokeWidth = 2.dp
                    colors.forEach { color ->
                        val isSelected = state.editMode.updatedColor == color
                        Box(
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(radius))
                                .clickable { actionListener(ProfileAction.OnColorChanged(color)) }
                                .padding(4.dp)
                                .border(
                                    width = strokeWidth,
                                    shape = RoundedCornerShape(radius),
                                    color = if (isSelected) LocalChatAppColorScheme.current.onScreenBackground75 else Color.Transparent,
                                )
                                .padding(strokeWidth)
                                .size(24.dp)
                                .background(color = Color(color), shape = RoundedCornerShape(radius - strokeWidth))
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun LoadedOtherScreenContent(state: ProfileState.Loaded.Other, actionListener: (ProfileAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = ProfileContentHorizontalPadding),
    ) {
        Spacer(modifier = Modifier.height(ProfileImageTopPadding))
        UserImage(
            modifier = Modifier
                .size(ProfileImageSize)
                .align(Alignment.CenterHorizontally),
            user = state.user,
            userDeviceAddress = state.user.deviceAddress,
            displayConnectedIndicator = false,
        )

        Spacer(modifier = Modifier.height(ProfileImageBottomPadding))
        ProfileFieldNameText(text = stringResource(id = CoreUiR.string.profile_name))
        ProfileFieldValueText(
            modifier = Modifier.height(CustomTextFieldValueHeight),
            text = state.user.userName ?: "",
        )

        Spacer(modifier = Modifier.height(ProfileInfoItemVerticalPadding))
        ProfileFieldNameText(text = stringResource(id = CoreUiR.string.profile_device))
        ProfileFieldValueText(
            modifier = Modifier.height(CustomTextFieldValueHeight),
            text = state.user.deviceName ?: "",
        )
    }
}
