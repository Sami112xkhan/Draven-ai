package com.samikhan.draven.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samikhan.draven.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedDravenMessage(
    text: String,
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {},
    onSkipAnimation: () -> Unit = {},
    onTextUpdate: (String) -> Unit = {},
    borderColor: Color = Color.White.copy(alpha = 0.3f)
) {
    var animatedText by remember { mutableStateOf("") }
    var isAnimating by remember { mutableStateOf(true) }
    var isSkipped by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Animation state
    val animatable = remember { Animatable(0f) }
    
    LaunchedEffect(text) {
        if (text.isNotEmpty()) {
            isAnimating = true
            isSkipped = false
            animatedText = ""
            
            // Animate character by character
            for (i in text.indices) {
                if (!isAnimating) break
                animatedText = text.substring(0, i + 1)
                onTextUpdate(animatedText) // Notify parent of text updates for scrolling
                delay(25) // 25ms delay per character for smooth typing
            }
            
            isAnimating = false
            onAnimationComplete()
        }
    }
    
    // Skip animation on click
    val handleClick = {
        if (isAnimating) {
            isAnimating = false
            isSkipped = true
            animatedText = text
            onSkipAnimation()
        }
    }
    
    Surface(
        modifier = modifier
            .clickable { handleClick() }
            .clip(RoundedCornerShape(28.dp)),
        color = AiBubbleColor.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.padding(20.dp)
        ) {
            Column {
                Text(
                    text = animatedText,
                    color = AiBubbleText,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Normal
                )
                
                // Show cursor when animating
                if (isAnimating && !isSkipped) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(200))
                    ) {
                        Text(
                            text = "|",
                            color = DravenNeon,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "typing")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "typing_dot"
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AiBubbleText.copy(alpha = alpha))
            )
        }
    }
} 