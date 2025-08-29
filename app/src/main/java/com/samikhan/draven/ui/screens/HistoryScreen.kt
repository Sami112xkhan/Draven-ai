package com.samikhan.draven.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.samikhan.draven.data.model.Conversation
import com.samikhan.draven.data.preferences.ThemePreferences
import com.samikhan.draven.ui.theme.*
import com.samikhan.draven.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onConversationSelected: (String) -> Unit,
    themePreferences: ThemePreferences,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(LocalContext.current))
) {
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = true)
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkMode) SurfaceGradient else LightSurfaceGradient
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Enhanced Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = getGlassBackground(isDarkMode),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                border = BorderStroke(1.dp, getGlassBorder(isDarkMode))
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Chat History",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }

            // Conversations List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.conversations.isEmpty()) {
                    item {
                        EmptyHistoryState()
                    }
                } else {
                    items(
                        items = uiState.conversations,
                        key = { it.id }
                    ) { conversation ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                animationSpec = tween(400, easing = EaseOutCubic),
                                initialOffsetY = { (it * 0.5).toInt() }
                            ) + fadeIn(animationSpec = tween(400))
                        ) {
                            ConversationCard(
                                conversation = conversation,
                                isSelected = conversation.id == uiState.selectedConversationId,
                                onConversationClick = {
                                    viewModel.selectConversation(conversation.id)
                                    onConversationSelected(conversation.id)
                                },
                                onDeleteClick = {
                                    viewModel.deleteConversation(conversation)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationCard(
    conversation: Conversation,
    isSelected: Boolean,
    onConversationClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    Surface(
        onClick = onConversationClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) DravenNeon.copy(alpha = 0.1f) else getGlassBackground(true),
        border = BorderStroke(
            1.dp,
            if (isSelected) DravenNeon else getGlassBorder(true)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Conversation Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = getSurfaceColor(true)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Conversation Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = conversation.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = conversation.lastMessage,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = dateFormat.format(Date(conversation.timestamp)),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Delete Button
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ErrorRed.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = getGlassBackground(true),
            border = BorderStroke(1.dp, getGlassBorder(true))
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.padding(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Chat History",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start a conversation with Draven to see your chat history here",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
} 