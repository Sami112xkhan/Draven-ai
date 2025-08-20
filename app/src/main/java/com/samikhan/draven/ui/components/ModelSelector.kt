package com.samikhan.draven.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val selectedModel = models.find { it.isSelected }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isDarkMode) DarkSurface.copy(alpha = 0.95f) else LightSurface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, getGlassBorder(isDarkMode).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with selected model
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = selectedModel?.name ?: "Select Model",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                    if (selectedModel != null) {
                        Text(
                            text = selectedModel.subtitle,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                )
            }
            
            // Model list
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(300, easing = EaseOutCubic)
                ) + fadeIn(
                    animationSpec = tween(300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(300, easing = EaseInCubic)
                ) + fadeOut(
                    animationSpec = tween(300)
                )
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val bestModels = models.filter { it.category == "Best" }
                    val reasoningModels = models.filter { it.category == "Reasoning" }
                    
                    if (bestModels.isNotEmpty()) {
                        item {
                            Text(
                                text = "Best",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(bestModels) { model ->
                            ModelItem(
                                model = model,
                                onClick = { onModelSelected(model) },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                    
                    if (reasoningModels.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = if (isDarkMode) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f)
                            )
                            Text(
                                text = "Reasoning",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(reasoningModels) { model ->
                            ModelItem(
                                model = model,
                                onClick = { onModelSelected(model) },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                    

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
    val backgroundColor = if (model.isSelected) {
        DravenNeon.copy(alpha = 0.2f)
    } else if (isDarkMode) {
        Color.White.copy(alpha = 0.05f)
    } else {
        Color.Black.copy(alpha = 0.05f)
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = model.isAvailable) { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = if (model.isSelected) {
            BorderStroke(1.dp, DravenNeon.copy(alpha = 0.5f))
        } else {
            BorderStroke(1.dp, Color.Transparent)
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Model icon
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = model.name,
                tint = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = model.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkMode) Color.White else Color.Black
                    )
                    
                    if (model.isNew) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DravenNeon.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "New",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DravenNeon,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    if (model.isMax) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFFD700).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "max",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    if (model.isHighTraffic) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFF6B6B).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "busy",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B6B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    if (model.recommendedForSpeed) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF4ECDC4).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "fast",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4ECDC4),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = model.subtitle,
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                )
            }
            
            if (model.isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = DravenNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
