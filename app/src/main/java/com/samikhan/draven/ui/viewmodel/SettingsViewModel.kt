package com.samikhan.draven.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.samikhan.draven.data.repository.ChatRepository
import com.samikhan.draven.data.model.AIModelManager
import com.samikhan.draven.data.preferences.StartupPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: ChatRepository,
    private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private val startupPreferences = StartupPreferences(context)
    
    companion object {
        private const val DEVELOPER_PASSWORD = "mySecret123"
    }
    
    fun checkPassword(password: String): Boolean {
        return password == DEVELOPER_PASSWORD
    }
    
    fun authenticate(password: String) {
        val isAuthenticated = checkPassword(password)
        if (isAuthenticated) {
            // Load current API keys for both models
            val nemotronModel = AIModelManager.getModelConfig("nvidia-nemotron")
            val gptOssModel = AIModelManager.getModelConfig("gpt-oss")
            
            _uiState.value = _uiState.value.copy(
                isAuthenticated = true,
                passwordError = false,
                nemotronApiKey = nemotronModel?.apiKey ?: "",
                gptOssApiKey = gptOssModel?.apiKey ?: ""
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isAuthenticated = false,
                passwordError = true,
                nemotronApiKey = "",
                gptOssApiKey = ""
            )
        }
    }
    
    fun updateNemotronApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(nemotronApiKey = apiKey)
    }
    
    fun updateGptOssApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(gptOssApiKey = apiKey)
    }
    
    fun saveApiKeys() {
        val nemotronApiKey = _uiState.value.nemotronApiKey
        val gptOssApiKey = _uiState.value.gptOssApiKey
        
        println("SettingsViewModel: Attempting to save API keys...")
        println("SettingsViewModel: NeMoTron API key: ${nemotronApiKey.take(10)}...")
        println("SettingsViewModel: GPT-OSS API key: ${gptOssApiKey.take(10)}...")
        
        if (nemotronApiKey.isNotBlank() && gptOssApiKey.isNotBlank()) {
            // Update the model configurations with new API keys
            val nemotronModel = AIModelManager.getModelConfig("nvidia-nemotron")
            val gptOssModel = AIModelManager.getModelConfig("gpt-oss")
            
            nemotronModel?.let { model ->
                val updatedModel = model.copy(apiKey = nemotronApiKey)
                AIModelManager.updateModelConfig("nvidia-nemotron", updatedModel)
            }
            
            gptOssModel?.let { model ->
                val updatedModel = model.copy(apiKey = gptOssApiKey)
                AIModelManager.updateModelConfig("gpt-oss", updatedModel)
            }
            
            // Also save to repository for backward compatibility
            repository.saveApiKey(nemotronApiKey)
            
            println("SettingsViewModel: API keys saved successfully")
            _uiState.value = _uiState.value.copy(
                isSaved = true,
                saveError = false
            )
        } else {
            println("SettingsViewModel: One or both API keys are blank, not saving")
            _uiState.value = _uiState.value.copy(
                saveError = true,
                isSaved = false
            )
        }
    }
    
    fun clearPasswordError() {
        _uiState.value = _uiState.value.copy(passwordError = false)
    }
    
    fun clearSaveStatus() {
        _uiState.value = _uiState.value.copy(
            isSaved = false,
            saveError = false
        )
    }
    
    fun resetAuthentication() {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = false,
            passwordError = false,
            nemotronApiKey = "",
            gptOssApiKey = ""
        )
    }
    
    fun enableStartupVideoNextTime() {
        startupPreferences.enableStartupVideoNextTime()
        _uiState.value = _uiState.value.copy(
            startupVideoEnabled = true
        )
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(ChatRepository(context), context) as T
        }
    }
}

data class SettingsUiState(
    val isAuthenticated: Boolean = false,
    val passwordError: Boolean = false,
    val nemotronApiKey: String = "",
    val gptOssApiKey: String = "",
    val isSaved: Boolean = false,
    val saveError: Boolean = false,
    val startupVideoEnabled: Boolean = false
) 