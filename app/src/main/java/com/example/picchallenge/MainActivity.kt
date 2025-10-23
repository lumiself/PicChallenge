package com.example.picchallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.picchallenge.ui.auth.LoginScreen
import com.example.picchallenge.ui.auth.RegisterScreen
import com.example.picchallenge.ui.blog.BlogScreen
import com.example.picchallenge.ui.blog.BlogDetailScreen
import com.example.picchallenge.ui.contest.ContestListScreen
import com.example.picchallenge.ui.contest.ContestDetailScreen
import com.example.picchallenge.ui.info.InfoScreen
import com.example.picchallenge.ui.profile.ProfileScreen
import com.example.picchallenge.ui.submissions.MySubmissionsScreen
import com.example.picchallenge.ui.navigation.BottomNavItem
import com.example.picchallenge.ui.navigation.BottomNavigationBar
import com.example.picchallenge.ui.theme.PicChallengeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PicChallengeTheme {
                MainScreenWithBottomNavigation()
            }
        }
    }
}

@Composable
fun MainScreenWithBottomNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                onItemSelected = { item ->
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Contests.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Contests Tab
            composable(BottomNavItem.Contests.route) { 
                ContestListScreen(
                    onContestClick = { contest ->
                        navController.navigate("contest/${contest.id}")
                    }
                )
            }
            
            // Blog/News Tab
            composable(BottomNavItem.News.route) {
                BlogScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onPostClick = { postId ->
                        // Navigate to blog detail screen
                        navController.navigate("blog/$postId")
                    }
                )
            }
            
            // Info Tab
            composable(BottomNavItem.Info.route) {
                InfoScreen(
                    onNavigateBack = {
                        // When pressing back from info, go to contests
                        navController.navigate("contests") {
                            popUpTo("contests") { inclusive = true }
                        }
                    }
                )
            }
            
            // Login Tab
            composable(BottomNavItem.Login.route) {
                LoginScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    },
                    onLoginSuccess = {
                        // After successful login, navigate back to contests
                        navController.navigate("contests") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            
            // Detail screens (not in bottom nav)
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
            
            // Blog detail screen
            composable("blog/{postId}") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
                BlogDetailScreen(
                    postId = postId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Authentication screens
            composable("login") {
                LoginScreen(
                    onNavigateBack = { navController.popBackStack() },
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
                    onNavigateBack = { navController.popBackStack() },
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
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
