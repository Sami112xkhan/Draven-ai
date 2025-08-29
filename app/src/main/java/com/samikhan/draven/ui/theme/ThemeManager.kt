package com.samikhan.draven.ui.theme

import androidx.compose.ui.graphics.Color

// Material 3 color helper functions
fun getGlassBackground(isDarkMode: Boolean): Color = if (isDarkMode) DarkSurface.copy(alpha = 0.1f) else LightSurface.copy(alpha = 0.1f)
fun getGlassBorder(isDarkMode: Boolean): Color = if (isDarkMode) DarkOutline else LightOutline
fun getGlassGlow(isDarkMode: Boolean): Color = if (isDarkMode) DravenPrimary.copy(alpha = 0.2f) else DravenPrimary.copy(alpha = 0.1f)
fun getGlassShadow(isDarkMode: Boolean): Color = if (isDarkMode) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.2f)
fun getGlassmorphismBackground(isDarkMode: Boolean): Color = if (isDarkMode) DarkSurface.copy(alpha = 0.12f) else LightSurface.copy(alpha = 0.12f)
fun getGlassmorphismBorder(isDarkMode: Boolean): Color = if (isDarkMode) DarkOutline.copy(alpha = 0.3f) else LightOutline.copy(alpha = 0.3f)
fun getGlassmorphismGlow(isDarkMode: Boolean): Color = if (isDarkMode) DravenPrimary.copy(alpha = 0.15f) else DravenPrimary.copy(alpha = 0.1f)
fun getBackgroundColor(isDarkMode: Boolean): Color = if (isDarkMode) DarkBackground else LightBackground
fun getSurfaceColor(isDarkMode: Boolean): Color = if (isDarkMode) DarkSurface else LightSurface
fun getOnSurfaceColor(isDarkMode: Boolean): Color = if (isDarkMode) DarkOnSurface else LightOnSurface
fun getOnBackgroundColor(isDarkMode: Boolean): Color = if (isDarkMode) DarkOnBackground else LightOnBackground 