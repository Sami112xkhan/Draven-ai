package com.samikhan.draven.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    isDarkModeFlow: Flow<Boolean>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkMode by isDarkModeFlow.collectAsState(initial = true)

    var isVideoReady by remember { mutableStateOf(false) }
    var isTextVisible by remember { mutableStateOf(false) }
    var isTransitioning by remember { mutableStateOf(false) }

    val textAlpha by animateFloatAsState(
        targetValue = if (isTextVisible) 1f else 0f,
        animationSpec = tween(1000, easing = EaseInOutCubic),
        label = "text_alpha"
    )

    val transitionAlpha by animateFloatAsState(
        targetValue = if (isTransitioning) 0f else 1f,
        animationSpec = tween(500, easing = EaseInOutCubic),
        label = "transition_alpha"
    )

    val backgroundGradient = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1A1A),
                Color(0xFF2D2D2D),
                Color(0xFF1A1A1A)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE8E8E8),
                Color(0xFFF0F0F0),
                Color(0xFFE8E8E8)
            )
        )
    }
    
    val glassColor = if (isDarkMode) {
        Color(0x80000000)
    } else {
        Color(0x80FFFFFF)
    }
    
    val textColor = if (isDarkMode) {
        Color.White
    } else {
        Color(0xFF1A1A1A)
    }
    
    val glassOverlayColor = if (isDarkMode) {
        glassColor.copy(alpha = 0.3f)
    } else {
        Color(0x40FFFFFF)
    }

    // --- ExoPlayer for video ---
    val videoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/raw/animation")
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    // --- ExoPlayer for audio ---
    val audioPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/raw/wakingup")
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_OFF
            volume = 1f
        }
    }

    // Fade out audio at the end
    LaunchedEffect(isVideoReady) {
        if (isVideoReady) {
            delay(1500)
            // Fade out over 500ms
            val fadeSteps = 10
            val fadeDuration = 500L
            val stepDuration = fadeDuration / fadeSteps
            for (i in fadeSteps downTo 1) {
                audioPlayer.volume = i / fadeSteps.toFloat()
                delay(stepDuration)
            }
            audioPlayer.volume = 0f
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            videoPlayer.release()
            audioPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        delay(500)
        isTextVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .alpha(transitionAlpha),
        contentAlignment = Alignment.Center
    ) {
        // Video Player
        AndroidView(
            factory = { context ->
                videoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                isVideoReady = true
                            }
                            Player.STATE_ENDED -> {
                                coroutineScope.launch {
                                    delay(500)
                                    isTransitioning = true
                                    delay(500)
                                    onSplashComplete()
                                }
                            }
                        }
                    }
                })
                PlayerView(context).apply {
                    player = videoPlayer
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Glass overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glassOverlayColor,
                            glassOverlayColor,
                            Color.Transparent
                        )
                    )
                )
        )

        // Centered text with glass effect
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isDarkMode) {
                            listOf(
                                glassColor.copy(alpha = 0.8f),
                                glassColor.copy(alpha = 0.6f)
                            )
                        } else {
                            listOf(
                                Color(0xCCFFFFFF),
                                Color(0x99FFFFFF)
                            )
                        }
                    )
                )
                .padding(24.dp)
                .alpha(textAlpha),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Draven is waking up...",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp
                ),
                color = textColor,
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
} 