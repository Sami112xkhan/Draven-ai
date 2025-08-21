package com.samikhan.draven.data.database

import androidx.room.*
import com.samikhan.draven.data.model.ChatMessage
import com.samikhan.draven.data.model.Conversation
import kotlinx.coroutines.flow.Flow
import java.util.*

// Enhanced entities for analytics
@Entity(tableName = "conversation_analytics")
data class ConversationAnalytics(
    @PrimaryKey val conversationId: String,
    val totalMessages: Int,
    val userMessages: Int,
    val aiMessages: Int,
    val averageResponseTime: Long, // in milliseconds
    val totalDuration: Long, // in milliseconds
    val modelUsed: String,
    val voiceInteractions: Int,
    val textInteractions: Int,
    val criticalThinkingEnabled: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "usage_statistics")
data class UsageStatistics(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: String, // YYYY-MM-DD format
    val totalConversations: Int,
    val totalMessages: Int,
    val voiceInteractions: Int,
    val textInteractions: Int,
    val modelsUsed: String, // JSON array of model IDs
    val averageSessionDuration: Long,
    val criticalThinkingUsage: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey val id: String = "default",
    val preferredModel: String,
    val voiceModeEnabled: Boolean,
    val criticalThinkingEnabled: Boolean,
    val animationEnabled: Boolean,
    val themePreference: String, // "light", "dark", "auto"
    val responseLengthPreference: String, // "short", "detailed"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversation_tags")
data class ConversationTag(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val tag: String, // e.g., "work", "personal", "learning", "creative"
    val confidence: Float, // 0.0 to 1.0
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "smart_suggestions")
data class SmartSuggestion(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val suggestion: String,
    val suggestionType: String, // "follow_up", "topic", "model_switch"
    val relevance: Float, // 0.0 to 1.0
    val used: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<Conversation>>
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): Conversation?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)
    
    @Delete
    suspend fun deleteConversation(conversation: Conversation)
    
    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: String)
    
    // Analytics queries
    @Query("SELECT COUNT(*) FROM conversations WHERE DATE(timestamp/1000, 'unixepoch') = :date")
    suspend fun getConversationCountForDate(date: String): Int
    
    @Query("SELECT * FROM conversations WHERE title LIKE '%' || :query || '%' OR id IN (SELECT conversationId FROM chat_messages WHERE content LIKE '%' || :query || '%')")
    fun searchConversations(query: String): Flow<List<Conversation>>
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>)
    
    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)
    
    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId AND isLoading = 1")
    suspend fun deleteLoadingMessagesForConversation(conversationId: String)
    
    // Analytics queries
    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun getMessageCountForConversation(conversationId: String): Int
    
    @Query("SELECT * FROM chat_messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<ChatMessage>>
}

@Dao
interface AnalyticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversationAnalytics(analytics: ConversationAnalytics)
    
    @Query("SELECT * FROM conversation_analytics WHERE conversationId = :conversationId")
    suspend fun getConversationAnalytics(conversationId: String): ConversationAnalytics?
    
    @Query("SELECT * FROM conversation_analytics ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentAnalytics(limit: Int): Flow<List<ConversationAnalytics>>
    
    @Query("SELECT SUM(totalMessages) FROM conversation_analytics WHERE DATE(createdAt/1000, 'unixepoch') = :date")
    suspend fun getTotalMessagesForDate(date: String): Int?
    
    @Query("SELECT modelUsed, COUNT(*) as usage_count FROM conversation_analytics GROUP BY modelUsed ORDER BY usage_count DESC")
    fun getModelUsageStatistics(): Flow<List<ModelUsageStats>>
}

@Dao
interface UsageStatisticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageStatistics(stats: UsageStatistics)
    
    @Query("SELECT * FROM usage_statistics WHERE date = :date")
    suspend fun getUsageStatisticsForDate(date: String): UsageStatistics?
    
    @Query("SELECT * FROM usage_statistics ORDER BY date DESC LIMIT :days")
    fun getUsageStatisticsForLastDays(days: Int): Flow<List<UsageStatistics>>
    
    @Query("SELECT SUM(totalConversations) FROM usage_statistics WHERE date >= :startDate")
    suspend fun getTotalConversationsSince(startDate: String): Int?
}

@Dao
interface UserPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferences)
    
    @Query("SELECT * FROM user_preferences WHERE id = :id")
    suspend fun getUserPreferences(id: String = "default"): UserPreferences?
    
    @Query("UPDATE user_preferences SET preferredModel = :model, updatedAt = :timestamp WHERE id = :id")
    suspend fun updatePreferredModel(model: String, id: String = "default", timestamp: Long = System.currentTimeMillis())
}

@Dao
interface ConversationTagsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversationTag(tag: ConversationTag)
    
    @Query("SELECT * FROM conversation_tags WHERE conversationId = :conversationId")
    fun getTagsForConversation(conversationId: String): Flow<List<ConversationTag>>
    
    @Query("SELECT tag, COUNT(*) as count FROM conversation_tags GROUP BY tag ORDER BY count DESC")
    fun getMostUsedTags(): Flow<List<TagUsageStats>>
}

@Dao
interface SmartSuggestionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartSuggestion(suggestion: SmartSuggestion)
    
    @Query("SELECT * FROM smart_suggestions WHERE conversationId = :conversationId AND used = 0 ORDER BY relevance DESC LIMIT :limit")
    fun getUnusedSuggestionsForConversation(conversationId: String, limit: Int = 5): Flow<List<SmartSuggestion>>
    
    @Query("UPDATE smart_suggestions SET used = 1 WHERE id = :suggestionId")
    suspend fun markSuggestionAsUsed(suggestionId: String)
}

// Data classes for query results
data class ModelUsageStats(
    val modelUsed: String,
    @androidx.room.ColumnInfo(name = "usage_count") val usageCount: Int
)

data class TagUsageStats(
    val tag: String,
    val count: Int
)

@Database(
    entities = [
        Conversation::class, 
        ChatMessage::class, 
        ConversationAnalytics::class,
        UsageStatistics::class,
        UserPreferences::class,
        ConversationTag::class,
        SmartSuggestion::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DravenDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun usageStatisticsDao(): UsageStatisticsDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun conversationTagsDao(): ConversationTagsDao
    abstract fun smartSuggestionsDao(): SmartSuggestionsDao
    
    companion object {
        @Volatile
        private var INSTANCE: DravenDatabase? = null
        
        fun getDatabase(context: android.content.Context): DravenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    DravenDatabase::class.java,
                    "draven_database"
                )
                .fallbackToDestructiveMigration() // This will recreate the database if migration fails
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 