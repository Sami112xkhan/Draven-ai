package com.samikhan.draven.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.samikhan.draven.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: ChatRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val DEVELOPER_PASSWORD = "mySecret123"
    }
    
    fun checkPassword(password: String): Boolean {
        return password == DEVELOPER_PASSWORD
    }
    
    fun authenticate(password: String) {
        val isAuthenticated = checkPassword(password)
        _uiState.value = _uiState.value.copy(
            isAuthenticated = isAuthenticated,
            passwordError = !isAuthenticated,
            apiKey = if (isAuthenticated) repository.getApiKey() else ""
        )
    }
    
    fun updateApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(apiKey = apiKey)
    }
    
    fun saveApiKey() {
        val apiKey = _uiState.value.apiKey
        if (apiKey.isNotBlank()) {
            repository.saveApiKey(apiKey)
            _uiState.value = _uiState.value.copy(
                isSaved = true,
                saveError = false
            )
        } else {
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
            apiKey = ""
        )
    }
    
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(ChatRepository(context)) as T
        }
    }
}

data class SettingsUiState(
    val isAuthenticated: Boolean = false,
    val passwordError: Boolean = false,
    val apiKey: String = "",
    val isSaved: Boolean = false,
    val saveError: Boolean = false
) 