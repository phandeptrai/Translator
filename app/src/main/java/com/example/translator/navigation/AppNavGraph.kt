package com.example.translator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.translator.TranslatorViewModel
import com.example.translator.ui.screens.HistoryScreen
import com.example.translator.ui.screens.MainScreen
import com.example.translator.ui.screens.OfflineLanguagesScreen

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object History : Screen("history")
    object OfflineLanguages : Screen("offline_languages")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: TranslatorViewModel = viewModel()
) {
    NavHost(navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(
                viewModel = viewModel,
                onShowHistory = { navController.navigate(Screen.History.route) },
                onShowOfflineLanguages = { navController.navigate(Screen.OfflineLanguages.route) }
            )
        }
        composable(Screen.History.route) {
            val history by viewModel.translationHistory.collectAsState()
            HistoryScreen(
                history = history,
                onItemClick = { viewModel.useHistoryItem(it); navController.popBackStack() },
                onDeleteItem = { viewModel.deleteHistoryItem(it) },
                onClearHistory = { viewModel.clearHistory() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OfflineLanguages.route) {
            OfflineLanguagesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
} 