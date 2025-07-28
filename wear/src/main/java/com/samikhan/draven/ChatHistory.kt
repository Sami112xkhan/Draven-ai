package com.samikhan.draven

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatHistory(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("chat_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveMessages(messages: List<Message>) {
        val json = gson.toJson(messages)
        prefs.edit().putString("messages", json).apply()
    }
    
    fun loadMessages(): List<Message> {
        val json = prefs.getString("messages", "[]")
        val type = object : TypeToken<List<Message>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun clearHistory() {
        prefs.edit().remove("messages").apply()
    }
    
    fun addMessage(message: Message) {
        val messages = loadMessages().toMutableList()
        messages.add(message)
        saveMessages(messages)
    }
} 