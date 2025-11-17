package com.example.e_wastehubkenya.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.ChatChannel
import com.example.e_wastehubkenya.data.model.ChatMessage
import com.example.e_wastehubkenya.data.repository.ChatRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()

    private val _channelId = MutableLiveData<Resource<String>>()
    val channelId: LiveData<Resource<String>> = _channelId

    private val _messages = MutableLiveData<Resource<List<ChatMessage>>>()
    val messages: LiveData<Resource<List<ChatMessage>>> = _messages

    private val _sendMessageResult = MutableLiveData<Resource<Unit>>()
    val sendMessageResult: LiveData<Resource<Unit>> = _sendMessageResult

    private val _chatChannels = MutableLiveData<Resource<List<ChatChannel>>>()
    val chatChannels: LiveData<Resource<List<ChatChannel>>> = _chatChannels

    private val _channelDetails = MutableLiveData<Resource<ChatChannel>>()
    val channelDetails: LiveData<Resource<ChatChannel>> = _channelDetails

    fun getOrCreateChatChannel(sellerId: String, listingId: String) {
        viewModelScope.launch {
            chatRepository.getOrCreateChatChannel(sellerId, listingId).collectLatest {
                _channelId.postValue(it)
            }
        }
    }

    fun sendMessage(channelId: String, message: ChatMessage) {
        viewModelScope.launch {
            chatRepository.sendMessage(channelId, message).collectLatest {
                _sendMessageResult.postValue(it)
            }
        }
    }

    fun getMessages(channelId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(channelId).collectLatest {
                _messages.postValue(it)
            }
        }
    }

    fun getChatChannels() {
        viewModelScope.launch {
            chatRepository.getChatChannels().collectLatest {
                _chatChannels.postValue(it)
            }
        }
    }

    fun markMessagesAsRead(channelId: String) {
        chatRepository.markMessagesAsRead(channelId)
    }

    fun getChannelDetails(channelId: String) {
        viewModelScope.launch {
            chatRepository.getChannelDetails(channelId).collectLatest {
                _channelDetails.postValue(it)
            }
        }
    }
}