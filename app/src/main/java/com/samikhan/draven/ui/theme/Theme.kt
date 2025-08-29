package com.samikhan.draven.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// Material 3 Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = DravenPrimary,
    onPrimary = DravenOnPrimary,
    primaryContainer = DravenPrimaryContainer,
    onPrimaryContainer = DravenOnPrimaryContainer,
    secondary = DravenSecondary,
    onSecondary = DravenOnSecondary,
    secondaryContainer = DravenSecondaryContainer,
    onSecondaryContainer = DravenOnSecondaryContainer,
    tertiary = DravenTertiary,
    onTertiary = DravenOnTertiary,
    tertiaryContainer = DravenTertiaryContainer,
    onTertiaryContainer = DravenOnTertiaryContainer,
    error = DravenError,
    onError = DravenOnError,
    errorContainer = DravenErrorContainer,
    onErrorContainer = DravenOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DravenPrimaryContainer,
    surfaceTint = DravenPrimary
)

// Material 3 Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = DravenPrimary,
    onPrimary = DravenOnPrimary,
    primaryContainer = DravenPrimaryContainer,
    onPrimaryContainer = DravenOnPrimaryContainer,
    secondary = DravenSecondary,
    onSecondary = DravenOnSecondary,
    secondaryContainer = DravenSecondaryContainer,
    onSecondaryContainer = DravenOnSecondaryContainer,
    tertiary = DravenTertiary,
    onTertiary = DravenOnTertiary,
    tertiaryContainer = DravenTertiaryContainer,
    onTertiaryContainer = DravenOnTertiaryContainer,
    error = DravenError,
    onError = DravenOnError,
    errorContainer = DravenErrorContainer,
    onErrorContainer = DravenOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = DravenPrimary,
    surfaceTint = DravenPrimary
)

@Composable
fun DravenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Material 3 edge-to-edge design with transparent system bars
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun DravenTheme(
    isDarkMode: Flow<Boolean>,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = isDarkMode.collectAsState(initial = true).value
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Material 3 edge-to-edge design with transparent system bars
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}