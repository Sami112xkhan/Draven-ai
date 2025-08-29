package com.samikhan.draven.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.samikhan.draven.data.model.ChatMessage
import com.samikhan.draven.data.model.Conversation
import com.samikhan.draven.data.model.AIModelManager
import com.samikhan.draven.data.analytics.AnalyticsManager
import com.samikhan.draven.data.repository.ChatRepository
import com.samikhan.draven.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository, 
    private val voiceManager: VoiceManager,
    private val context: Context,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    // Voice state
    private val _isVoiceModeEnabled = MutableStateFlow(false)
    val isVoiceModeEnabled: StateFlow<Boolean> = _isVoiceModeEnabled.asStateFlow()
    
    // Permission state
    private val _hasMicrophonePermission = MutableStateFlow(false)
    val hasMicrophonePermission: StateFlow<Boolean> = _hasMicrophonePermission.asStateFlow()
    

    
    init {
        // Initialize AI Model Manager
        AIModelManager.initialize(context)
        
        // Check initial permission status
        checkMicrophonePermission()
        
        viewModelScope.launch {
            repository.messages.collect { messages ->
                val newMessages = messages.filter { it.role != com.samikhan.draven.data.model.MessageRole.SYSTEM }
                _uiState.value = _uiState.value.copy(
                    messages = newMessages,
                    isLoading = messages.any { it.isLoading }
                )
                
                // Check for new AI responses to speak and auto-turn off detailed thinking
                val lastMessage = newMessages.lastOrNull()
                if (lastMessage?.role == com.samikhan.draven.data.model.MessageRole.ASSISTANT && 
                    lastMessage.content.isNotEmpty() && 
                    !lastMessage.isLoading) {
                    
                    // Auto-turn off detailed thinking after each response (saves tokens)
                    if (_uiState.value.detailedThinking) {
                        _uiState.value = _uiState.value.copy(detailedThinking = false)
                    }
                    
                    // Speak if voice mode is enabled (regardless of critical thinking)
                    if (_isVoiceModeEnabled.value) {
                        speakResponse(lastMessage.content)
                    }
                }
            }
        }
        
        // Collect voice state
        viewModelScope.launch {
            voiceManager.isListening.collect { isListening ->
                _uiState.value = _uiState.value.copy(isListening = isListening)
            }
        }
        
        viewModelScope.launch {
            voiceManager.isSpeaking.collect { isSpeaking ->
                _uiState.value = _uiState.value.copy(isSpeaking = isSpeaking)
            }
        }
        
        viewModelScope.launch {
            voiceManager.transcribedText.collect { transcribedText ->
                _uiState.value = _uiState.value.copy(transcribedText = transcribedText)
            }
        }
        
        viewModelScope.launch {
            voiceManager.speechConfidence.collect { confidence ->
                _uiState.value = _uiState.value.copy(speechConfidence = confidence)
            }
        }
        
        viewModelScope.launch {
            voiceManager.voiceError.collect { error ->
                _uiState.value = _uiState.value.copy(voiceError = error)
            }
        }
    }
    
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            // Track analytics after sending message
            analyticsManager.updateDailyUsageStatistics()
            
            repository.sendMessage(content.trim(), _uiState.value.detailedThinking)
            _uiState.value = _uiState.value.copy(
                inputText = ""
            )
        }
    }
    

    
    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }
    
    fun toggleDetailedThinking() {
        _uiState.value = _uiState.value.copy(
            detailedThinking = !_uiState.value.detailedThinking
        )
    }
    
    fun toggleVoiceMode() {
        val newValue = !_isVoiceModeEnabled.value
        _isVoiceModeEnabled.value = newValue
        _uiState.value = _uiState.value.copy(isVoiceModeEnabled = newValue)
    }
    
    fun startVoiceListening() {
        voiceManager.startListening { transcribedText ->
            if (transcribedText.isNotBlank()) {
                sendMessage(transcribedText)
            }
        }
    }
    
    fun stopVoiceListening() {
        voiceManager.stopListening()
    }
    
    fun toggleVoiceListening() {
        if (_uiState.value.isListening) {
            stopVoiceListening()
        } else {
            startVoiceListening()
        }
    }
    
    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }
    
    fun stopVoiceResponse() {
        voiceManager.stopSpeaking()
        _uiState.value = _uiState.value.copy(isSpeaking = false)
    }
    
    fun speakResponse(text: String) {
        if (_isVoiceModeEnabled.value) {
            voiceManager.speak(text)
        }
    }
    
    private fun checkMicrophonePermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        _hasMicrophonePermission.value = hasPermission
    }
    
    fun requestMicrophonePermission() {
        // This will be called from the UI when permission is needed
        checkMicrophonePermission()
    }
    
    fun onPermissionGranted() {
        _hasMicrophonePermission.value = true
    }
    
    fun createNewChat() {
        viewModelScope.launch {
            repository.createNewConversation()
        }
    }
    
    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            repository.loadConversation(conversationId)
        }
    }
    
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val voiceManager = VoiceManager(context)
            val analyticsManager = AnalyticsManager(
                database = com.samikhan.draven.data.database.DravenDatabase.getDatabase(context),
                context = context
            )
            return ChatViewModel(ChatRepository(context), voiceManager, context, analyticsManager) as T
        }
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val detailedThinking: Boolean = false,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val transcribedText: String = "",
    val speechConfidence: Float = 0f,
    val voiceError: String? = null,
    val isVoiceModeEnabled: Boolean = false
) 