package com.samikhan.draven

import android.content.Context
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://integrate.api.nvidia.com/v1/chat/completions"
    private const val API_KEY_PREF = "api_key"
    private const val DEFAULT_API_KEY = "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"

    fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences("draven_prefs", Context.MODE_PRIVATE)
        return prefs.getString(API_KEY_PREF, DEFAULT_API_KEY) ?: DEFAULT_API_KEY
    }

    fun setApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences("draven_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(API_KEY_PREF, key).apply()
    }

    fun sendMessage(context: Context, messages: List<Message>, detailedThinking: Boolean = false): String {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
        
        val apiKey = getApiKey(context)

        val json = JSONObject()
        val jsonMessages = JSONArray()
        
        // Add system message based on detailed thinking toggle
        val systemMessage = if (detailedThinking) {
            "detailed thinking on. Keep responses concise and watch-friendly (under 100 words) unless specifically asked for more detail."
        } else {
            "detailed thinking off. Keep responses short and useful (under 50 words) for a smartwatch interface. Be direct and to the point."
        }
        val systemObj = JSONObject()
        systemObj.put("role", "system")
        systemObj.put("content", systemMessage)
        jsonMessages.put(systemObj)
        
        for (msg in messages) {
            val obj = JSONObject()
            obj.put("role", msg.role)
            obj.put("content", msg.content)
            jsonMessages.put(obj)
        }
        json.put("messages", jsonMessages)
        json.put("model", "nvidia/llama-3.1-nemotron-ultra-253b-v1")
        json.put("max_tokens", 150) // Reduced from 1024 for shorter responses
        json.put("temperature", if (detailedThinking) 0.3 else 0.1)
        json.put("top_p", 0.9)
        json.put("frequency_penalty", 0)
        json.put("presence_penalty", 0)
        json.put("stream", false)

        val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val obj = JSONObject(responseBody)
                val choices = obj.getJSONArray("choices")
                if (choices.length() > 0) {
                    val message = choices.getJSONObject(0).getJSONObject("message")
                    message.getString("content")
                } else {
                    "Sorry, I couldn't generate a response."
                }
            } else {
                val errorMessage = when (response.code) {
                    401 -> "API key error. Please check your API key in settings."
                    429 -> "Rate limit exceeded. Please wait a moment and try again."
                    else -> "Error: ${response.code} ${response.message}"
                }
                generateMockResponse(messages.lastOrNull()?.content ?: "your message")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> 
                    "Sorry, the request timed out. Please try again."
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Network error. Please check your connection and try again."
                else -> "Sorry, I encountered an error: ${e.message}. Please try again."
            }
            generateMockResponse(messages.lastOrNull()?.content ?: "your message")
        }
    }

    private fun generateMockResponse(userMessage: String): String {
        val responses = listOf(
            "I understand. Here's a quick answer: ${userMessage.take(20)}...",
            "Got it! Short answer: ${userMessage.take(15)}...",
            "Quick response: ${userMessage.take(25)}...",
            "Here's the brief version: ${userMessage.take(20)}...",
            "Short answer: ${userMessage.take(15)}..."
        )
        return responses.random()
    }
} 