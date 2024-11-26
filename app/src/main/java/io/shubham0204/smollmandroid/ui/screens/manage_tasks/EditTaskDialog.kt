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

package io.shubham0204.smollmandroid.ui.screens.manage_tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.shubham0204.smollmandroid.ui.components.DialogTitleText
import io.shubham0204.smollmandroid.ui.theme.AppFontFamily

@Composable
fun EditTaskDialog(viewModel: TasksViewModel) {
    val selectedTask by remember { viewModel.selectedTaskState }
    var taskName by remember { mutableStateOf(selectedTask?.name ?: "") }
    var systemPrompt by remember { mutableStateOf(selectedTask?.systemPrompt ?: "") }
    LaunchedEffect(selectedTask) {
        taskName = selectedTask?.name ?: ""
        systemPrompt = selectedTask?.systemPrompt ?: ""
    }
    var showEditTaskDialog by remember { viewModel.showEditTaskDialogState }
    if (showEditTaskDialog && selectedTask != null) {
        Dialog(onDismissRequest = { showEditTaskDialog = false }) {
            Column(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DialogTitleText(text = "Edit Task")
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name", fontFamily = AppFontFamily) },
                    textStyle = TextStyle(fontFamily = AppFontFamily),
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Words)
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    value = systemPrompt,
                    onValueChange = { systemPrompt = it },
                    label = { Text("System Prompt", fontFamily = AppFontFamily) },
                    textStyle = TextStyle(fontFamily = AppFontFamily),
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    enabled = taskName.isNotBlank() && systemPrompt.isNotBlank(),
                    onClick = {
                        // selectedTask is never null
                        // as we check for null in the if condition above
                        viewModel.updateTask(
                            selectedTask!!.copy(
                                name = taskName,
                                systemPrompt = systemPrompt,
                            ),
                        )
                        showEditTaskDialog = false
                    },
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Add")
                    Text("Add", fontFamily = AppFontFamily)
                }
            }
        }
    }
}
