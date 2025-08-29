package com.samikhan.draven.data.api

import android.content.Context
import com.samikhan.draven.data.model.ApiRequest
import com.samikhan.draven.data.model.Message
import com.samikhan.draven.data.model.AIModelManager
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
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
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
            val currentModel = AIModelManager.getCurrentModel()
            if (currentModel == null) {
                return@withContext "Error: No AI model selected."
            }
            
            // Use the model's default API key directly
            val apiKey = currentModel.apiKey
            
            val messages = mutableListOf<Message>()
            
            // Add system message based on detailed thinking toggle and model
            // GPT-OSS doesn't use system messages (as per shell code)
            if (currentModel.id != "gpt-oss") {
                val systemMessage = when {
                    detailedThinking -> 
                        "You are a helpful AI assistant. Provide detailed, thorough responses with comprehensive explanations and analysis."
                    else -> 
                        "You are a helpful AI assistant. Provide concise, direct responses without unnecessary introductions."
                }
                messages.add(Message("system", systemMessage))
            }
            
            // For GPT-OSS, only send user message (as per shell code)
            if (currentModel.id == "gpt-oss") {
                messages.add(Message("user", userMessage))
            } else {
                // For other models, include conversation history
                val limitedHistory = conversationHistory.takeLast(10)
                messages.addAll(limitedHistory)
                messages.add(Message("user", userMessage))
            }
            
            val request = ApiRequest(
                messages = messages,
                model = currentModel.modelName,
                max_tokens = currentModel.maxTokens,
                temperature = currentModel.temperature.toDouble(),
                top_p = 1.0,
                frequency_penalty = 0,
                presence_penalty = 0,
                stream = false, // We don't support streaming in our API service
                reasoning_effort = if (currentModel.id == "gpt-oss") "medium" else null
            )
            
            // Debug logging for request
            println("Sending request to model: ${currentModel.modelName}")
            println("Request max_tokens: ${currentModel.maxTokens}")
            println("Request temperature: ${currentModel.temperature.toDouble()}")
            println("Request top_p: 1.0")
            println("Request reasoning_effort: ${if (currentModel.id == "gpt-oss") "medium" else "null"}")
            println("Request messages count: ${messages.size}")
            println("Request messages: $messages")
            println("API Key (first 10 chars): ${apiKey.take(10)}...")
            println("About to make API call...")
            val response = apiService.sendMessage("Bearer $apiKey", request = request)
            println("API call completed!")
            
            // Debug logging
            println("API Response: $response")
            println("Response choices: ${response.choices}")
            println("Response choices size: ${response.choices?.size}")
            
            // Add null checks for response
            if (response.choices.isNullOrEmpty()) {
                return@withContext "Sorry, I couldn't generate a response. The API returned an empty response."
            }
            
            val firstChoice = response.choices.firstOrNull()
            if (firstChoice == null) {
                return@withContext "Sorry, I couldn't generate a response. No choices available."
            }
            
            println("First choice: $firstChoice")
            println("First choice message: ${firstChoice.message}")
            println("First choice content: ${firstChoice.message?.content}")
            
            val content = firstChoice.message?.content
            if (content.isNullOrBlank()) {
                return@withContext "DEBUG: Empty content - Response: $response"
            }
            
            return@withContext content
        } catch (e: Exception) {
            // Log the error for debugging
            println("API Error occurred: ${e.javaClass.simpleName}")
            println("API Error message: ${e.message}")
            e.printStackTrace()
            
            val currentModel = AIModelManager.getCurrentModel()
            
            // Handle NeMoTron timeout with automatic fallback (only for NeMoTron)
            if (currentModel?.id == "nvidia-nemotron" && 
                (e.message?.contains("timeout", ignoreCase = true) == true || 
                 e.message?.contains("queue", ignoreCase = true) == true ||
                 e.message?.contains("busy", ignoreCase = true) == true)) {
                
                println("NeMoTron model is busy/queued, attempting fallback to GPT-OSS...")
                
                // Try GPT-OSS as fallback
                return@withContext tryFallbackModel(userMessage, conversationHistory, detailedThinking)
            }
            
            // For GPT-OSS, don't try fallback, just return error
            if (currentModel?.id == "gpt-oss") {
                println("GPT-OSS model failed, not attempting fallback")
            }
            
            // Return specific error message based on exception type
            val errorMessage = when {
                e.message?.contains("500", ignoreCase = true) == true -> 
                    "🔧 Server error (500). The AI model is temporarily unavailable. Please try again in a moment."
                e.message?.contains("timeout", ignoreCase = true) == true -> 
                    if (currentModel?.id == "gpt-oss") {
                        "⏱️ Request timed out. Please check your internet connection and try again."
                    } else {
                        "⏱️ ${currentModel?.name ?: "Model"} is experiencing high traffic. Try switching to GPT-OSS for faster responses, or wait a moment and try again."
                    }
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "🌐 Network error. Please check your connection and try again."
                e.message?.contains("401", ignoreCase = true) == true -> 
                    "🔑 API key error. Please check your API key in settings."
                e.message?.contains("429", ignoreCase = true) == true -> 
                    "🚦 Rate limit exceeded. Try switching to GPT-OSS or wait a moment and try again."
                else -> "❌ Sorry, I encountered an error: ${e.message}. Try switching models or try again."
            }
            
            return@withContext errorMessage
        }
    }
    
    private suspend fun tryFallbackModel(
        userMessage: String,
        conversationHistory: List<Message>,
        detailedThinking: Boolean
    ): String {
        try {
            // Switch to GPT-OSS temporarily
            val fallbackModel = AIModelManager.getModelConfig("gpt-oss")
            if (fallbackModel == null) {
                return "⚠️ NeMoTron is busy and no fallback model available. Please try again later."
            }
            
            val messages = mutableListOf<Message>()
            
            // Add system message for fallback
            val systemMessage = "You are a helpful AI assistant. Automatically switched to GPT-OSS due to high traffic on NeMoTron. Provide helpful responses without unnecessary introductions."
            messages.add(Message("system", systemMessage))
            
            // Add conversation history (limit to last 10 messages for performance)
            val limitedHistory = conversationHistory.takeLast(10)
            messages.addAll(limitedHistory)
            messages.add(Message("user", userMessage))
            
            val request = ApiRequest(
                messages = messages,
                model = fallbackModel.modelName,
                max_tokens = fallbackModel.maxTokens,
                temperature = if (detailedThinking) (fallbackModel.temperature * 1.5).toDouble() else fallbackModel.temperature.toDouble(),
                top_p = 1.0,
                frequency_penalty = 0,
                presence_penalty = 0,
                stream = false,
                reasoning_effort = "medium"
            )
            
            val response = apiService.sendMessage("Bearer ${fallbackModel.apiKey}", request = request)
            
            // Add null checks for fallback response
            if (response.choices.isNullOrEmpty()) {
                return "⚠️ Fallback model returned an empty response. Please try again later."
            }
            
            val content = response.choices.firstOrNull()?.message?.content
            
            return if (content != null) {
                "🔄 *Switched to GPT-OSS due to NeMoTron queue*\n\n$content"
            } else {
                "⚠️ Both models are experiencing issues. Please try again later."
            }
            
        } catch (e: Exception) {
            println("Fallback model also failed: ${e.message}")
            return "⚠️ All models are currently experiencing high traffic. Please try again in a few minutes.\n\n💡 Tip: GPT-OSS usually has shorter wait times!"
        }
    }
    
    private fun generateMockResponse(userMessage: String): String {
        val responses = listOf(
            "I understand you're asking about \"$userMessage\". Let me help you with that.",
            "That's an interesting question about \"$userMessage\". Here's what I think...",
            "Based on your message \"$userMessage\", I can provide some insights.",
            "Regarding \"$userMessage\", here's my response.",
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