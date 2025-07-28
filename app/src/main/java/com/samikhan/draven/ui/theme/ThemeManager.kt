package com.samikhan.draven.ui.theme

import androidx.compose.ui.graphics.Color

fun getGlassBackground(isDarkMode: Boolean): Color = if (isDarkMode) GlassBackgroundDark else GlassBackgroundLight
fun getGlassBorder(isDarkMode: Boolean): Color = if (isDarkMode) GlassBorderDark else GlassBorderLight
fun getGlassGlow(isDarkMode: Boolean): Color = if (isDarkMode) GlassGlowDark else GlassGlowLight
fun getGlassShadow(isDarkMode: Boolean): Color = if (isDarkMode) GlassShadowDark else GlassShadowLight
fun getGlassmorphismBackground(isDarkMode: Boolean): Color = if (isDarkMode) GlassmorphismBackgroundDark else GlassmorphismBackgroundLight
fun getGlassmorphismBorder(isDarkMode: Boolean): Color = if (isDarkMode) GlassmorphismBorderDark else GlassmorphismBorderLight
fun getGlassmorphismGlow(isDarkMode: Boolean): Color = if (isDarkMode) GlassmorphismGlowDark else GlassmorphismGlowLight
fun getBackgroundColor(isDarkMode: Boolean): Color = if (isDarkMode) DarkBackground else LightBackground
fun getSurfaceColor(isDarkMode: Boolean): Color = if (isDarkMode) DarkSurface else LightSurface
fun getOnSurfaceColor(isDarkMode: Boolean): Color = if (isDarkMode) DarkOnSurface else LightOnSurface
fun getOnBackgroundColor(isDarkMode: Boolean): Color = if (isDarkMode) DarkOnBackground else LightOnBackground 