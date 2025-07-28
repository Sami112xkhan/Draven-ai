package com.samikhan.draven.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.samikhan.draven.data.model.ChatMessage
import com.samikhan.draven.data.model.MessageRole
import com.samikhan.draven.data.preferences.ThemePreferences
import com.samikhan.draven.ui.components.AnimatedDravenMessage
import com.samikhan.draven.ui.components.TypingIndicator
import com.samikhan.draven.ui.components.EnhancedMessageBubble
import com.samikhan.draven.ui.components.*
import com.samikhan.draven.ui.theme.*
import com.samikhan.draven.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    themePreferences: ThemePreferences,
    viewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    val hasPermission by viewModel.hasMicrophonePermission.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        }
    }
    
    // Function to request microphone permission
    val requestMicrophonePermission = {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.onPermissionGranted()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    // Theme state
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = true)

    // Enhanced scrolling behavior for animated messages
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }
    
    // Auto-scroll during animation
    val lastMessage = uiState.messages.lastOrNull()
    LaunchedEffect(lastMessage?.content, lastMessage?.role) {
        if (lastMessage?.role == MessageRole.ASSISTANT && lastMessage.content.isNotEmpty()) {
            // Scroll to bottom during animation
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkMode) {
                        listOf(
                            Color(0xFF0A0A0F),
                            Color(0xFF1A1A2E),
                            Color(0xFF0F0F23)
                        )
                    } else {
                        listOf(
                            Color(0xFFFAFAFA),
                            Color(0xFFF5F5F5),
                            Color(0xFFEEEEEE)
                        )
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Enhanced Top Bar with Clean Glass Effect
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                        ambientColor = getGlassGlow(isDarkMode),
                        spotColor = getGlassGlow(isDarkMode)
                    ),
                color = if (isDarkMode) DarkSurface.copy(alpha = 0.95f) else LightSurface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                border = BorderStroke(1.dp, getGlassBorder(isDarkMode).copy(alpha = 0.3f))
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Draven",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    actions = {
                        // Voice Mode Toggle
                        val isVoiceModeEnabled by viewModel.isVoiceModeEnabled.collectAsState()
                        var isVoicePressed by remember { mutableStateOf(false) }
                        val voiceScale by animateFloatAsState(
                            targetValue = if (isVoicePressed) 0.9f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "voice_scale"
                        )
                        
                        IconButton(
                            onClick = { 
                                if (!hasPermission) {
                                    requestMicrophonePermission()
                                } else {
                                    viewModel.toggleVoiceMode()
                                }
                            },
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = voiceScale,
                                    scaleY = voiceScale
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isVoicePressed = true
                                            tryAwaitRelease()
                                            isVoicePressed = false
                                        }
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = if (uiState.isVoiceModeEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = if (uiState.isVoiceModeEnabled) "Voice Mode On" else "Voice Mode Off",
                                tint = if (uiState.isVoiceModeEnabled) DravenNeon else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Enhanced Detailed Thinking Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Detailed Thinking",
                                tint = if (uiState.detailedThinking) DravenNeon else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = uiState.detailedThinking,
                                onCheckedChange = { viewModel.toggleDetailedThinking() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DravenNeon,
                                    checkedTrackColor = DravenNeon.copy(alpha = 0.3f),
                                    uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                        }
                        
                        // Theme Toggle Button
                        var isThemePressed by remember { mutableStateOf(false) }
                        val themeScale by animateFloatAsState(
                            targetValue = if (isThemePressed) 0.9f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "theme_scale"
                        )
                        
                        IconButton(
                            onClick = {
                                scope.launch {
                                    themePreferences.setDarkMode(!isDarkMode)
                                }
                            },
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = themeScale,
                                    scaleY = themeScale
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isThemePressed = true
                                            tryAwaitRelease()
                                            isThemePressed = false
                                        }
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Enhanced Action Buttons with animations
                        var isHistoryPressed by remember { mutableStateOf(false) }
                        var isSettingsPressed by remember { mutableStateOf(false) }
                        
                        val historyScale by animateFloatAsState(
                            targetValue = if (isHistoryPressed) 0.9f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "history_scale"
                        )
                        
                        val settingsScale by animateFloatAsState(
                            targetValue = if (isSettingsPressed) 0.9f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "settings_scale"
                        )
                        
                        IconButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = historyScale,
                                    scaleY = historyScale
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isHistoryPressed = true
                                            tryAwaitRelease()
                                            isHistoryPressed = false
                                        }
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = settingsScale,
                                    scaleY = settingsScale
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isSettingsPressed = true
                                            tryAwaitRelease()
                                            isSettingsPressed = false
                                        }
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }

            // Messages List with enhanced animations
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(
                    items = uiState.messages,
                    key = { it.id }
                ) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            animationSpec = tween(400, easing = EaseOutCubic),
                            initialOffsetY = { (it * 0.5).toInt() }
                        ) + fadeIn(
                            animationSpec = tween(400)
                        ),
                        exit = slideOutVertically(
                            animationSpec = tween(300),
                            targetOffsetY = { it }
                        ) + fadeOut(animationSpec = tween(300))
                    ) {
                        EnhancedMessageBubble(
                            message = message,
                            onTextUpdate = {
                                scope.launch {
                                    listState.animateScrollToItem(uiState.messages.size - 1)
                                }
                            },
                            borderColor = getGlassBorder(isDarkMode)
                        )
                    }
                }
            }
            
            // Voice Transcription Display
            VoiceTranscriptionDisplay(
                transcribedText = uiState.transcribedText,
                isListening = uiState.isListening,
                isDarkMode = isDarkMode,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Voice Controls
            VoiceControls(
                isSpeaking = uiState.isSpeaking,
                onStopSpeaking = { viewModel.stopSpeaking() },
                isDarkMode = isDarkMode,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Voice Error Display
            VoiceErrorDisplay(
                error = uiState.voiceError,
                isDarkMode = isDarkMode,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Permission Request Notification
            if (!hasPermission && uiState.isVoiceModeEnabled) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { it }
                    ) + fadeIn(animationSpec = tween(300))
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Microphone",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Text(
                                text = "Microphone permission needed for voice features",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF9800)
                            )
                            
                            TextButton(
                                onClick = { requestMicrophonePermission() },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFFFF9800)
                                )
                            ) {
                                Text("Grant")
                            }
                        }
                    }
                }
            }

            // Enhanced Input Section with Glassmorphism
            EnhancedChatInput(
                value = uiState.inputText,
                onValueChange = viewModel::updateInputText,
                onSendClick = {
                    viewModel.sendMessage(uiState.inputText)
                    keyboardController?.hide()
                },
                isLoading = uiState.isLoading,
                focusRequester = focusRequester,
                isDarkMode = isDarkMode,
                isVoiceModeEnabled = uiState.isVoiceModeEnabled && hasPermission,
                isListening = uiState.isListening,
                speechConfidence = uiState.speechConfidence,
                onToggleVoiceListening = { 
                    if (!hasPermission) {
                        requestMicrophonePermission()
                    } else {
                        viewModel.toggleVoiceListening()
                    }
                }
            )
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean,
    focusRequester: FocusRequester,
    isDarkMode: Boolean,
    isVoiceModeEnabled: Boolean = false,
    isListening: Boolean = false,
    speechConfidence: Float = 0f,
    onToggleVoiceListening: () -> Unit = {}
) {
    var isSendPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isSendPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "send_scale"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (value.isNotBlank() && !isLoading) 0.3f else 0.1f,
        animationSpec = tween(300),
        label = "glow_alpha"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = getGlassGlow(isDarkMode),
                spotColor = getGlassGlow(isDarkMode)
            ),
        shape = RoundedCornerShape(24.dp),
        color = if (isDarkMode) DarkSurface.copy(alpha = 0.95f) else LightSurface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, getGlassBorder(isDarkMode).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Button (if voice mode is enabled)
            if (isVoiceModeEnabled) {
                AnimatedMicrophoneButton(
                    isListening = isListening,
                    confidence = speechConfidence,
                    onToggleListening = onToggleVoiceListening,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        text = if (isVoiceModeEnabled) "Tap mic to speak..." else "Message Draven...",
                        color = DarkOnSurface.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = DravenNeon,
                    focusedTextColor = DarkOnSurface,
                    unfocusedTextColor = DarkOnSurface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                maxLines = 4,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Enhanced Send Button with animations
            Surface(
                onClick = {
                    if (value.isNotBlank() && !isLoading) {
                        onSendClick()
                    }
                },
                enabled = value.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(20.dp),
                color = if (value.isNotBlank() && !isLoading) DravenNeon else Color.White.copy(alpha = 0.1f),
                border = BorderStroke(
                    1.dp,
                    if (value.isNotBlank() && !isLoading) DravenNeon else Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                if (value.isNotBlank() && !isLoading) {
                                    isSendPressed = true
                                    tryAwaitRelease()
                                    isSendPressed = false
                                }
                            }
                        )
                    }
                    .shadow(
                        elevation = if (value.isNotBlank() && !isLoading) 8.dp else 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = DravenNeon.copy(alpha = glowAlpha),
                        spotColor = DravenNeon.copy(alpha = glowAlpha)
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (value.isNotBlank() && !isLoading) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
} 