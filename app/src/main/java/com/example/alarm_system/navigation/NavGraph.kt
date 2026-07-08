package com.example.alarm_system.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.alarm_system.features.alarm_edit.AlarmEditScreen
import com.example.alarm_system.features.alarm_edit.AlarmEditViewModel
import com.example.alarm_system.features.alarm_list.AlarmListScreen
import com.example.alarm_system.features.alarm_list.AlarmListViewModel
import com.example.alarm_system.features.analytics.AnalyticsScreen
import com.example.alarm_system.features.timer.TimerScreen
import com.example.alarm_system.features.stopwatch.StopwatchScreen
import com.example.alarm_system.features.onboarding.OnboardingScreen
import com.example.alarm_system.features.onboarding.OnboardingViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "alarm_list"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("onboarding") {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate("alarm_list") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("alarm_list") {
            val viewModel: AlarmListViewModel = hiltViewModel()
            AlarmListScreen(
                viewModel = viewModel,
                onAddAlarm = { navController.navigate("alarm_edit/-1") },
                onEditAlarm = { id -> navController.navigate("alarm_edit/$id") }
            )
        }
        composable(
            route = "alarm_edit/{alarmId}",
            arguments = listOf(navArgument("alarmId") { type = NavType.IntType })
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getInt("alarmId") ?: -1
            val viewModel: AlarmEditViewModel = hiltViewModel()
            viewModel.loadAlarm(alarmId)
            AlarmEditScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("analytics") {
            AnalyticsScreen()
        }
        composable("timer") {
            TimerScreen()
        }
        composable("stopwatch") {
            StopwatchScreen()
        }
    }
}
