package com.samikhan.draven.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStorage(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "draven_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // If encrypted prefs fail, fall back to regular SharedPreferences
        println("EncryptedSharedPreferences failed, using regular SharedPreferences: ${e.message}")
        context.getSharedPreferences("draven_secure_prefs", Context.MODE_PRIVATE)
    }
    
    companion object {
        private const val API_KEY = "api_key"
        private const val DEFAULT_API_KEY = "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"
    }
    
    fun saveApiKey(apiKey: String) {
        println("Saving API key: ${apiKey.take(10)}...")
        sharedPreferences.edit().putString(API_KEY, apiKey).apply()
        println("API key saved successfully")
    }
    
    fun getApiKey(): String {
        return try {
            val key = sharedPreferences.getString(API_KEY, DEFAULT_API_KEY) ?: DEFAULT_API_KEY
            println("Retrieved API key: ${key.take(10)}...")
            key
        } catch (e: Exception) {
            println("Error retrieving API key, using default: ${e.message}")
            DEFAULT_API_KEY
        }
    }
    
    fun hasApiKey(): Boolean {
        val key = getApiKey()
        return key.isNotEmpty()
    }
} 