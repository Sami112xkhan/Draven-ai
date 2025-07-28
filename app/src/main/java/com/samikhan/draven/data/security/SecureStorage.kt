package com.samikhan.draven.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "draven_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val API_KEY = "api_key"
        private const val DEFAULT_API_KEY = "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"
    }
    
    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString(API_KEY, apiKey).apply()
    }
    
    fun getApiKey(): String {
        return sharedPreferences.getString(API_KEY, DEFAULT_API_KEY) ?: DEFAULT_API_KEY
    }
    
    fun hasApiKey(): Boolean {
        val key = getApiKey()
        return key != DEFAULT_API_KEY && key.isNotEmpty()
    }
} 