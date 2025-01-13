package com.bluetoothchat.core.ui.model.mapper

import com.bluetoothchat.core.filemanager.file.FileState
import com.bluetoothchat.core.ui.model.ViewMessageAction
import com.bluetoothchat.core.ui.model.ViewMessageContent
import com.bluetoothchat.core.ui.model.primaryContent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewMessageActionsMapper @Inject constructor() {

    suspend fun map(
        content: List<ViewMessageContent>,
        canSendMessages: Boolean,
    ): List<ViewMessageAction> {
        val primaryContent = content.primaryContent()
        return mutableListOf<ViewMessageAction>().apply {
            if (canSendMessages) add(ViewMessageAction.REPLY)
            when {
                primaryContent is ViewMessageContent.Text -> add(ViewMessageAction.COPY)
                primaryContent is ViewMessageContent.Image && primaryContent.file is FileState.Downloaded -> add(
                    ViewMessageAction.SAVE_TO_GALLERY
                )
            }
        }
    }

}
