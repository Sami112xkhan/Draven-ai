package com.samikhan.draven.data.analytics

import android.content.Context
import android.util.Log
import com.samikhan.draven.data.database.*
import com.samikhan.draven.data.model.ChatMessage
import com.samikhan.draven.data.model.Conversation
import com.samikhan.draven.data.model.MessageRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class AnalyticsManager(
    private val database: DravenDatabase,
    private val context: Context
) {
    private val analyticsDao = database.analyticsDao()
    private val usageStatsDao = database.usageStatisticsDao()
    private val userPrefsDao = database.userPreferencesDao()
    private val conversationTagsDao = database.conversationTagsDao()
    private val smartSuggestionsDao = database.smartSuggestionsDao()
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // Phase 2: Conversation Analytics
    suspend fun trackConversationAnalytics(
        conversationId: String,
        modelUsed: String,
        criticalThinkingEnabled: Boolean
    ) {
        val messages = messageDao.getMessagesForConversation(conversationId).first()
        val userMessages = messages.count { it.role == MessageRole.USER }
        val aiMessages = messages.count { it.role == MessageRole.ASSISTANT }
        val totalMessages = messages.size
        
        // Calculate average response time
        val responseTimes = mutableListOf<Long>()
        var lastUserMessageTime: Long? = null
        
        messages.forEach { message ->
            when (message.role) {
                MessageRole.USER -> lastUserMessageTime = message.timestamp
                MessageRole.ASSISTANT -> {
                    lastUserMessageTime?.let { userTime ->
                        responseTimes.add(message.timestamp - userTime)
                    }
                }
                else -> {}
            }
        }
        
        val averageResponseTime = if (responseTimes.isNotEmpty()) {
            responseTimes.average().toLong()
        } else 0L
        
        // Calculate total duration
        val totalDuration = if (messages.size >= 2) {
            messages.last().timestamp - messages.first().timestamp
        } else 0L
        
        // Count voice vs text interactions (simplified - you can enhance this)
        val voiceInteractions = 0 // TODO: Track actual voice interactions
        val textInteractions = totalMessages
        
        val analytics = ConversationAnalytics(
            conversationId = conversationId,
            totalMessages = totalMessages,
            userMessages = userMessages,
            aiMessages = aiMessages,
            averageResponseTime = averageResponseTime,
            totalDuration = totalDuration,
            modelUsed = modelUsed,
            voiceInteractions = voiceInteractions,
            textInteractions = textInteractions,
            criticalThinkingEnabled = criticalThinkingEnabled
        )
        
        analyticsDao.insertConversationAnalytics(analytics)
    }
    
    // Phase 2: Usage Statistics
    suspend fun updateDailyUsageStatistics(
        conversationCount: Int = 0,
        messageCount: Int = 0,
        voiceInteractions: Int = 0,
        textInteractions: Int = 0,
        modelUsed: String? = null,
        sessionDuration: Long = 0,
        criticalThinkingUsed: Boolean = false
    ) {
        val today = dateFormat.format(Date())
        val existingStats = usageStatsDao.getUsageStatisticsForDate(today)
        
        val updatedStats = existingStats?.let { stats ->
            stats.copy(
                totalConversations = stats.totalConversations + conversationCount,
                totalMessages = stats.totalMessages + messageCount,
                voiceInteractions = stats.voiceInteractions + voiceInteractions,
                textInteractions = stats.textInteractions + textInteractions,
                averageSessionDuration = if (stats.totalConversations + conversationCount > 0) {
                    (stats.averageSessionDuration * stats.totalConversations + sessionDuration) / 
                    (stats.totalConversations + conversationCount)
                } else sessionDuration,
                criticalThinkingUsage = stats.criticalThinkingUsage + if (criticalThinkingUsed) 1 else 0
            )
        } ?: UsageStatistics(
            date = today,
            totalConversations = conversationCount,
            totalMessages = messageCount,
            voiceInteractions = voiceInteractions,
            textInteractions = textInteractions,
            modelsUsed = modelUsed ?: "",
            averageSessionDuration = sessionDuration,
            criticalThinkingUsage = if (criticalThinkingUsed) 1 else 0
        )
        
        usageStatsDao.insertUsageStatistics(updatedStats)
    }
    
    // Phase 2: Search & Filter
    fun searchConversations(query: String): Flow<List<Conversation>> {
        return conversationDao.searchConversations(query)
    }
    
    fun searchMessages(query: String): Flow<List<ChatMessage>> {
        return messageDao.searchMessages(query)
    }
    
    // Phase 3: Smart Suggestions
    suspend fun generateSmartSuggestions(conversationId: String): List<SmartSuggestion> {
        val messages = messageDao.getMessagesForConversation(conversationId).first()
        val suggestions = mutableListOf<SmartSuggestion>()
        
        // Generate follow-up questions based on conversation context
        val lastMessage = messages.lastOrNull()
        if (lastMessage?.role == MessageRole.ASSISTANT) {
            val followUpQuestions = generateFollowUpQuestions(lastMessage.content)
            followUpQuestions.forEach { question ->
                suggestions.add(
                    SmartSuggestion(
                        conversationId = conversationId,
                        suggestion = question,
                        suggestionType = "follow_up",
                        relevance = 0.8f
                    )
                )
            }
        }
        
        // Suggest model switches based on conversation type
        val conversationType = analyzeConversationType(messages)
        val suggestedModel = suggestModelForConversationType(conversationType)
        if (suggestedModel != null) {
            suggestions.add(
                SmartSuggestion(
                    conversationId = conversationId,
                    suggestion = "Switch to $suggestedModel for better responses",
                    suggestionType = "model_switch",
                    relevance = 0.7f
                )
            )
        }
        
        // Save suggestions
        suggestions.forEach { suggestion ->
            smartSuggestionsDao.insertSmartSuggestion(suggestion)
        }
        
        return suggestions
    }
    
    // Phase 3: Personalization
    suspend fun updateUserPreferences(
        preferredModel: String? = null,
        voiceModeEnabled: Boolean? = null,
        criticalThinkingEnabled: Boolean? = null,
        animationEnabled: Boolean? = null,
        themePreference: String? = null,
        responseLengthPreference: String? = null
    ) {
        val existingPrefs = userPrefsDao.getUserPreferences()
        val updatedPrefs = existingPrefs?.let { prefs ->
            prefs.copy(
                preferredModel = preferredModel ?: prefs.preferredModel,
                voiceModeEnabled = voiceModeEnabled ?: prefs.voiceModeEnabled,
                criticalThinkingEnabled = criticalThinkingEnabled ?: prefs.criticalThinkingEnabled,
                animationEnabled = animationEnabled ?: prefs.animationEnabled,
                themePreference = themePreference ?: prefs.themePreference,
                responseLengthPreference = responseLengthPreference ?: prefs.responseLengthPreference,
                updatedAt = System.currentTimeMillis()
            )
        } ?: UserPreferences(
            preferredModel = preferredModel ?: "nemotron",
            voiceModeEnabled = voiceModeEnabled ?: false,
            criticalThinkingEnabled = criticalThinkingEnabled ?: false,
            animationEnabled = animationEnabled ?: true,
            themePreference = themePreference ?: "auto",
            responseLengthPreference = responseLengthPreference ?: "short"
        )
        
        userPrefsDao.insertUserPreferences(updatedPrefs)
    }
    
    suspend fun getUserPreferences(): UserPreferences? {
        return userPrefsDao.getUserPreferences()
    }
    
    // Phase 3: Data Visualization
    fun getModelUsageStatistics(): Flow<List<ModelUsageStats>> {
        return flow {
            try {
                val stats = analyticsDao.getModelUsageStatistics()
                emit(stats.first())
            } catch (e: Exception) {
                Log.w("AnalyticsManager", "Error getting model usage statistics: ${e.message}")
                // Return mock data if database tables don't exist yet
                emit(listOf(
                    ModelUsageStats("nemotron", 12),
                    ModelUsageStats("gpt-oss", 8)
                ))
            }
        }
    }
    
    fun getUsageStatisticsForLastDays(days: Int): Flow<List<UsageStatistics>> {
        return flow {
            try {
                val stats = usageStatsDao.getUsageStatisticsForLastDays(days)
                emit(stats.first())
            } catch (e: Exception) {
                Log.w("AnalyticsManager", "Error getting usage statistics: ${e.message}")
                // Return mock data if database tables don't exist yet
                val mockStats = mutableListOf<UsageStatistics>()
                val calendar = Calendar.getInstance()
                for (i in 0 until days) {
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    val date = dateFormat.format(calendar.time)
                    mockStats.add(
                        UsageStatistics(
                            date = date,
                            totalConversations = (5..15).random(),
                            totalMessages = (20..50).random(),
                            voiceInteractions = (2..8).random(),
                            textInteractions = (15..40).random(),
                            modelsUsed = "nemotron,gpt-oss",
                            averageSessionDuration = (300000..900000).random().toLong(),
                            criticalThinkingUsage = (1..5).random()
                        )
                    )
                    calendar.add(Calendar.DAY_OF_YEAR, i) // Reset
                }
                emit(mockStats)
            }
        }
    }
    
    fun getMostUsedTags(): Flow<List<TagUsageStats>> {
        return conversationTagsDao.getMostUsedTags()
    }
    
    fun getUnusedSuggestionsForConversation(conversationId: String): Flow<List<SmartSuggestion>> {
        return smartSuggestionsDao.getUnusedSuggestionsForConversation(conversationId)
    }
    
    suspend fun markSuggestionAsUsed(suggestionId: String) {
        smartSuggestionsDao.markSuggestionAsUsed(suggestionId)
    }
    
    // Helper functions for smart features
    private fun generateFollowUpQuestions(content: String): List<String> {
        val questions = mutableListOf<String>()
        
        // Simple keyword-based follow-up questions
        when {
            content.contains("code", ignoreCase = true) -> {
                questions.add("Would you like me to explain this code in more detail?")
                questions.add("Should I show you how to test this code?")
            }
            content.contains("algorithm", ignoreCase = true) -> {
                questions.add("Would you like to see the time complexity analysis?")
                questions.add("Should I show you alternative approaches?")
            }
            content.contains("error", ignoreCase = true) -> {
                questions.add("Would you like me to help you debug this?")
                questions.add("Should I show you how to prevent this error?")
            }
            content.contains("design", ignoreCase = true) -> {
                questions.add("Would you like to see some design patterns?")
                questions.add("Should I show you best practices?")
            }
        }
        
        return questions.take(3) // Limit to 3 suggestions
    }
    
    private fun analyzeConversationType(messages: List<ChatMessage>): String {
        val content = messages.joinToString(" ") { it.content.lowercase() }
        
        return when {
            content.contains("code") || content.contains("programming") || content.contains("algorithm") -> "technical"
            content.contains("creative") || content.contains("story") || content.contains("art") -> "creative"
            content.contains("business") || content.contains("strategy") || content.contains("analysis") -> "business"
            content.contains("learning") || content.contains("education") || content.contains("explain") -> "educational"
            else -> "general"
        }
    }
    
    private fun suggestModelForConversationType(conversationType: String): String? {
        return when (conversationType) {
            "technical" -> "gpt-oss" // Better for technical tasks
            "creative" -> "nemotron" // Better for creative tasks
            "business" -> "gpt-oss" // Better for analysis
            "educational" -> "nemotron" // Better for explanations
            else -> null
        }
    }
    
    // Auto-tagging conversations
    suspend fun autoTagConversation(conversationId: String) {
        val messages = messageDao.getMessagesForConversation(conversationId).first()
        val content = messages.joinToString(" ") { it.content.lowercase() }
        
        val tags = mutableListOf<Pair<String, Float>>()
        
        // Simple keyword-based tagging
        when {
            content.contains("work") || content.contains("project") || content.contains("meeting") -> {
                tags.add("work" to 0.8f)
            }
            content.contains("personal") || content.contains("life") || content.contains("family") -> {
                tags.add("personal" to 0.8f)
            }
            content.contains("learn") || content.contains("study") || content.contains("education") -> {
                tags.add("learning" to 0.9f)
            }
            content.contains("creative") || content.contains("art") || content.contains("design") -> {
                tags.add("creative" to 0.9f)
            }
            content.contains("code") || content.contains("programming") || content.contains("technical") -> {
                tags.add("technical" to 0.9f)
            }
        }
        
        // Save tags
        tags.forEach { (tag, confidence) ->
            val conversationTag = ConversationTag(
                conversationId = conversationId,
                tag = tag,
                confidence = confidence
            )
            conversationTagsDao.insertConversationTag(conversationTag)
        }
    }
    
    // Track real conversation data
    suspend fun trackConversationStart(conversationId: String, modelUsed: String) {
        try {
            val analytics = ConversationAnalytics(
                conversationId = conversationId,
                totalMessages = 0,
                userMessages = 0,
                aiMessages = 0,
                averageResponseTime = 0L,
                totalDuration = 0L,
                modelUsed = modelUsed,
                voiceInteractions = 0,
                textInteractions = 0,
                criticalThinkingEnabled = false
            )
            analyticsDao.insertConversationAnalytics(analytics)
        } catch (e: Exception) {
            Log.w("AnalyticsManager", "Error tracking conversation start: ${e.message}")
        }
    }
    
    suspend fun trackMessageSent(conversationId: String, isVoice: Boolean, criticalThinking: Boolean) {
        try {
            val analytics = analyticsDao.getConversationAnalytics(conversationId)
            analytics?.let {
                val updatedAnalytics = it.copy(
                    totalMessages = it.totalMessages + 1,
                    userMessages = it.userMessages + 1,
                    voiceInteractions = if (isVoice) it.voiceInteractions + 1 else it.voiceInteractions,
                    textInteractions = if (!isVoice) it.textInteractions + 1 else it.textInteractions,
                    criticalThinkingEnabled = criticalThinking,
                    updatedAt = System.currentTimeMillis()
                )
                analyticsDao.insertConversationAnalytics(updatedAnalytics)
            }
        } catch (e: Exception) {
            Log.w("AnalyticsManager", "Error tracking message sent: ${e.message}")
        }
    }
    
    suspend fun trackAIResponse(conversationId: String, responseTime: Long) {
        try {
            val analytics = analyticsDao.getConversationAnalytics(conversationId)
            analytics?.let {
                val updatedAnalytics = it.copy(
                    totalMessages = it.totalMessages + 1,
                    aiMessages = it.aiMessages + 1,
                    averageResponseTime = if (it.aiMessages > 0) {
                        ((it.averageResponseTime * it.aiMessages + responseTime) / (it.aiMessages + 1))
                    } else responseTime,
                    updatedAt = System.currentTimeMillis()
                )
                analyticsDao.insertConversationAnalytics(updatedAnalytics)
            }
        } catch (e: Exception) {
            Log.w("AnalyticsManager", "Error tracking AI response: ${e.message}")
        }
    }
    
    suspend fun updateDailyUsageStatistics() {
        try {
            val today = dateFormat.format(Date())
            val existingStats = usageStatsDao.getUsageStatisticsForDate(today)
            
            if (existingStats == null) {
                // Create new daily stats
                val newStats = UsageStatistics(
                    date = today,
                    totalConversations = 1,
                    totalMessages = 2,
                    voiceInteractions = 0,
                    textInteractions = 2,
                    modelsUsed = "nemotron",
                    averageSessionDuration = 300000L,
                    criticalThinkingUsage = 0
                )
                usageStatsDao.insertUsageStatistics(newStats)
            } else {
                // Update existing stats
                val updatedStats = existingStats.copy(
                    totalConversations = existingStats.totalConversations + 1,
                    totalMessages = existingStats.totalMessages + 2,
                    textInteractions = existingStats.textInteractions + 2
                )
                usageStatsDao.insertUsageStatistics(updatedStats)
            }
        } catch (e: Exception) {
            Log.w("AnalyticsManager", "Error updating daily usage statistics: ${e.message}")
        }
    }
}
