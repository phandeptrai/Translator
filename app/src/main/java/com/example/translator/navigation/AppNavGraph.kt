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

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object History : Screen("history")
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
                onShowHistory = { navController.navigate(Screen.History.route) }
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
    }
} 