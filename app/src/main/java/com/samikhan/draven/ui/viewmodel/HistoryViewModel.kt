package com.samikhan.draven.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.samikhan.draven.data.model.Conversation
import com.samikhan.draven.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: ChatRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.getAllConversations().collect { conversations ->
                _uiState.value = _uiState.value.copy(
                    conversations = conversations
                )
            }
        }
    }
    
    fun deleteConversation(conversation: Conversation) {
        viewModelScope.launch {
            repository.deleteConversation(conversation.id)
        }
    }
    
    fun selectConversation(conversationId: String) {
        _uiState.value = _uiState.value.copy(
            selectedConversationId = conversationId
        )
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(ChatRepository(context)) as T
        }
    }
}

data class HistoryUiState(
    val conversations: List<Conversation> = emptyList(),
    val selectedConversationId: String? = null
) 