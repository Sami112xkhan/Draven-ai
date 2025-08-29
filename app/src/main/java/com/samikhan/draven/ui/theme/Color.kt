package com.samikhan.draven.ui.theme

import androidx.compose.ui.graphics.Color

// Material 3 Expressive Color System
// Primary Brand Colors with deeper tonal palette
val DravenPrimary = Color(0xFF6750A4) // Material 3 Primary
val DravenPrimaryContainer = Color(0xFFEADDFF) // Primary Container
val DravenOnPrimary = Color(0xFFFFFFFF) // On Primary
val DravenOnPrimaryContainer = Color(0xFF21005D) // On Primary Container

// Secondary Brand Colors
val DravenSecondary = Color(0xFF625B71) // Material 3 Secondary
val DravenSecondaryContainer = Color(0xFFE8DEF8) // Secondary Container
val DravenOnSecondary = Color(0xFFFFFFFF) // On Secondary
val DravenOnSecondaryContainer = Color(0xFF1D192B) // On Secondary Container

// Tertiary/Accent Colors
val DravenTertiary = Color(0xFF7D5260) // Material 3 Tertiary
val DravenTertiaryContainer = Color(0xFFFFD8E4) // Tertiary Container
val DravenOnTertiary = Color(0xFFFFFFFF) // On Tertiary
val DravenOnTertiaryContainer = Color(0xFF31111D) // On Tertiary Container

// Error Colors
val DravenError = Color(0xFFBA1A1A) // Material 3 Error
val DravenErrorContainer = Color(0xFFFFDAD6) // Error Container
val DravenOnError = Color(0xFFFFFFFF) // On Error
val DravenOnErrorContainer = Color(0xFF410002) // On Error Container

// Surface Colors - Dark Theme
val DarkSurface = Color(0xFF1C1B1F) // Material 3 Dark Surface
val DarkSurfaceVariant = Color(0xFF49454F) // Dark Surface Variant
val DarkInverseSurface = Color(0xFFE6E1E5) // Dark Inverse Surface
val DarkInverseOnSurface = Color(0xFF313033) // Dark Inverse On Surface
val DarkOutline = Color(0xFF938F99) // Dark Outline
val DarkOutlineVariant = Color(0xFF49454F) // Dark Outline Variant

// Surface Colors - Light Theme
val LightSurface = Color(0xFFFFFBFE) // Material 3 Light Surface
val LightSurfaceVariant = Color(0xFFE7E0EC) // Light Surface Variant
val LightInverseSurface = Color(0xFF313033) // Light Inverse Surface
val LightInverseOnSurface = Color(0xFFF4EFF4) // Light Inverse On Surface
val LightOutline = Color(0xFF79747E) // Light Outline
val LightOutlineVariant = Color(0xFFCAC4D0) // Light Outline Variant

// Background Colors
val DarkBackground = Color(0xFF1C1B1F) // Material 3 Dark Background
val LightBackground = Color(0xFFFFFBFE) // Material 3 Light Background

// On Colors
val DarkOnSurface = Color(0xFFE6E1E5) // Dark On Surface
val DarkOnSurfaceVariant = Color(0xFFCAC4D0) // Dark On Surface Variant
val DarkOnBackground = Color(0xFFE6E1E5) // Dark On Background

val LightOnSurface = Color(0xFF1C1B1F) // Light On Surface
val LightOnSurfaceVariant = Color(0xFF49454F) // Light On Surface Variant
val LightOnBackground = Color(0xFF1C1B1F) // Light On Background

// Enhanced Chat Bubble Colors - Material 3 Style
val UserBubbleColor = DravenPrimary
val UserBubbleContainer = DravenPrimaryContainer
val UserBubbleText = DravenOnPrimary
val UserBubbleGlow = DravenPrimary.copy(alpha = 0.3f)

val AiBubbleColor = DarkSurfaceVariant
val AiBubbleContainer = LightSurfaceVariant
val AiBubbleText = DarkOnSurface
val AiBubbleGlow = DarkOutline.copy(alpha = 0.2f)
val AiBubbleBorder = DarkOutline.copy(alpha = 0.4f)

val SystemBubbleColor = DarkSurface
val SystemBubbleContainer = LightSurface
val SystemBubbleText = DarkOnSurface
val SystemBubbleGlow = DarkOutline.copy(alpha = 0.15f)

// Status Colors - Material 3 Style
val SuccessGreen = Color(0xFF4CAF50) // Material 3 Success
val SuccessContainer = Color(0xFFC8E6C9) // Success Container
val OnSuccess = Color(0xFF1B5E20) // On Success

val ErrorRed = Color(0xFFF44336) // Material 3 Error
val ErrorContainer = Color(0xFFFFCDD2) // Error Container
val OnError = Color(0xFFB71C1C) // On Error

val WarningYellow = Color(0xFFFF9800) // Material 3 Warning
val WarningContainer = Color(0xFFFFE0B2) // Warning Container
val OnWarning = Color(0xFFE65100) // On Warning

// Material 3 Expressive Gradients
val PrimaryGradient = listOf(
    DravenPrimary,
    DravenSecondary,
    DravenTertiary
)

val SurfaceGradient = listOf(
    Color(0xFF1C1B1F),
    Color(0xFF2B2930),
    Color(0xFF3A3741)
)

val LightSurfaceGradient = listOf(
    Color(0xFFFFFBFE),
    Color(0xFFF5F5F5),
    Color(0xFFEEEEEE)
)

// Material 3 State Colors
val StateHover = Color(0xFF1C1B1F).copy(alpha = 0.08f)
val StateFocus = Color(0xFF6750A4).copy(alpha = 0.12f)
val StatePressed = Color(0xFF1C1B1F).copy(alpha = 0.12f)
val StateDragged = Color(0xFF1C1B1F).copy(alpha = 0.16f)

// Material 3 Elevation Colors
val ElevationLevel0 = Color(0x00000000)
val ElevationLevel1 = Color(0xFF1C1B1F).copy(alpha = 0.05f)
val ElevationLevel2 = Color(0xFF1C1B1F).copy(alpha = 0.08f)
val ElevationLevel3 = Color(0xFF1C1B1F).copy(alpha = 0.11f)
val ElevationLevel4 = Color(0xFF1C1B1F).copy(alpha = 0.12f)
val ElevationLevel5 = Color(0xFF1C1B1F).copy(alpha = 0.14f)

// Legacy colors for backward compatibility (will be removed gradually)
val DravenNeon = DravenPrimary
val DravenAccent = DravenTertiary