package com.bluetoothchat.feature.chat.group.chatinfo

import com.bluetoothchat.core.analytics.AnalyticsClient
import com.bluetoothchat.core.analytics.AnalyticsEvent
import com.bluetoothchat.core.analytics.toPropertyMap
import com.bluetoothchat.core.domain.model.Chat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupChatInfoAnalyticsClient @Inject constructor(private val analyticsClient: AnalyticsClient) {

    suspend fun reportScreenShown(chat: Chat.Group) {
        val event = AnalyticsEvent(
            name = EVENT_SCREEN_SHOWN,
            params = chat.toPropertyMap()
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportEditClicked() {
        val event = AnalyticsEvent(
            name = EVENT_EDIT_CLICKED,
            params = emptyMap()
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportSaveError(isGroupImageSet: Boolean) {
        val event = AnalyticsEvent(
            name = EVENT_SAVE_ERROR,
            params = mapOf(PROPERTY_GROUP_IMAGE_SET to isGroupImageSet)
        )
        analyticsClient.logEvent(event)
    }

    suspend fun reportSaveSuccess(isGroupImageSet: Boolean) {
        val event = AnalyticsEvent(
            name = EVENT_SAVE_SUCCESS,
            params = mapOf(PROPERTY_GROUP_IMAGE_SET to isGroupImageSet)
        )
        analyticsClient.logEvent(event)
    }

    companion object {
        private const val EVENT_SCREEN_SHOWN = "group_chat_info_screen_shown"
        private const val EVENT_EDIT_CLICKED = "group_chat_info_edit_clicked"
        private const val EVENT_SAVE_ERROR = "group_chat_info_save_error"
        private const val EVENT_SAVE_SUCCESS = "group_chat_info_save_success"

        private const val PROPERTY_GROUP_IMAGE_SET = "group_image_set"
    }
}
