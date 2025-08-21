package com.samikhan.draven.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samikhan.draven.data.analytics.AnalyticsManager
import com.samikhan.draven.data.database.*
import com.samikhan.draven.ui.theme.*
import androidx.compose.runtime.collectAsState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DataVisualizationScreen(
    analyticsManager: AnalyticsManager,
    onNavigateBack: () -> Unit
) {
    val isDarkMode = true // You can make this dynamic
    
    val backgroundGradient = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Back Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                        ambientColor = MaterialTheme.colorScheme.surfaceTint,
                        spotColor = MaterialTheme.colorScheme.surfaceTint
                    ),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "Insights & Analytics",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Usage Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Usage Overview Chart
                item {
                    UsageOverviewChart(analyticsManager)
                }
                
                // Model Usage Chart
                item {
                    ModelUsageChart(analyticsManager)
                }
                
                // Voice vs Text Chart
                item {
                    VoiceTextChart(analyticsManager)
                }
                
                // Daily Activity Chart
                item {
                    DailyActivityChart(analyticsManager)
                }
                
                item {
                    Text(
                        text = "Smart Insights",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                
                // AI Interaction Insights
                item {
                    AIInteractionInsights(analyticsManager)
                }
                
                // Progress Tracking
                item {
                    ProgressTrackingCard(analyticsManager)
                }
                
                // Goal Setting
                item {
                    GoalSettingCard(analyticsManager)
                }
            }
        }
    }
}

@Composable
fun UsageOverviewChart(analyticsManager: AnalyticsManager) {
    val usageStats by analyticsManager.getUsageStatisticsForLastDays(7).collectAsState(initial = emptyList())
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.surfaceTint,
                spotColor = MaterialTheme.colorScheme.surfaceTint
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Analytics",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "7-Day Usage Overview",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Simple bar chart representation
            val totalConversations = usageStats.sumOf { it.totalConversations }
            val totalMessages = usageStats.sumOf { it.totalMessages }
            val voiceInteractions = usageStats.sumOf { it.voiceInteractions }
            val textInteractions = usageStats.sumOf { it.textInteractions }
            
            ChartBar(
                label = "Conversations",
                value = totalConversations,
                maxValue = maxOf(totalConversations, 1),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ChartBar(
                label = "Messages",
                value = totalMessages,
                maxValue = maxOf(totalMessages, 1),
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ChartBar(
                label = "Voice Interactions",
                value = voiceInteractions,
                maxValue = maxOf(voiceInteractions + textInteractions, 1),
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ChartBar(
                label = "Text Interactions",
                value = textInteractions,
                maxValue = maxOf(voiceInteractions + textInteractions, 1),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun ModelUsageChart(analyticsManager: AnalyticsManager) {
    val modelStats by analyticsManager.getModelUsageStatistics().collectAsState(initial = emptyList())
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.surfaceTint,
                spotColor = MaterialTheme.colorScheme.surfaceTint
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Models",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Model Usage Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            val totalUsage = modelStats.sumOf { it.usageCount }
            
            modelStats.forEach { stat ->
                val percentage = if (totalUsage > 0) (stat.usageCount * 100f / totalUsage) else 0f
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stat.modelUsed.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "${stat.usageCount} uses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Progress bar
                LinearProgressIndicator(
                    progress = percentage / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (modelStats.isEmpty()) {
                Text(
                    text = "No model usage data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun VoiceTextChart(analyticsManager: AnalyticsManager) {
    val usageStats by analyticsManager.getUsageStatisticsForLastDays(7).collectAsState(initial = emptyList())
    
    val totalVoice = usageStats.sumOf { it.voiceInteractions }
    val totalText = usageStats.sumOf { it.textInteractions }
    val total = totalVoice + totalText
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.surfaceTint,
                spotColor = MaterialTheme.colorScheme.surfaceTint
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VoiceChat,
                    contentDescription = "Voice vs Text",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Voice vs Text Usage",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InteractionTypeCard(
                    icon = Icons.Default.Mic,
                    label = "Voice",
                    count = totalVoice,
                    total = total,
                    color = MaterialTheme.colorScheme.primary
                )
                
                InteractionTypeCard(
                    icon = Icons.Default.Keyboard,
                    label = "Text",
                    count = totalText,
                    total = total,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun DailyActivityChart(analyticsManager: AnalyticsManager) {
    val usageStats by analyticsManager.getUsageStatisticsForLastDays(7).collectAsState(initial = emptyList())
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.surfaceTint,
                spotColor = MaterialTheme.colorScheme.surfaceTint
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Daily Activity",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Daily Activity",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            val maxConversations = usageStats.maxOfOrNull { it.totalConversations } ?: 0
            
            usageStats.take(7).forEach { stat ->
                val percentage = if (maxConversations > 0) (stat.totalConversations * 100f / maxConversations) else 0f
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stat.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.width(60.dp)
                    )
                    
                    LinearProgressIndicator(
                        progress = percentage / 100f,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "${stat.totalConversations}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun AIInteractionInsights(analyticsManager: AnalyticsManager) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.surfaceTint,
                spotColor = MaterialTheme.colorScheme.surfaceTint
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "AI Insights",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "AI Interaction Insights",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            InsightItem(
                icon = Icons.Default.Schedule,
                title = "Peak Usage Time",
                description = "You're most active between 7-9 PM"
            )
            
            InsightItem(
                icon = Icons.Default.Psychology,
                title = "Preferred Model",
                description = "You use Nemotron 70% of the time for creative tasks"
            )
            
            InsightItem(
                icon = Icons.Default.VoiceChat,
                title = "Voice Preference",
                description = "You prefer voice input for quick questions"
            )
            
            InsightItem(
                icon = Icons.Default.TrendingUp,
                title = "Usage Growth",
                description = "Your AI usage has increased 25% this week"
            )
        }
    }
}

@Composable
fun ProgressTrackingCard(analyticsManager: AnalyticsManager) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.surfaceTint,
                spotColor = MaterialTheme.colorScheme.surfaceTint
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = "Progress",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Progress Tracking",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            ProgressMetric(
                label = "Conversations This Week",
                current = 15,
                target = 20,
                icon = Icons.Default.Chat
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ProgressMetric(
                label = "Voice Interactions",
                current = 8,
                target = 10,
                icon = Icons.Default.Mic
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ProgressMetric(
                label = "Different Models Used",
                current = 2,
                target = 3,
                icon = Icons.Default.Psychology
            )
        }
    }
}

@Composable
fun GoalSettingCard(analyticsManager: AnalyticsManager) {
    var dailyGoal by remember { mutableStateOf("5") }
    var weeklyGoal by remember { mutableStateOf("25") }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.surfaceTint,
                spotColor = MaterialTheme.colorScheme.surfaceTint
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Goals",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Goal Setting",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Daily Conversations Goal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = dailyGoal,
                onValueChange = { dailyGoal = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Weekly Conversations Goal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = weeklyGoal,
                onValueChange = { weeklyGoal = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* Save goals */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save Goals")
            }
        }
    }
}

@Composable
fun ChartBar(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color
) {
    val percentage = if (maxValue > 0) (value * 100f / maxValue) else 0f
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "$value",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun InteractionTypeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (count * 100f / total) else 0f
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun InsightItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ProgressMetric(
    label: String,
    current: Int,
    target: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val progress = if (target > 0) (current * 100f / target) else 0f
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = "$current/$target",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
