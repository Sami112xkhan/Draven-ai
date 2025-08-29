package com.samikhan.draven.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samikhan.draven.data.model.ChatMessage
import com.samikhan.draven.data.model.MessageRole
import com.samikhan.draven.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun EnhancedMessageBubble(
    message: ChatMessage,
    onTextUpdate: () -> Unit = {},
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.3f),
    isAnimationEnabled: Boolean = true
) {
    val isUser = message.role == MessageRole.USER
    val isSystem = message.role == MessageRole.SYSTEM
    val isDraven = message.role == MessageRole.ASSISTANT
    val alignment = if (isUser) Alignment.End else Alignment.Start
    
    // Animation states
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(150),
        label = "alpha"
    )
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        when {
            isUser -> {
                // Enhanced User Message Bubble
                AnimatedVisibility(
                    visible = true,
                    enter = if (isAnimationEnabled) {
                        slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetX = { (it * 0.3).toInt() }
                        ) + fadeIn(
                            animationSpec = tween(400)
                        ) + scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialScale = 0.8f
                        )
                    } else {
                        EnterTransition.None
                    },
                    exit = if (isAnimationEnabled) {
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { (it * 0.3).toInt() }
                        ) + fadeOut(animationSpec = tween(300))
                    } else {
                        ExitTransition.None
                    }
                ) {
                    val clipboardManager = LocalClipboardManager.current
                    
                    Surface(
                        modifier = Modifier
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                alpha = alpha
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPressed = true
                                        tryAwaitRelease()
                                        isPressed = false
                                    }
                                )
                            }
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = UserBubbleGlow,
                                spotColor = UserBubbleGlow
                            )
                            .clip(RoundedCornerShape(28.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Box(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Column {
                                Text(
                                    text = message.content,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 24.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                // Copy button for user messages
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    var showCopiedFeedback by remember { mutableStateOf(false) }
                                    
                                    Surface(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(message.content))
                                            showCopiedFeedback = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(16.dp)
                                        )
                                    }
                                    
                                    // Reset copied feedback after delay
                                    LaunchedEffect(showCopiedFeedback) {
                                        if (showCopiedFeedback) {
                                            delay(2000)
                                            showCopiedFeedback = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            isSystem -> {
                // Enhanced System Message Bubble
                AnimatedVisibility(
                    visible = true,
                    enter = slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        initialOffsetX = { (-it * 0.3).toInt() }
                    ) + fadeIn(
                        animationSpec = tween(400)
                    ) + scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        initialScale = 0.8f
                    ),
                    exit = slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { (-it * 0.3).toInt() }
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                alpha = alpha
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPressed = true
                                        tryAwaitRelease()
                                        isPressed = false
                                    }
                                )
                            }
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = SystemBubbleGlow,
                                spotColor = SystemBubbleGlow
                            )
                            .clip(RoundedCornerShape(28.dp)),
                        color = SystemBubbleColor,
                        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = message.content,
                                color = SystemBubbleText,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
            isDraven -> {
                // Enhanced Draven Message Bubble with Glassmorphism
                if (message.isLoading) {
                    AnimatedVisibility(
                        visible = true,
                        enter = if (isAnimationEnabled) {
                            slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialOffsetX = { (-it * 0.3).toInt() }
                            ) + fadeIn(
                                animationSpec = tween(400)
                            ) + scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialScale = 0.8f
                            )
                        } else {
                            EnterTransition.None
                        }
                    ) {
                        Surface(
                            modifier = Modifier
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    ambientColor = AiBubbleGlow,
                                    spotColor = AiBubbleGlow
                                )
                                .clip(RoundedCornerShape(28.dp)),
                            color = AiBubbleColor.copy(alpha = 0.95f),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Box(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                TypingIndicator(isAnimationEnabled = isAnimationEnabled)
                            }
                        }
                    }
                } else {
                    AnimatedVisibility(
                        visible = true,
                        enter = if (isAnimationEnabled) {
                            slideInHorizontally(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialOffsetX = { (-it * 0.3).toInt() }
                            ) + fadeIn(
                                animationSpec = tween(400)
                            ) + scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialScale = 0.8f
                            )
                        } else {
                            EnterTransition.None
                        }
                    ) {
                        Surface(
                            modifier = Modifier
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    alpha = alpha
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isPressed = true
                                            tryAwaitRelease()
                                            isPressed = false
                                        }
                                    )
                                }
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    ambientColor = AiBubbleGlow,
                                    spotColor = AiBubbleGlow
                                )
                                .clip(RoundedCornerShape(28.dp)),
                            color = AiBubbleColor.copy(alpha = 0.95f),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                // Simple, always visible text
                                Text(
                                    text = message.content,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 24.sp,
                                    softWrap = true
                                )
                                
                                // Copy button
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    val clipboardManager = LocalClipboardManager.current
                                    var showCopiedFeedback by remember { mutableStateOf(false) }
                                    
                                    Surface(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(message.content))
                                            showCopiedFeedback = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (showCopiedFeedback) "Copied!" else "Copy",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    
                                    // Reset copied feedback after delay
                                    LaunchedEffect(showCopiedFeedback) {
                                        if (showCopiedFeedback) {
                                            delay(2000)
                                            showCopiedFeedback = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 