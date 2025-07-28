package com.samikhan.draven.data.repository

import android.content.Context
import androidx.room.Room
import com.samikhan.draven.data.api.ApiManager
import com.samikhan.draven.data.database.DravenDatabase
import com.samikhan.draven.data.model.ChatMessage
import com.samikhan.draven.data.model.Conversation
import com.samikhan.draven.data.model.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class ChatRepository(context: Context) {
    
    private val apiManager = ApiManager.getInstance(context)
    private val database = Room.databaseBuilder(
        context,
        DravenDatabase::class.java,
        "draven_database"
    ).build()
    
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    
    private val _currentConversationId = MutableStateFlow<String?>(null)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    companion object {
        private const val DRAVEN_SYSTEM_PROMPT = "You are Draven, a powerful and witty AI assistant with a futuristic personality. You respond with clarity, confidence, and insight. Refer to yourself as Draven if asked who you are."
    }
    
    suspend fun createNewConversation(): String {
        val conversationId = java.util.UUID.randomUUID().toString()
        val conversation = Conversation(
            id = conversationId,
            title = "New Chat",
            timestamp = System.currentTimeMillis()
        )
        conversationDao.insertConversation(conversation)
        
        // Add system message
        val systemMessage = ChatMessage(
            conversationId = conversationId,
            content = DRAVEN_SYSTEM_PROMPT,
            role = MessageRole.SYSTEM
        )
        messageDao.insertMessage(systemMessage)
        
        _currentConversationId.value = conversationId
        loadMessagesForConversation(conversationId)
        
        return conversationId
    }
    
    suspend fun loadConversation(conversationId: String) {
        _currentConversationId.value = conversationId
        loadMessagesForConversation(conversationId)
    }
    
    private suspend fun loadMessagesForConversation(conversationId: String) {
        val messages = messageDao.getMessagesForConversation(conversationId).first()
        _messages.value = messages
    }
    
    suspend fun sendMessage(content: String, detailedThinking: Boolean = false) {
        val conversationId = _currentConversationId.value ?: createNewConversation()
        
        // Add user message
        val userMessage = ChatMessage(
            conversationId = conversationId,
            content = content,
            role = MessageRole.USER
        )
        messageDao.insertMessage(userMessage)
        
        // Add loading message
        val loadingMessage = ChatMessage(
            conversationId = conversationId,
            content = "",
            role = MessageRole.ASSISTANT,
            isLoading = true
        )
        messageDao.insertMessage(loadingMessage)
        
        // Reload messages to show loading state
        loadMessagesForConversation(conversationId)
        
        try {
            // Get all messages for this conversation (including system message)
            val allMessages = messageDao.getMessagesForConversation(conversationId).first()
            
            // Convert chat messages to API format (excluding system message for API)
            val apiMessages = allMessages
                .filter { it.role != MessageRole.SYSTEM && !it.isLoading }
                .map { com.samikhan.draven.data.model.Message(
                    role = when (it.role) {
                        MessageRole.USER -> "user"
                        MessageRole.ASSISTANT -> "assistant"
                        MessageRole.SYSTEM -> "system"
                    },
                    content = it.content
                ) }
            
            // Add system message for API call
            val messagesWithSystem = mutableListOf<com.samikhan.draven.data.model.Message>()
            messagesWithSystem.add(com.samikhan.draven.data.model.Message("system", DRAVEN_SYSTEM_PROMPT))
            messagesWithSystem.addAll(apiMessages)
            
            // Get AI response
            val aiResponse = apiManager.sendMessage(content, messagesWithSystem, detailedThinking)
            
            // Remove loading message
            messageDao.deleteLoadingMessagesForConversation(conversationId)
            
            val aiMessage = ChatMessage(
                conversationId = conversationId,
                content = aiResponse,
                role = MessageRole.ASSISTANT
            )
            messageDao.insertMessage(aiMessage)
            
            // Update conversation title and last message
            val conversation = conversationDao.getConversationById(conversationId)
            conversation?.let {
                val updatedConversation = it.copy(
                    title = content.take(50) + if (content.length > 50) "..." else "",
                    lastMessage = aiResponse.take(100) + if (aiResponse.length > 100) "..." else ""
                )
                conversationDao.insertConversation(updatedConversation)
            }
            
            // Reload messages
            loadMessagesForConversation(conversationId)
            
        } catch (e: Exception) {
            // Remove loading message and add error message
            messageDao.deleteLoadingMessagesForConversation(conversationId)
            
            val errorMessage = ChatMessage(
                conversationId = conversationId,
                content = "Sorry, I encountered an error. Please try again.",
                role = MessageRole.ASSISTANT
            )
            messageDao.insertMessage(errorMessage)
            
            loadMessagesForConversation(conversationId)
        }
    }
    
    fun getAllConversations() = conversationDao.getAllConversations()
    
    suspend fun deleteConversation(conversationId: String) {
        conversationDao.deleteConversationById(conversationId)
        messageDao.deleteMessagesForConversation(conversationId)
        
        if (_currentConversationId.value == conversationId) {
            _currentConversationId.value = null
            _messages.value = emptyList()
        }
    }
    
    fun getApiKey(): String = apiManager.getApiKey()
    
    fun saveApiKey(apiKey: String) {
        apiManager.saveApiKey(apiKey)
    }
    
    fun hasApiKey(): Boolean = apiManager.hasApiKey()
} 