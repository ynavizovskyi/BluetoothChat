package com.bluetoothchat.feature.connect.group.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluetoothchat.core.ui.ChatAppToolbar
import com.bluetoothchat.core.ui.ToolbarBackIconButton
import com.bluetoothchat.core.ui.ToolbarIconButton
import com.bluetoothchat.core.ui.ToolbarIconHorizontalPadding
import com.bluetoothchat.core.ui.ToolbarTitleText
import com.bluetoothchat.core.ui.components.ChatImage
import com.bluetoothchat.core.ui.components.CustomTextField
import com.bluetoothchat.core.ui.components.ScreenContainer
import com.bluetoothchat.core.ui.components.profile.ProfileContentHorizontalPadding
import com.bluetoothchat.core.ui.components.profile.ProfileFieldNameText
import com.bluetoothchat.core.ui.components.profile.ProfileImageActionIcon
import com.bluetoothchat.core.ui.components.profile.ProfileImageBottomPadding
import com.bluetoothchat.core.ui.components.profile.ProfileImageSize
import com.bluetoothchat.core.ui.components.profile.ProfileImageTopPadding
import com.bluetoothchat.core.ui.util.createPickImageIntent
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.bluetoothchat.core.ui.util.rememberLauncherForImage
import com.bluetoothchat.feature.connect.group.create.contract.CreateGroupAction
import com.bluetoothchat.feature.connect.group.create.contract.CreateGroupEvent
import com.bluetoothchat.feature.connect.group.create.contract.CreateGroupState
import com.ramcosta.composedestinations.annotation.Destination
import com.bluetoothchat.core.ui.R as CoreUiR

@Destination
@Composable
fun CreateGroupScreen(navigator: CreateGroupNavigator) {
    val viewModel: CreateGroupViewModel = hiltViewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ObserveOneTimeEvents(viewModel = viewModel, navigator = navigator)
    ScreenContent(state = viewState, actionListener = { viewModel.handleAction(it) })
}

@Composable
private fun ObserveOneTimeEvents(
    viewModel: CreateGroupViewModel,
    navigator: CreateGroupNavigator,
) {
    val mediaPickerLauncher = rememberLauncherForImage { resultUri ->
        viewModel.handleAction(CreateGroupAction.ExternalGalleryContentSelected(resultUri))
    }

    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is CreateGroupEvent.NavigateBack -> navigator.navigateBack()
            is CreateGroupEvent.OpenExternalGalleryForImage -> mediaPickerLauncher.launch(createPickImageIntent())
            is CreateGroupEvent.NavigateToGroupInfo -> navigator.navigateToGroupChatInfoScreen(groupChatId = event.groupId)
            is CreateGroupEvent.ShowErrorDialog -> navigator.showDialog(event.params)
        }
    }
}

@Composable
private fun ScreenContent(state: CreateGroupState, actionListener: (CreateGroupAction) -> Unit) {
    ScreenContainer(
        topAppBar = { CreateGroupToolbar(actionListener = actionListener) },
    ) {
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
                ChatImage(
                    modifier = Modifier
                        .fillMaxSize(),
                    imageFileState = state.groupImageChatState,
                    chatName = state.chatName,
                    clickListener = { actionListener(CreateGroupAction.ChangePhotoClicked) },
                )

                if (state.groupImageChatState == null) {
                    ProfileImageActionIcon(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        painter = painterResource(id = CoreUiR.drawable.ic_image),
                        clickListener = { actionListener(CreateGroupAction.ChangePhotoClicked) },
                    )
                } else {
                    ProfileImageActionIcon(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        painter = rememberVectorPainter(image = Icons.Default.Delete),
                        clickListener = { actionListener(CreateGroupAction.DeletePhotoClicked) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(ProfileImageBottomPadding))
            ProfileFieldNameText(text = stringResource(id = CoreUiR.string.group_name))
            CustomTextField(
                modifier = Modifier.fillMaxWidth(),
                isError = state.displayNameError,
                hint = stringResource(CoreUiR.string.group_name_hint),
                value = state.chatName ?: "",
                onValueChange = { actionListener(CreateGroupAction.OnGroupNameChanged(it)) },
            )
        }
    }
}

@Composable
private fun CreateGroupToolbar(actionListener: (CreateGroupAction) -> Unit) {
    ChatAppToolbar {
        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))
        ToolbarBackIconButton(navigateUp = { actionListener(CreateGroupAction.BackButtonClicked) })
        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))


        ToolbarTitleText(
            modifier = Modifier.weight(1f),
            text = stringResource(id = CoreUiR.string.screen_title_new_group),
        )

        ToolbarIconButton(
            vector = Icons.Default.Done,
            clickListener = { actionListener(CreateGroupAction.SaveButtonClicked) },
        )
        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))
    }
}
