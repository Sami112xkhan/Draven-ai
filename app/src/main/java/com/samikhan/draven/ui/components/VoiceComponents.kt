package com.samikhan.draven.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samikhan.draven.ui.theme.*

@Composable
fun AnimatedMicrophoneButton(
    isListening: Boolean,
    confidence: Float,
    onToggleListening: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isListening) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_glow"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer pulse ring
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DravenNeon.copy(alpha = 0.3f * glowAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        // Main microphone button
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isListening) {
                            listOf(
                                DravenNeon.copy(alpha = 0.8f),
                                DravenNeon.copy(alpha = 0.6f)
                            )
                        } else {
                            listOf(
                                getGlassBackground(isDarkMode),
                                getGlassBorder(isDarkMode)
                            )
                        }
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = if (isListening) {
                            listOf(DravenNeon, DravenNeon.copy(alpha = 0.7f))
                        } else {
                            listOf(getGlassBorder(isDarkMode), getGlassBorder(isDarkMode).copy(alpha = 0.5f))
                        }
                    ),
                    shape = CircleShape
                )
                .clickable { onToggleListening() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop listening" else "Start listening",
                tint = if (isListening) Color.White else getOnSurfaceColor(isDarkMode),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun VoiceWaveform(
    confidence: Float,
    isListening: Boolean,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val bars = 5
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(bars) { index ->
            val delay = index * 100
            val height by infiniteTransition.animateFloat(
                initialValue = 4f,
                targetValue = if (isListening) (confidence * 20f + 4f) else 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500 + delay, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "waveform_bar_$index"
            )
            
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .background(
                        color = if (isListening) DravenNeon else getGlassBorder(isDarkMode),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun VoiceTranscriptionDisplay(
    transcribedText: String,
    isListening: Boolean,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    if (transcribedText.isNotEmpty() || isListening) {
        Surface(
            modifier = modifier,
            color = getGlassBackground(isDarkMode),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, getGlassBorder(isDarkMode))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isListening) {
                    VoiceWaveform(
                        confidence = 0.5f,
                        isListening = isListening,
                        isDarkMode = isDarkMode
                    )
                }
                
                Text(
                    text = if (transcribedText.isNotEmpty()) transcribedText else "Listening...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = getOnSurfaceColor(isDarkMode),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun VoiceControls(
    isSpeaking: Boolean,
    onStopSpeaking: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    if (isSpeaking) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Speaking",
                tint = DravenNeon,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = "Draven is speaking...",
                style = MaterialTheme.typography.bodySmall,
                color = getOnSurfaceColor(isDarkMode)
            )
            
            IconButton(
                onClick = onStopSpeaking,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop speaking",
                    tint = getOnSurfaceColor(isDarkMode),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun VoiceErrorDisplay(
    error: String?,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    if (error != null) {
        Surface(
            modifier = modifier,
            color = Color(0xFFFF4444).copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFFF4444).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color(0xFFFF4444),
                    modifier = Modifier.size(16.dp)
                )
                
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF4444)
                )
            }
        }
    }
} 