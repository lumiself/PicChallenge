package com.example.picchallenge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.picchallenge.ui.contest.ContestListScreen
import com.example.picchallenge.ui.contest.ContestDetailScreen
import com.example.picchallenge.ui.auth.LoginScreen
import com.example.picchallenge.ui.profile.ProfileScreen
import com.example.picchallenge.ui.upload.PhotoUploadScreen
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
                },
                onProfileClick = {
                    navController.navigate("profile")
                }
            )
        }
        
        composable("contest/{id}") { backStackEntry ->
            val contestId = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
            ContestDetailScreen(
                contestId = contestId,
                onNavigateBack = { navController.popBackStack() },
                onUploadPhoto = {
                    navController.navigate("upload/$contestId")
                },
                onViewSubmissions = {
                    navController.navigate("submissions")
                }
            )
        }
        
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.popBackStack()
                },
                onNavigateToRegister = {
                    // Handle registration navigation - for now just go back
                    navController.popBackStack()
                }
            )
        }
        
        composable("profile") {
            ProfileScreen(
                onLoginClick = {
                    navController.navigate("login")
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUploadPhoto = { contestId ->
                    navController.navigate("upload/$contestId")
                },
                onViewSubmissions = {
                    navController.navigate("submissions")
                }
            )
        }
        
        composable("upload/{contestId}") { backStackEntry ->
            val contestId = backStackEntry.arguments?.getString("contestId")?.toIntOrNull() ?: return@composable
            PhotoUploadScreen(
                contestId = contestId,
                onUploadComplete = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
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
    }
}
