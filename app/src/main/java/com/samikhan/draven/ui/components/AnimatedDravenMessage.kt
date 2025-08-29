package com.samikhan.draven.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samikhan.draven.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AnimatedDravenMessage(
    text: String,
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {},
    onSkipAnimation: () -> Unit = {},
    onTextUpdate: (String) -> Unit = {},
    borderColor: Color = Color.White.copy(alpha = 0.3f),
    isAnimationEnabled: Boolean = true
) {
    // Use stable keys to prevent animation restart on recomposition
    var animatedText by remember(text) { mutableStateOf(if (isAnimationEnabled) "" else text) }
    var isAnimating by remember(text) { mutableStateOf(false) }
    var hasAnimated by remember(text) { mutableStateOf(false) }
    
    // Animation values for smooth effects - Always visible
    val textAlpha by animateFloatAsState(
        targetValue = 1f, // Always fully visible
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "text_alpha"
    )
    
    val textScale by animateFloatAsState(
        targetValue = 1f, // Always full scale
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "text_scale"
    )
    
    LaunchedEffect(text, hasAnimated, isAnimationEnabled) {
        // Always show text if animation is disabled
        if (!isAnimationEnabled) {
            animatedText = text
            hasAnimated = true
            isAnimating = false
            onAnimationComplete()
            return@LaunchedEffect
        }
        
        // Only animate if text is new and hasn't been animated yet
        if (text.isNotEmpty() && !hasAnimated) {
            hasAnimated = true
            if (isAnimationEnabled) {
                isAnimating = true
                animatedText = ""
                
                // Modern word-by-word animation with smooth fade-in
                val words = text.split(" ")
                val animatedWords = mutableListOf<String>()
                
                for (word in words) {
                    if (!isAnimating) break
                    animatedWords.add(word)
                    animatedText = animatedWords.joinToString(" ")
                    onTextUpdate(animatedText)
                    delay(80) // 80ms per word for smooth reading pace
                }
                
                // Ensure full text is shown
                animatedText = text
                isAnimating = false
                onAnimationComplete()
            } else {
                // Skip animation entirely for instant response
                animatedText = text
                isAnimating = false
                onAnimationComplete()
            }
        }
    }
    
    // Skip animation on click
    val handleClick = {
        if (isAnimating) {
            isAnimating = false
            animatedText = text
            onSkipAnimation()
        }
    }
    
    val clipboardManager = LocalClipboardManager.current
    
    // Container animation - Always visible, just scale for entrance effect
    val containerAlpha by animateFloatAsState(
        targetValue = 1f, // Always visible
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "container_alpha"
    )
    
    val containerScale by animateFloatAsState(
        targetValue = 1f, // Always full size
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "container_scale"
    )

    Surface(
        modifier = modifier
            .clickable { handleClick() }
            .clip(RoundedCornerShape(28.dp))
            .graphicsLayer(
                alpha = containerAlpha,
                scaleX = containerScale,
                scaleY = containerScale
            ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.padding(20.dp)
        ) {
            Column {
                Text(
                    text = if (animatedText.isEmpty() && text.isNotEmpty()) text else animatedText, // Force show full text if animated text is empty
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    softWrap = true,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                    modifier = Modifier.graphicsLayer(
                        alpha = textAlpha,
                        scaleX = textScale,
                        scaleY = textScale
                    )
                )
                
                // Show modern typing indicator when animating
                if (isAnimating) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Animated typing dots
                        repeat(3) { index ->
                            val infiniteTransition = rememberInfiniteTransition(label = "typing_$index")
                            val dotScale by infiniteTransition.animateFloat(
                                initialValue = 0.5f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, delayMillis = index * 150),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "dot_scale_$index"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                    .graphicsLayer(
                                        scaleX = dotScale,
                                        scaleY = dotScale
                                    )
                            )
                        }
                        
                        Text(
                            text = "Typing...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Copy button - appears after animation completes
                AnimatedVisibility(
                    visible = !isAnimating && animatedText.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(300, delayMillis = 200)) + 
                            slideInVertically(
                                animationSpec = tween(300, delayMillis = 200),
                                initialOffsetY = { it / 4 }
                            ),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        var isPressed by remember { mutableStateOf(false) }
                        var showCopiedFeedback by remember { mutableStateOf(false) }
                        
                        val copyButtonScale by animateFloatAsState(
                            targetValue = if (isPressed) 0.95f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "copy_button_scale"
                        )
                        
                        Surface(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(text))
                                showCopiedFeedback = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = copyButtonScale,
                                    scaleY = copyButtonScale
                                )
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

@Composable
fun TypingIndicator(
    isAnimationEnabled: Boolean = true
) {
    if (isAnimationEnabled) {
        // Modern animated typing indicator
        Row(
            modifier = Modifier
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wave animation dots
            repeat(3) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "typing_wave_$index")
                val offsetY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, delayMillis = index * 200, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "wave_offset_$index"
                )
                
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, delayMillis = index * 200, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "wave_scale_$index"
                )
                
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .graphicsLayer(
                            translationY = offsetY,
                            scaleX = scale,
                            scaleY = scale
                        )
                )
            }
            
            Text(
                text = "AI is thinking...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        // Static typing indicator when animations are disabled
        Row(
            modifier = Modifier
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            
            Text(
                text = "AI is thinking...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
} 