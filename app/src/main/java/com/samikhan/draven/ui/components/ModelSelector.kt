package com.samikhan.draven.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.samikhan.draven.ui.theme.*

data class AIModel(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: String,
    val isSelected: Boolean = false,
    val isNew: Boolean = false,
    val isMax: Boolean = false,
    val isAvailable: Boolean = true,
    val isHighTraffic: Boolean = false,
    val recommendedForSpeed: Boolean = false
)

@Composable
fun ModelSelector(
    models: List<AIModel>,
    onModelSelected: (AIModel) -> Unit,
    onDismiss: () -> Unit,
    isDarkMode: Boolean
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = MaterialTheme.colorScheme.surfaceTint,
                    spotColor = MaterialTheme.colorScheme.surfaceTint
                ),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Text(
                    text = "Select AI Model",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Model list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    // Best Models
                    val bestModels = models.filter { it.category == "Best" }
                    if (bestModels.isNotEmpty()) {
                        item {
                            Text(
                                text = "Best",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(bestModels) { model ->
                            ModelItem(
                                model = model,
                                onClick = {
                                    onModelSelected(model)
                                    onDismiss()
                                },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                    
                    // Reasoning Models
                    val reasoningModels = models.filter { it.category == "Reasoning" }
                    if (reasoningModels.isNotEmpty()) {
                        item {
                            if (bestModels.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            Text(
                                text = "Reasoning",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(reasoningModels) { model ->
                            ModelItem(
                                model = model,
                                onClick = {
                                    onModelSelected(model)
                                    onDismiss()
                                },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }
                
                // Cancel button
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ModelItem(
    model: AIModel,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "model_item_scale"
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
        shape = RoundedCornerShape(12.dp),
        color = if (model.isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(
            1.dp,
            if (model.isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Model icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (model.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = model.name,
                    tint = if (model.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Model info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (model.isNew) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NEW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    if (model.isHighTraffic) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BUSY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                    
                    if (model.recommendedForSpeed) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "FAST",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
                
                Text(
                    text = model.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            // Selection indicator
            if (model.isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
