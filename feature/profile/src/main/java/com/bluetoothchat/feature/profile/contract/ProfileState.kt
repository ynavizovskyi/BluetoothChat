package com.bluetoothchat.feature.profile.contract

import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.model.ViewUser
import com.bluetoothchat.core.ui.mvi.contract.ViewState


internal interface ProfileState : ViewState {

    object Loading : ProfileState

    sealed interface Loaded : ProfileState {
        val user: ViewUser

        fun copyState(user: ViewUser): Loaded

        data class Me(
            override val user: ViewUser,
            val editMode: EditMode,
            val isInitialSetUp: Boolean,
        ) : Loaded {
            override fun copyState(user: ViewUser) = copy(user = user)
        }

        data class Other(override val user: ViewUser) : Loaded {
            override fun copyState(user: ViewUser) = copy(user = user)
        }
    }

}

internal sealed interface EditMode {
    object None : EditMode

    data class Editing(
        val updatedPictureFileState: FileState?,
        val updatedName: String?,
        val updatedColor: Int,
        val displayEmptyUpdatedNameError: Boolean,
    ) : EditMode
}
