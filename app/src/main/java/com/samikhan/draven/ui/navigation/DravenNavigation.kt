package com.samikhan.draven.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.samikhan.draven.data.preferences.ThemePreferences
import com.samikhan.draven.ui.screens.ChatScreen
import com.samikhan.draven.ui.screens.HistoryScreen
import com.samikhan.draven.ui.screens.SettingsScreen
import com.samikhan.draven.ui.screens.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Chat : Screen("chat")
    object History : Screen("history")
    object Settings : Screen("settings")
}

@Composable
fun DravenNavigation(
    navController: NavHostController,
    themePreferences: ThemePreferences
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                isDarkModeFlow = themePreferences.isDarkMode
            )
        }
        
        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                themePreferences = themePreferences
            )
        }
        
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onConversationSelected = { conversationId ->
                    // Navigate back to chat with the selected conversation
                    navController.popBackStack()
                    // TODO: Load the selected conversation in ChatScreen
                },
                themePreferences = themePreferences
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                themePreferences = themePreferences
            )
        }
    }
} 