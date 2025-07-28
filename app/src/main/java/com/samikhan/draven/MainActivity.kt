package com.samikhan.draven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.samikhan.draven.data.preferences.ThemePreferences
import com.samikhan.draven.ui.navigation.DravenNavigation
import com.samikhan.draven.ui.theme.DravenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themePreferences = remember { ThemePreferences(this) }
            DravenTheme(isDarkMode = themePreferences.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    DravenNavigation(
                        navController = navController,
                        themePreferences = themePreferences
                    )
                }
            }
        }
    }
}