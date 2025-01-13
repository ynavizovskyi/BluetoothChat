package com.bluetoothchat.feature.chat.common

import android.util.Log
import com.bluetoothchat.core.db.datasource.MessageDataSource
import com.bluetoothchat.core.domain.model.Message
import com.bluetoothchat.core.ui.util.throttleLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class MessagePagingDelegate(
    private val messageDataSource: MessageDataSource,
) {

    private var currentWindowMessages: List<Message> = emptyList()
    private val firstVisibleMessageFlow = MutableStateFlow<String?>(null)

    fun updateWindowStartMessageId(messageId: String) {
        firstVisibleMessageFlow.value = messageId
    }

    fun observeChatMessages(chatId: String): Flow<List<Message>> {
        return firstVisibleMessageFlow
            .throttleLatest(100)
            .filter {
                val firstVisibleItemIndexInWindow =
                    currentWindowMessages.indexOfFirst { it.id == firstVisibleMessageFlow.value }
                Log.v(
                    "pager",
                    "observeChatMessages filter() FIRST visible: $firstVisibleItemIndexInWindow size: ${currentWindowMessages.size}"
                )
                currentWindowMessages.isEmpty()
                        || (currentWindowMessages.size >= PAGE_SIZE && (currentWindowMessages.size - firstVisibleItemIndexInWindow) < PAGE_SIZE / 2)
                        || currentWindowMessages.size - firstVisibleItemIndexInWindow > PAGE_SIZE * 2
            }.map { messageId ->
                val nextWindow = if (messageId != null) {
                    messageDataSource.getOlderThan(chatId = chatId, messageId = messageId, count = PAGE_SIZE)
                } else {
                    messageDataSource.getNewest(chatId = chatId, count = PAGE_SIZE)
                }
                nextWindow.lastOrNull()
            }
            .filter { it != null || currentWindowMessages.isEmpty() }
            .flatMapLatest { message ->
                Log.v("pager", "observeAllNewerThan flatMapLatest messages $message")

                if(message != null){
                    messageDataSource.observeAllStartingWithThan(chatId = chatId, messageId = message.id)
                } else {
                    messageDataSource.observeAll(chatId = chatId)
                }
            }
            .onEach { currentWindowMessages = it }

    }

    companion object {
        private const val PAGE_SIZE = 50
    }
}
