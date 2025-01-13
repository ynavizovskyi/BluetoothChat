package com.bluetoothchat.feature.chat.group.chatinfo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bluetoothchat.core.ui.ChatAppToolbar
import com.bluetoothchat.core.ui.R
import com.bluetoothchat.core.ui.ToolbarBackIconButton
import com.bluetoothchat.core.ui.ToolbarIconButton
import com.bluetoothchat.core.ui.ToolbarIconHorizontalPadding
import com.bluetoothchat.core.ui.ToolbarTitleText
import com.bluetoothchat.core.ui.components.ChatImage
import com.bluetoothchat.core.ui.components.ContentCrossFade
import com.bluetoothchat.core.ui.components.CustomTextField
import com.bluetoothchat.core.ui.components.CustomTextFieldValueHeight
import com.bluetoothchat.core.ui.components.ScreenContainer
import com.bluetoothchat.core.ui.components.UserImage
import com.bluetoothchat.core.ui.components.dropdown.DropDownDisplayStrategy
import com.bluetoothchat.core.ui.components.dropdown.DropdownMenuItemModel
import com.bluetoothchat.core.ui.components.dropdown.ItemDropdownMenuContainer
import com.bluetoothchat.core.ui.components.profile.ProfileContentHorizontalPadding
import com.bluetoothchat.core.ui.components.profile.ProfileFieldNameText
import com.bluetoothchat.core.ui.components.profile.ProfileFieldValueText
import com.bluetoothchat.core.ui.components.profile.ProfileImageActionIcon
import com.bluetoothchat.core.ui.components.profile.ProfileImageBottomPadding
import com.bluetoothchat.core.ui.components.profile.ProfileImageSize
import com.bluetoothchat.core.ui.components.profile.ProfileImageTopPadding
import com.bluetoothchat.core.ui.model.UiText
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.model.ViewUserAction
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme
import com.bluetoothchat.core.ui.theme.ScreenContentHorizontalPadding
import com.bluetoothchat.core.ui.theme.mainItemDivider
import com.bluetoothchat.core.ui.util.createPickImageIntent
import com.bluetoothchat.core.ui.util.observeWithLifecycle
import com.bluetoothchat.core.ui.util.rememberLauncherForImage
import com.bluetoothchat.feature.chat.group.chatinfo.contract.EditMode
import com.bluetoothchat.feature.chat.group.chatinfo.contract.GroupChatInfoAction
import com.bluetoothchat.feature.chat.group.chatinfo.contract.GroupChatInfoEvent
import com.bluetoothchat.feature.chat.group.chatinfo.contract.GroupChatInfoState
import com.ramcosta.composedestinations.annotation.Destination

val UserItemHorizontalPadding = ScreenContentHorizontalPadding
val UserItemVerticalPadding = 6.dp
val UserItemImageSize = 40.dp
val UserItemContentHorizontalPadding = 12.dp


@Destination(navArgsDelegate = GroupChatInfoInputParams::class)
@Composable
fun GroupChatInfoScreen(navigator: GroupChatInfoNavigator) {
    val viewModel: GroupChatInfoViewModel = hiltViewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ObserveOneTimeEvents(viewModel = viewModel, navigator = navigator)
    ScreenContent(state = viewState, actionListener = { viewModel.handleAction(it) })
}

@Composable
private fun ObserveOneTimeEvents(
    viewModel: GroupChatInfoViewModel,
    navigator: GroupChatInfoNavigator
) {
    val mediaPickerLauncher = rememberLauncherForImage { resultUri ->
        viewModel.handleAction(GroupChatInfoAction.ExternalGalleryContentSelected(resultUri))
    }

    viewModel.oneTimeEvent.observeWithLifecycle(minActiveState = Lifecycle.State.RESUMED) { event ->
        when (event) {
            is GroupChatInfoEvent.NavigateBack -> navigator.navigateBack()
            is GroupChatInfoEvent.NavigateToAddUsersScreen -> navigator.navigateToAddUsersScreen(event.chatId)
            is GroupChatInfoEvent.NavigateToProfileScreen -> navigator.navigateToUserScreen(event.userDeviceAddress)
            is GroupChatInfoEvent.ShowDialog -> navigator.showDialog(event.params)
            is GroupChatInfoEvent.OpenExternalGalleryForImage -> mediaPickerLauncher.launch(createPickImageIntent())
        }
    }
}

@Composable
private fun ScreenContent(state: GroupChatInfoState, actionListener: (GroupChatInfoAction) -> Unit) {
    BackHandler { actionListener(GroupChatInfoAction.BackButtonClicked) }

    ScreenContainer(
        topAppBar = { GroupChatInfoToolbar(state = state, actionListener = actionListener) }
    ) {
        ContentCrossFade(
            modifier = Modifier.fillMaxSize(),
            targetState = state,
            contentKey = { it::class },
        ) { animatedState ->
            when (animatedState) {
                is GroupChatInfoState.Loading -> {
                    LoadingScreenContent(state = animatedState, actionListener = actionListener)
                }

                is GroupChatInfoState.Loaded.Host -> {
                    LoadedHostScreenContent(state = animatedState, actionListener = actionListener)
                }

                is GroupChatInfoState.Loaded.Client -> {
                    LoadedClientScreenContent(state = animatedState, actionListener = actionListener)
                }
            }
        }
    }
}

@Composable
private fun BoxScope.LoadingScreenContent(
    state: GroupChatInfoState.Loading,
    actionListener: (GroupChatInfoAction) -> Unit,
) {

}

@Composable
private fun LoadedHostScreenContent(
    state: GroupChatInfoState.Loaded.Host,
    actionListener: (GroupChatInfoAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
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
                        ChatImage(
                            modifier = Modifier
                                .fillMaxSize(),
                            imageFileState = state.chat.pictureFileState,
                            chatName = state.chat.name,
                        )
                    }

                    is EditMode.Editing -> {
                        ChatImage(
                            modifier = Modifier
                                .fillMaxSize(),
                            imageFileState = animatedEditMode.updatedPictureFileState,
                            chatName = animatedEditMode.updatedName,
                            clickListener = { actionListener(GroupChatInfoAction.ChangePhotoClicked) },
                        )

                        if (animatedEditMode.updatedPictureFileState == null) {
                            ProfileImageActionIcon(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                painter = painterResource(id = R.drawable.ic_image),
                                clickListener = { actionListener(GroupChatInfoAction.ChangePhotoClicked) },
                            )
                        } else {
                            ProfileImageActionIcon(
                                modifier = Modifier.align(Alignment.BottomEnd),
                                painter = rememberVectorPainter(image = Icons.Default.Delete),
                                clickListener = { actionListener(GroupChatInfoAction.DeletePhotoClicked) },
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(ProfileImageBottomPadding))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ProfileContentHorizontalPadding),
        ) {
            ProfileFieldNameText(text = stringResource(id = R.string.group_name))
            when (state.editMode) {
                is EditMode.None -> {
                    ProfileFieldValueText(
                        modifier = Modifier.height(CustomTextFieldValueHeight),
                        text = state.chat.name ?: "",
                    )
                }

                is EditMode.Editing -> {
                    CustomTextField(
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.editMode.displayEmptyUpdatedNameError,
                        hint = stringResource(R.string.group_name_hint),
                        value = state.chat.name ?: "",
                        onValueChange = { actionListener(GroupChatInfoAction.OnUsernameChanged(it)) },
                    )
                }
            }
        }

        if (state.editMode is EditMode.None) {
            val dropDownDisplayStrategy = remember {
                DropDownDisplayStrategy.OnLongPress<ViewUser, ViewUserAction>(
                    displayRipple = true,
                    onActionClick = { item, action ->
                        actionListener(GroupChatInfoAction.UserActionClicked(user = item, action = action))
                    },
                    onPressListener = { item ->
                        actionListener(GroupChatInfoAction.UserClicked(user = item))
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AddMembersButton(
                modifier = Modifier.fillMaxWidth(),
                clickListener = { actionListener(GroupChatInfoAction.AddMembersClicked) },
            )
            UserDivider()

            state.chat.users.forEach { user ->
                val isAdmin = remember(state.chat.hostDeviceAddress, user.deviceAddress) {
                    state.chat.hostDeviceAddress == user.deviceAddress
                }
                UserItem(
                    user = user,
                    isAdmin = isAdmin,
                    dropDownDisplayStrategy = dropDownDisplayStrategy,
                )
            }
        }
    }
}

@Composable
private fun LoadedClientScreenContent(
    state: GroupChatInfoState.Loaded.Client,
    actionListener: (GroupChatInfoAction) -> Unit
) {
    val dropDownDisplayStrategy = remember {
        DropDownDisplayStrategy.OnLongPress<ViewUser, ViewUserAction>(
            displayRipple = true,
            onActionClick = { _, _ -> },
            onPressListener = { item ->
                actionListener(GroupChatInfoAction.UserClicked(user = item))
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(ProfileImageTopPadding))

        ChatImage(
            modifier = Modifier
                .size(ProfileImageSize)
                .align(Alignment.CenterHorizontally),
            imageFileState = state.chat.pictureFileState,
            chatName = state.chat.name,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ProfileContentHorizontalPadding),
        ) {
            Spacer(modifier = Modifier.height(ProfileImageBottomPadding))
            ProfileFieldNameText(text = stringResource(id = R.string.group_name))
            ProfileFieldValueText(
                modifier = Modifier.height(CustomTextFieldValueHeight),
                text = state.chat.name ?: "",
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        state.chat.users.forEach { user ->
            val isAdmin = remember(state.chat.hostDeviceAddress, user.deviceAddress) {
                state.chat.hostDeviceAddress == user.deviceAddress
            }
            UserItem(
                user = user,
                isAdmin = isAdmin,
                dropDownDisplayStrategy = dropDownDisplayStrategy,
            )
        }
    }
}

@Composable
private fun AddMembersButton(clickListener: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clickable { clickListener() }
            .padding(horizontal = ScreenContentHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(UserItemImageSize)
                .padding(10.dp),
            painter = painterResource(id = R.drawable.ic_person_add),
            contentDescription = "Add member",
            tint = LocalChatAppColorScheme.current.accent,
        )

        Spacer(modifier = Modifier.width(UserItemContentHorizontalPadding))
        Text(
            text = stringResource(id = R.string.add_members),
            style = TextStyle(
                fontSize = 15.sp,
                fontFamily = FontFamily.SansSerif,
                color = LocalChatAppColorScheme.current.accent,
                fontWeight = FontWeight.Normal,
            )
        )

    }
}

@Composable
private fun GroupChatInfoToolbar(state: GroupChatInfoState, actionListener: (GroupChatInfoAction) -> Unit) {
    ChatAppToolbar {
        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))
        ToolbarBackIconButton(navigateUp = { actionListener(GroupChatInfoAction.BackButtonClicked) })
        Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))

        val toolbarText = when (state) {
            is GroupChatInfoState.Loaded -> state.chat.name
            else -> null
        }
        ToolbarTitleText(
            modifier = Modifier
                .padding(end = ToolbarIconHorizontalPadding)
                .weight(1f),
            text = toolbarText ?: "",
        )

        (state as? GroupChatInfoState.Loaded.Host)?.let { loadedState ->
            Spacer(modifier = Modifier.weight(1f))

            ContentCrossFade(
                targetState = state.editMode,
                contentKey = { it::class },
            ) { animatedEditMode ->
                if (animatedEditMode is EditMode.Editing) {
                    ToolbarIconButton(
                        vector = Icons.Default.Done,
                        clickListener = { actionListener(GroupChatInfoAction.SaveClicked) },
                    )
                } else {
                    ToolbarIconButton(
                        vector = Icons.Default.Edit,
                        clickListener = { actionListener(GroupChatInfoAction.EditClicked) },
                    )
                }
            }
            Spacer(modifier = Modifier.width(ToolbarIconHorizontalPadding))
        }
    }
}

@Composable
private fun UserItem(
    user: ViewUser,
    isAdmin: Boolean,
    dropDownDisplayStrategy: DropDownDisplayStrategy<ViewUser, ViewUserAction>,
) {
    val actions = remember(user.actions) {
        user.actions.map { action ->
            DropdownMenuItemModel(
                text = UiText.Resource(action.nameStringRes),
                data = action,
            )
        }
    }

    ItemDropdownMenuContainer(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        item = user,
        displayStrategy = dropDownDisplayStrategy,
        actions = actions,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = UserItemHorizontalPadding, vertical = UserItemVerticalPadding)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UserImage(
                    modifier = Modifier.size(UserItemImageSize),
                    user = user,
                    userDeviceAddress = user.deviceAddress,
                )

                Spacer(modifier = Modifier.width(UserItemContentHorizontalPadding))

                Text(
                    modifier = Modifier.weight(1f),
                    text = user.userName ?: user.deviceAddress,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = LocalChatAppColorScheme.current.onScreenBackground,
                        fontWeight = FontWeight.Medium,
                    )
                )

                if (isAdmin) {
                    Spacer(modifier = Modifier.width(UserItemContentHorizontalPadding))
                    Text(
                        text = stringResource(id = R.string.group_admin),
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = LocalChatAppColorScheme.current.accent,
                            fontWeight = FontWeight.Normal,
                        )
                    )
                }
            }

            UserDivider()
        }
    }
}

@Composable
private fun UserDivider() {
    Box(
        modifier = Modifier
            .padding(
                start = UserItemHorizontalPadding + UserItemImageSize + UserItemContentHorizontalPadding,
                end = UserItemHorizontalPadding,
            )
            .fillMaxWidth()
            .height(1.dp)
            .background(color = LocalChatAppColorScheme.current.mainItemDivider),
    )
}
