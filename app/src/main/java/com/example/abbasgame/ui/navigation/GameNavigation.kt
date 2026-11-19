package com.example.abbasgame.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.abbasgame.ui.screens.*

@Composable
fun GameNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigateToMain = {
                navController.navigate("main_menu") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("main_menu") {
            MainMenuScreen(
                context = context,
                onStartGame = { navController.navigate("levels") },
                onOpenAirplanes = { navController.navigate("airplanes") },
                onOpenLevels = { navController.navigate("levels") },
                onOpenRewards = { navController.navigate("rewards") },
                onOpenAchievements = { navController.navigate("achievements") },
                onOpenSettings = { navController.navigate("settings") }
            )
        }
        composable("airplanes") {
            AirplaneSelectionScreen(
                context = context,
                onBack = { navController.popBackStack() }
            )
        }
        composable("levels") {
            WorldLevelSelectionScreen(
                context = context,
                onSelectLevel = { levelId -> navController.navigate("gameplay/$levelId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "gameplay/{levelId}",
            arguments = listOf(navArgument("levelId") { type = NavType.IntType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1
            GamePlayScreen(
                context = context,
                levelId = levelId,
                onBack = { navController.popBackStack() },
                onWin = { score, rewardEarned -> navController.navigate("level_complete/$score/$rewardEarned") { popUpTo("levels") } }
            )
        }
        composable(
            route = "level_complete/{score}/{rewardEarned}",
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("rewardEarned") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 10
            val rewardEarned = backStackEntry.arguments?.getBoolean("rewardEarned") ?: false
            LevelCompleteScreen(
                context = context,
                score = score,
                rewardEarned = rewardEarned,
                onNextLevel = { navController.navigate("levels") { popUpTo("main_menu") } },
                onHome = { navController.navigate("main_menu") { popUpTo("main_menu") { inclusive = true } } }
            )
        }
        composable("rewards") {
            RewardsScreen(
                context = context,
                onBack = { navController.popBackStack() }
            )
        }
        composable("achievements") {
            AchievementsScreen(
                context = context,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                context = context,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
