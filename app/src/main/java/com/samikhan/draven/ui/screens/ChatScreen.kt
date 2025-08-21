package com.samikhan.draven.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.samikhan.draven.data.model.AIModelManager

import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
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
    
    // Animation state
    var isAnimationEnabled by remember { mutableStateOf(true) }
    
    // Sync animation state with preferences
    LaunchedEffect(Unit) {
        themePreferences.isAnimationEnabled.collect { prefValue ->
            isAnimationEnabled = prefValue
        }
    }
    
    // Voice mode state
    val isVoiceModeEnabled by viewModel.isVoiceModeEnabled.collectAsState()
    
    // Model selector state
    var isModelSelectorExpanded by remember { mutableStateOf(false) }
    val availableModels = remember { AIModelManager.getAllModels() }
    
    // Overflow menu state
    var showOverflowMenu by remember { mutableStateOf(false) }

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
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    } else {
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Enhanced Top App Bar with Overflow Menu
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
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Draven",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Voice Mode Toggle
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
                                    imageVector = if (isVoiceModeEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = if (isVoiceModeEnabled) "Voice Mode On" else "Voice Mode Off",
                                    tint = if (isVoiceModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            // Light/Dark Mode Toggle
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
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                actions = {
                        // Overflow Menu
                        var isOverflowPressed by remember { mutableStateOf(false) }
                        val overflowScale by animateFloatAsState(
                            targetValue = if (isOverflowPressed) 0.9f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "overflow_scale"
                        )
                        
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = overflowScale,
                                    scaleY = overflowScale
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isOverflowPressed = true
                                            tryAwaitRelease()
                                            isOverflowPressed = false
                                        }
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

        // Overflow Menu Dialog
        if (showOverflowMenu) {
            OverflowMenuDialog(
                onDismiss = { showOverflowMenu = false },
                onCriticalThinkingToggle = { 
                    viewModel.toggleDetailedThinking()
                },
                onAnimationToggle = { 
                    isAnimationEnabled = !isAnimationEnabled 
                    scope.launch {
                        themePreferences.setAnimationEnabled(isAnimationEnabled)
                    }
                },
                onHistoryClick = { 
                    showOverflowMenu = false
                    onNavigateToHistory() 
                },
                onAnalyticsClick = { 
                    showOverflowMenu = false
                    onNavigateToAnalytics() 
                },
                onSettingsClick = { 
                    showOverflowMenu = false
                    onNavigateToSettings() 
                },
                isCriticalThinkingEnabled = uiState.detailedThinking,
                isAnimationEnabled = isAnimationEnabled
            )
        }

        // Model Selector Modal
        if (isModelSelectorExpanded) {
            ModelSelector(
                models = availableModels,
                onModelSelected = { model ->
                    AIModelManager.setCurrentModel(model.id)
                    isModelSelectorExpanded = false
                },
                onDismiss = { isModelSelectorExpanded = false },
                isDarkMode = isDarkMode
            )
        }



            // Chat Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(uiState.messages) { message ->
                    EnhancedMessageBubble(
                        message = message,
                        isAnimationEnabled = isAnimationEnabled
                    )
                }
            }

            // Simple Bottom Chat Bar
            SimpleChatInput(
                value = uiState.inputText,
                onValueChange = { viewModel.updateInputText(it) },
                onSendClick = {
                    if (uiState.inputText.isNotBlank()) {
                        viewModel.sendMessage(uiState.inputText)
                        keyboardController?.hide()
                    }
                },
                onVoiceClick = {
                    if (isVoiceModeEnabled) {
                        if (uiState.isListening) {
                            viewModel.stopVoiceListening()
                        } else {
                            viewModel.startVoiceListening()
                            keyboardController?.hide()
                        }
                    } else {
                        // Toggle voice mode on first click
                        viewModel.toggleVoiceMode()
                    }
                },
                onStopSpeaking = { viewModel.stopVoiceResponse() },
                onToggleModelSelector = { isModelSelectorExpanded = !isModelSelectorExpanded },
                isLoading = uiState.isLoading,
                isVoiceModeEnabled = isVoiceModeEnabled,
                isListening = uiState.isListening,
                isSpeaking = uiState.isSpeaking,
                focusRequester = focusRequester
            )
        }
    }
}

@Composable
fun SimpleChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onStopSpeaking: () -> Unit,
    onToggleModelSelector: () -> Unit,
    isLoading: Boolean,
    isVoiceModeEnabled: Boolean,
    isListening: Boolean,
    isSpeaking: Boolean,
    focusRequester: FocusRequester
) {
    var isSendPressed by remember { mutableStateOf(false) }
    var isVoicePressed by remember { mutableStateOf(false) }
    var isStopPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isSendPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "send_scale"
    )
    
    val voiceScale by animateFloatAsState(
        targetValue = if (isVoicePressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "voice_scale"
    )
    
    val stopScale by animateFloatAsState(
        targetValue = if (isStopPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "stop_scale"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Button (only show if voice mode is enabled)
            if (isVoiceModeEnabled) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
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
                        .clickable { onVoiceClick() }
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Recording" else "Start Voice Input",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            // Stop Speaking Button (only show when AI is speaking)
            if (isSpeaking) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer(
                            scaleX = stopScale,
                            scaleY = stopScale
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isStopPressed = true
                                    tryAwaitRelease()
                                    isStopPressed = false
                                }
                            )
                        }
                        .clickable { onStopSpeaking() }
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeOff,
                        contentDescription = "Stop AI Speaking",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            // Large Input Field (hide if voice mode is active and listening)
            if (!isVoiceModeEnabled || !isListening) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = if (isVoiceModeEnabled) "Tap mic to speak..." else "Message Draven...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { 
                        if (value.isNotBlank() && !isLoading) {
                            onSendClick()
                        }
                    }),
                    maxLines = 4,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            } else {
                // Show listening indicator when voice mode is active
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Listening...",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // AI Model Selector Button
            var isModelButtonPressed by remember { mutableStateOf(false) }
            val modelButtonScale by animateFloatAsState(
                targetValue = if (isModelButtonPressed) 0.9f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "model_button_scale"
            )
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer(
                        scaleX = modelButtonScale,
                        scaleY = modelButtonScale
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isModelButtonPressed = true
                                tryAwaitRelease()
                                isModelButtonPressed = false
                            }
                        )
                    }
                    .clickable { onToggleModelSelector() }
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Select AI Model",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Send Button (hide if voice mode is active and listening)
            if (!isVoiceModeEnabled || !isListening) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (value.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (value.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                        .clickable(enabled = value.isNotBlank() && !isLoading) {
                            if (value.isNotBlank() && !isLoading) {
                                onSendClick()
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (value.isNotBlank() && !isLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OverflowMenuDialog(
    onDismiss: () -> Unit,
    onCriticalThinkingToggle: () -> Unit,
    onAnimationToggle: () -> Unit,
    onHistoryClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isCriticalThinkingEnabled: Boolean,
    isAnimationEnabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.TopEnd
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(200)) + 
                    slideInHorizontally(
                        initialOffsetX = { it / 2 },
                        animationSpec = tween(200)
                    ),
            exit = fadeOut(animationSpec = tween(150)) + 
                   slideOutHorizontally(
                       targetOffsetX = { it / 2 },
                       animationSpec = tween(150)
                   )
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 80.dp, end = 16.dp)
                    .width(280.dp)
                    .wrapContentHeight()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = MaterialTheme.colorScheme.surfaceTint,
                        spotColor = MaterialTheme.colorScheme.surfaceTint
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Prevent click-through */ },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    // Header with back button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Menu",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Menu",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    OverflowMenuItem(
                        icon = Icons.Default.Psychology,
                        title = "Critical Thinking",
                        subtitle = if (isCriticalThinkingEnabled) "Enabled" else "Disabled",
                        onClick = {
                            onCriticalThinkingToggle()
                            onDismiss()
                        },
                        isActive = isCriticalThinkingEnabled
                    )
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    OverflowMenuItem(
                        icon = Icons.Default.Speed,
                        title = "Animations",
                        subtitle = if (isAnimationEnabled) "Enabled" else "Disabled",
                        onClick = {
                            onAnimationToggle()
                            onDismiss()
                        },
                        isActive = isAnimationEnabled
                    )
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    OverflowMenuItem(
                        icon = Icons.Default.History,
                        title = "History",
                        subtitle = "View chat history",
                        onClick = {
                            onHistoryClick()
                        },
                        isActive = false
                    )
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    OverflowMenuItem(
                        icon = Icons.Default.Analytics,
                        title = "Insights & Analytics",
                        subtitle = "View usage analytics and insights",
                        onClick = {
                            onAnalyticsClick()
                        },
                        isActive = false
                    )
                    
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    OverflowMenuItem(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        subtitle = "Configure app settings",
                        onClick = {
                            onSettingsClick()
                        },
                        isActive = false
                    )
                }
            }
        }
    }
}

@Composable
fun OverflowMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isActive: Boolean
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "menu_item_scale"
    )
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
} 