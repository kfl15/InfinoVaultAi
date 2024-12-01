/*
 * Copyright (C) 2024 Shubham Panchal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shubham0204.smollmandroid.data

import io.objectbox.kotlin.flow
import io.shubham0204.smollmandroid.ui.screens.chat.LOGD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.koin.core.annotation.Single
import java.util.Date

@Single
class ChatsDB {
    private val chatsBox = ObjectBoxStore.store.boxFor(Chat::class.java)

    /** Get all chats from the database sorted by dateUsed in descending order. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChats(): Flow<List<Chat>> =
        chatsBox
            .query()
            .orderDesc(Chat_.dateUsed)
            .build()
            .flow()
            .flowOn(Dispatchers.IO)

    fun loadDefaultChat(): Chat {
        val defaultChat =
            if (getChatsCount() == 0L) {
                addChat("Untitled")
                getRecentlyUsedChat()!!
            } else {
                // Given that chatsDB has at least one chat
                // chatsDB.getRecentlyUsedChat() will never return null
                getRecentlyUsedChat()!!
            }
        LOGD("Default chat is $defaultChat")
        return defaultChat
    }

    /**
     * Get the most recently used chat from the database. This function might return null, if there
     * are no chats in the database.
     */
    fun getRecentlyUsedChat(): Chat? =
        chatsBox
            .query()
            .orderDesc(Chat_.dateUsed)
            .build()
            .findFirst()

    fun addChat(
        chatName: String,
        systemPrompt: String = "You are a helpful assistant.",
        llmModelId: Long = -1,
    ): Long =
        chatsBox.put(
            Chat(
                name = chatName,
                systemPrompt = systemPrompt,
                dateCreated = Date(),
                dateUsed = Date(),
                llmModelId = llmModelId,
            ),
        )

    /** Update the chat in the database. ObjectBox overwrites the entry if it already exists. */
    fun updateChat(modifiedChat: Chat) {
        chatsBox.put(modifiedChat)
    }

    fun deleteChat(chat: Chat) {
        chatsBox.remove(chat)
    }

    fun getChatsCount(): Long = chatsBox.count()
}
