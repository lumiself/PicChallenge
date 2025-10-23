package com.example.picchallenge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.picchallenge.ui.auth.LoginScreen
import com.example.picchallenge.ui.auth.RegisterScreen
import com.example.picchallenge.ui.contest.ContestListScreen
import com.example.picchallenge.ui.contest.ContestDetailScreen
import com.example.picchallenge.ui.info.InfoScreen
import com.example.picchallenge.ui.submissions.MySubmissionsScreen

@Composable
fun PicChallengeNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "contests"
    ) {
        composable("contests") { 
            ContestListScreen(
                onContestClick = { contest ->
                    navController.navigate("contest/${contest.id}")
                }
            )
        }
        
        composable("contest/{id}") { backStackEntry ->
            val contestId = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
            ContestDetailScreen(
                contestId = contestId,
                onNavigateBack = { navController.popBackStack() },
                onViewSubmissions = {
                    navController.navigate("submissions")
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }
        
        
        composable("submissions") {
            MySubmissionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Authentication routes
        composable("login") {
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("register") {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        // Information screen
        composable("info") {
            InfoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
