package com.example.chatbottextandalsoimage.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbottextandalsoimage.ChatState
import com.example.chatbottextandalsoimage.ChatUiEvent
import com.example.chatbottextandalsoimage.data.Chat
import com.example.chatbottextandalsoimage.data.ChatData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()

    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.SendPrompt -> {
                if (event.prompt.isNotEmpty()) {
                    addPrompt(
                        prompt = event.prompt,
                        bitmap = event.bitmap
                    )

                    if (event.bitmap != null) {
                        getResponseWithImage(
                            prompt = event.prompt,
                            bitmap = event.bitmap
                        )
                    } else {
                        getResponse(prompt = event.prompt)
                    }
                }
            }

            is ChatUiEvent.UpdatePrompt -> {
                _chatState.update {
                    it.copy(prompt = event.newPrompt)
                }
            }
        }
    }

    private fun addPrompt(prompt: String, bitmap: Bitmap?) {
        _chatState.update {
            it.copy(
                chatList = it.chatList.toMutableList().apply {
                    add(
                        index = 0,
                        element = Chat(
                            prompt = prompt,
                            bitmap = bitmap,
                            isFromUser = true
                        )
                    )
                },
                prompt = "",
                bitmap = null
            )
        }
    }

    private fun getResponse(prompt: String) {
        viewModelScope.launch {
            val chat = ChatData.getResponse(prompt = prompt)
            _chatState.update {
                it.copy(
                    chatList = it.chatList.toMutableList().apply {
                        add(
                            index = 0,
                            element = chat
                        )
                    }
                )
            }
        }
    }

    private fun getResponseWithImage(prompt: String, bitmap: Bitmap) {
        viewModelScope.launch {
            val chat = ChatData.getResponseWithImage(prompt = prompt, bitmap = bitmap)
            _chatState.update {
                it.copy(
                    chatList = it.chatList.toMutableList().apply {
                        add(index = 0, element = chat)
                    }
                )
            }
        }
    }
}