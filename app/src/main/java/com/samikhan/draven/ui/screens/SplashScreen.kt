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
import com.samikhan.draven.data.preferences.StartupPreferences

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    isDarkModeFlow: Flow<Boolean>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkMode by isDarkModeFlow.collectAsState(initial = true)
    
    // Startup preferences
    val startupPreferences = remember { StartupPreferences(context) }
    val shouldShowVideo by startupPreferences.shouldShowStartupVideo.collectAsState()

    var isVideoReady by remember { mutableStateOf(false) }
    var isTransitioning by remember { mutableStateOf(false) }
    
    // If startup video should not be shown, skip directly to main app
    LaunchedEffect(shouldShowVideo) {
        if (!shouldShowVideo) {
            // Mark as shown for future reference and proceed to main app
            startupPreferences.markStartupVideoShown()
            onSplashComplete()
            return@LaunchedEffect
        }
    }

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
            try {
                val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${com.samikhan.draven.R.raw.animation}")
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
                
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        android.util.Log.e("SplashScreen", "Video player error: ${error.message}")
                        // Skip to next screen if video fails
                        coroutineScope.launch {
                            delay(2000) // Show splash for 2 seconds even if video fails
                            onSplashComplete()
                        }
                    }
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        android.util.Log.d("SplashScreen", "Video playback state: $playbackState")
                        when (playbackState) {
                            Player.STATE_READY -> {
                                android.util.Log.d("SplashScreen", "Video is ready to play")
                                isVideoReady = true
                            }
                            Player.STATE_ENDED -> {
                                android.util.Log.d("SplashScreen", "Video playback ended")
                                coroutineScope.launch {
                                    // Mark startup video as shown
                                    startupPreferences.markStartupVideoShown()
                                    delay(200) // Reduced from 500ms
                                    isTransitioning = true
                                    delay(300) // Reduced from 500ms
                                    onSplashComplete()
                                }
                            }
                            Player.STATE_BUFFERING -> {
                                android.util.Log.d("SplashScreen", "Video is buffering")
                            }
                            Player.STATE_IDLE -> {
                                android.util.Log.d("SplashScreen", "Video player is idle")
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                android.util.Log.e("SplashScreen", "Failed to load video: ${e.message}")
                // Fallback: just show splash for 3 seconds
                coroutineScope.launch {
                    delay(3000)
                    onSplashComplete()
                }
            }
        }
    }



    DisposableEffect(Unit) {
        onDispose {
            videoPlayer.release()
        }
    }

    // Only render the video UI if the video should be shown
    if (shouldShowVideo) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .alpha(transitionAlpha),
            contentAlignment = Alignment.Center
        ) {
            // Video Player - Full Screen
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = videoPlayer
                        useController = false
                        setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                        // Make video fill the entire screen
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
} 