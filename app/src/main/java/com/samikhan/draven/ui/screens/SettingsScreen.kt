package com.samikhan.draven.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.samikhan.draven.data.preferences.ThemePreferences
import com.samikhan.draven.ui.theme.*
import com.samikhan.draven.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    themePreferences: ThemePreferences,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(LocalContext.current))
) {
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = true)
    val uiState by viewModel.uiState.collectAsState()
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showApiKey by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            delay(2000)
            viewModel.clearSaveStatus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, DarkSurface)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Developer Settings",
                        fontSize = 20.sp,
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

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!uiState.isAuthenticated) {
                    // Password Authentication
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            animationSpec = tween(300),
                            initialOffsetY = { it }
                        ) + fadeIn(animationSpec = tween(300))
                    ) {
                        PasswordAuthSection(
                            password = password,
                            onPasswordChange = { password = it },
                            showPassword = showPassword,
                            onShowPasswordChange = { showPassword = it },
                            onAuthenticate = { viewModel.authenticate(password) },
                            hasError = uiState.passwordError,
                            onErrorDismiss = { viewModel.clearPasswordError() },
                            isDarkMode = isDarkMode
                        )
                    }
                } else {
                    // API Key Management
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            animationSpec = tween(300),
                            initialOffsetY = { it }
                        ) + fadeIn(animationSpec = tween(300))
                    ) {
                        ApiKeySection(
                            nemotronApiKey = uiState.nemotronApiKey,
                            gptOssApiKey = uiState.gptOssApiKey,
                            onNemotronApiKeyChange = viewModel::updateNemotronApiKey,
                            onGptOssApiKeyChange = viewModel::updateGptOssApiKey,
                            showApiKey = showApiKey,
                            onShowApiKeyChange = { showApiKey = it },
                            onSave = viewModel::saveApiKeys,
                            isSaved = uiState.isSaved,
                            hasError = uiState.saveError,
                            onLogout = viewModel::resetAuthentication,
                            isDarkMode = isDarkMode
                        )
                    }
                    
                    // Voice Settings
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            animationSpec = tween(300, delayMillis = 100),
                            initialOffsetY = { it }
                        ) + fadeIn(animationSpec = tween(300, delayMillis = 100))
                    ) {
                        VoiceSettingsSection(
                            isDarkMode = isDarkMode
                        )
                    }
                }

                // Status Messages
                AnimatedVisibility(
                    visible = uiState.isSaved,
                    enter = slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { it }
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(
                        animationSpec = tween(300),
                        targetOffsetY = { it }
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = SuccessGreen.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, SuccessGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "API Keys saved successfully!",
                                color = SuccessGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = uiState.saveError,
                    enter = slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { it }
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(
                        animationSpec = tween(300),
                        targetOffsetY = { it }
                    ) + fadeOut(animationSpec = tween(300))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = ErrorRed.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, ErrorRed)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ErrorRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Please enter valid API keys for both models",
                                color = ErrorRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordAuthSection(
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onShowPasswordChange: (Boolean) -> Unit,
    onAuthenticate: () -> Unit,
    hasError: Boolean,
    onErrorDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = getGlassBackground(isDarkMode = isDarkMode),
        border = BorderStroke(1.dp, getGlassBorder(isDarkMode = isDarkMode))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Developer Access",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Enter the developer password to access API settings",
                color = DarkOnSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { onShowPasswordChange(!showPassword) }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DravenPrimary,
                    unfocusedBorderColor = getGlassBorder(isDarkMode = isDarkMode),
                    focusedLabelColor = DravenPrimary,
                    unfocusedLabelColor = DarkOnSurface.copy(alpha = 0.7f),
                    cursorColor = DravenPrimary,
                    focusedTextColor = DarkOnSurface,
                    unfocusedTextColor = DarkOnSurface
                ),
                isError = hasError,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onAuthenticate,
                enabled = password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DravenPrimary,
                    disabledContainerColor = DravenPrimary.copy(alpha = 0.3f)
                )
            ) {
                Text("Authenticate")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeySection(
    nemotronApiKey: String,
    gptOssApiKey: String,
    onNemotronApiKeyChange: (String) -> Unit,
    onGptOssApiKeyChange: (String) -> Unit,
    showApiKey: Boolean,
    onShowApiKeyChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    isSaved: Boolean,
    hasError: Boolean,
    onLogout: () -> Unit,
    isDarkMode: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = getGlassBackground(isDarkMode = isDarkMode),
        border = BorderStroke(1.dp, getGlassBorder(isDarkMode = isDarkMode))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "API Configuration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "Enter your NVIDIA API keys for both models",
                color = DarkOnSurface.copy(alpha = 0.7f)
            )

            // NeMoTron API Key
            Text(
                text = "NeMoTron Ultra API Key",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            OutlinedTextField(
                value = nemotronApiKey,
                onValueChange = onNemotronApiKeyChange,
                label = { Text("NeMoTron API Key") },
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { onShowApiKeyChange(!showApiKey) }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "Hide API key" else "Show API key"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DravenPrimary,
                    unfocusedBorderColor = getGlassBorder(isDarkMode = isDarkMode),
                    focusedLabelColor = DravenPrimary,
                    unfocusedLabelColor = DarkOnSurface.copy(alpha = 0.7f),
                    cursorColor = DravenPrimary,
                    focusedTextColor = DarkOnSurface,
                    unfocusedTextColor = DarkOnSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // GPT-OSS API Key
            Text(
                text = "GPT-OSS API Key",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            OutlinedTextField(
                value = gptOssApiKey,
                onValueChange = onGptOssApiKeyChange,
                label = { Text("GPT-OSS API Key") },
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { onShowApiKeyChange(!showApiKey) }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "Hide API key" else "Show API key"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DravenPrimary,
                    unfocusedBorderColor = getGlassBorder(isDarkMode = isDarkMode),
                    focusedLabelColor = DravenPrimary,
                    unfocusedLabelColor = DarkOnSurface.copy(alpha = 0.7f),
                    cursorColor = DravenPrimary,
                    focusedTextColor = DarkOnSurface,
                    unfocusedTextColor = DarkOnSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    enabled = nemotronApiKey.isNotBlank() && gptOssApiKey.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen
                    )
                ) {
                    Text("Save API Key")
                }
                
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRed
                    )
                ) {
                    Text("Logout")
                }
            }
        }
    }
} 

@Composable
fun VoiceSettingsSection(
    isDarkMode: Boolean
) {
    var isVoiceModeEnabled by remember { mutableStateOf(false) }
    var voiceSpeed by remember { mutableStateOf(1.0f) }
    var voicePitch by remember { mutableStateOf(1.0f) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = getGlassBackground(isDarkMode)
        ),
        border = BorderStroke(1.dp, getGlassBorder(isDarkMode))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = DravenNeon,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Voice Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = getOnSurfaceColor(isDarkMode)
                )
            }
            
            // Voice Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isVoiceModeEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = null,
                        tint = if (isVoiceModeEnabled) DravenNeon else getOnSurfaceColor(isDarkMode).copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Voice Mode",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = getOnSurfaceColor(isDarkMode)
                    )
                }
                
                Switch(
                    checked = isVoiceModeEnabled,
                    onCheckedChange = { isVoiceModeEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DravenNeon,
                        checkedTrackColor = DravenNeon.copy(alpha = 0.3f),
                        uncheckedThumbColor = getOnSurfaceColor(isDarkMode).copy(alpha = 0.6f),
                        uncheckedTrackColor = getOnSurfaceColor(isDarkMode).copy(alpha = 0.2f)
                    )
                )
            }
            
            // Voice Speed Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = getOnSurfaceColor(isDarkMode),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Voice Speed",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = getOnSurfaceColor(isDarkMode)
                        )
                    }
                    Text(
                        text = "${String.format("%.1f", voiceSpeed)}x",
                        fontSize = 14.sp,
                        color = getOnSurfaceColor(isDarkMode).copy(alpha = 0.7f)
                    )
                }
                
                Slider(
                    value = voiceSpeed,
                    onValueChange = { voiceSpeed = it },
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = DravenNeon,
                        activeTrackColor = DravenNeon,
                        inactiveTrackColor = getOnSurfaceColor(isDarkMode).copy(alpha = 0.3f)
                    )
                )
            }
            
            // Voice Pitch Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = getOnSurfaceColor(isDarkMode),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Voice Pitch",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = getOnSurfaceColor(isDarkMode)
                        )
                    }
                    Text(
                        text = "${String.format("%.1f", voicePitch)}x",
                        fontSize = 14.sp,
                        color = getOnSurfaceColor(isDarkMode).copy(alpha = 0.7f)
                    )
                }
                
                Slider(
                    value = voicePitch,
                    onValueChange = { voicePitch = it },
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = DravenNeon,
                        activeTrackColor = DravenNeon,
                        inactiveTrackColor = getOnSurfaceColor(isDarkMode).copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
} 