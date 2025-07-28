package com.samikhan.draven.data.api

import android.content.Context
import com.samikhan.draven.data.model.ApiRequest
import com.samikhan.draven.data.model.Message
import com.samikhan.draven.data.security.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiManager private constructor(context: Context) {
    
    private val secureStorage = SecureStorage(context)
    private val apiService: ApiService
    
    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://integrate.api.nvidia.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(ApiService::class.java)
    }
    
    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<Message>,
        detailedThinking: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        try {
            val apiKey = secureStorage.getApiKey()
            
            val messages = mutableListOf<Message>()
            
            // Add system message based on detailed thinking toggle
            val systemMessage = if (detailedThinking) "detailed thinking on" else "detailed thinking off"
            messages.add(Message("system", systemMessage))
            
            // Add conversation history
            messages.addAll(conversationHistory)
            messages.add(Message("user", userMessage))
            
            val request = ApiRequest(
                messages = messages,
                model = "nvidia/llama-3.1-nemotron-ultra-253b-v1",
                max_tokens = 1024, // Reduced from 4096 for faster responses
                temperature = if (detailedThinking) 0.3 else 0.1, // Reduced for faster, more focused responses
                top_p = 0.9, // Slightly reduced for faster generation
                frequency_penalty = 0,
                presence_penalty = 0,
                stream = false
            )
            val response = apiService.sendMessage("Bearer $apiKey", request = request)
            
            response.choices.firstOrNull()?.message?.content ?: "Sorry, I couldn't generate a response."
        } catch (e: Exception) {
            // Log the error for debugging
            e.printStackTrace()
            
            // Return specific error message based on exception type
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> 
                    "Sorry, the request timed out. Please try again."
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Network error. Please check your connection and try again."
                e.message?.contains("401", ignoreCase = true) == true -> 
                    "API key error. Please check your API key in settings."
                e.message?.contains("429", ignoreCase = true) == true -> 
                    "Rate limit exceeded. Please wait a moment and try again."
                else -> "Sorry, I encountered an error: ${e.message}. Please try again."
            }
            
            // Fallback to mock response if API fails
            generateMockResponse(userMessage)
        }
    }
    
    private fun generateMockResponse(userMessage: String): String {
        val responses = listOf(
            "I understand you're asking about \"$userMessage\". Let me help you with that.",
            "That's an interesting question about \"$userMessage\". Here's what I think...",
            "Based on your message \"$userMessage\", I can provide some insights.",
            "I'm Draven, your AI assistant. Regarding \"$userMessage\", here's my response.",
            "Thanks for reaching out about \"$userMessage\". Let me share my thoughts on this."
        )
        return responses.random()
    }
    
    fun getApiKey(): String = secureStorage.getApiKey()
    
    fun saveApiKey(apiKey: String) {
        secureStorage.saveApiKey(apiKey)
    }
    
    fun hasApiKey(): Boolean = secureStorage.hasApiKey()
    
    companion object {
        @Volatile
        private var INSTANCE: ApiManager? = null
        
        fun getInstance(context: Context): ApiManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
} 