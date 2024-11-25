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

package io.shubham0204.smollmandroid.ui.screens.chat

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.shubham0204.smollmandroid.data.Chat
import io.shubham0204.smollmandroid.ui.components.AppAlertDialog
import io.shubham0204.smollmandroid.ui.theme.AppAccentColor
import io.shubham0204.smollmandroid.ui.theme.AppFontFamily

@Composable
fun DrawerUI(
    viewModel: ChatScreenViewModel,
    onItemClick: (Chat) -> Unit,
    onManageTasksClick: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier =
            Modifier.background(Color.White)
                .windowInsetsPadding(WindowInsets.safeContent)
                .padding(8.dp)
                .requiredWidth(300.dp)
                .fillMaxHeight(),
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val chatCount = viewModel.chatsDB.getChatsCount()
                viewModel.chatsDB.addChat(chatName = "Untitled ${chatCount + 1}")
            },
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Chat")
            Text("New Chat", fontFamily = AppFontFamily)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Previous Chats",
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        ChatsList(viewModel, onItemClick)
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onManageTasksClick() },
            verticalAlignment = Alignment.CenterVertically
            ) {
            Icon(Icons.Default.AddTask, contentDescription = "Manage Tasks", tint = AppAccentColor)
            Text(
                "Manage Tasks",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                fontFamily = AppFontFamily,
            )
        }

    }
    AppAlertDialog()
}

@Composable
private fun ColumnScope.ChatsList(
    viewModel: ChatScreenViewModel,
    onItemClick: (Chat) -> Unit,
) {
    val chats by viewModel.getChats().collectAsState(emptyList())
    LazyColumn(modifier = Modifier.weight(1f)) {
        items(chats) { chat -> ChatListItem(chat, onItemClick) }
    }
}

@Composable
private fun LazyItemScope.ChatListItem(
    chat: Chat,
    onItemClick: (Chat) -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth().clickable { onItemClick(chat) }.padding(8.dp).animateItem(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(chat.name, fontSize = 16.sp, fontFamily = AppFontFamily)
            Text(
                text = DateUtils.getRelativeTimeSpanString(chat.dateUsed.time).toString(),
                fontSize = 12.sp,
                fontFamily = AppFontFamily,
            )
        }
    }
}
