package com.example.picchallenge.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.picchallenge.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Contests : BottomNavItem(
        route = "contests",
        title = "Contests",
        icon = Icons.Default.PhotoCamera
    )
    
    object News : BottomNavItem(
        route = "blog",
        title = "News",
        icon = Icons.Default.Article
    )
    
    object Join : BottomNavItem(
        route = "join",
        title = "Join",
        icon = Icons.Default.PersonAdd
    )
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    onItemSelected: (BottomNavItem) -> Unit
) {
    val items = listOf(
        BottomNavItem.Contests,
        BottomNavItem.News,
        BottomNavItem.Join
    )
    
    Surface(
        modifier = Modifier
            .shadow(elevation = 8.dp),
        color = SurfaceWhite,
        tonalElevation = 0.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = TextPrimary,
            tonalElevation = 0.dp
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            items.forEach { item ->
                // Enhanced route matching - handle detail screens
                val isSelected = when (item.route) {
                    "contests" -> currentRoute == "contests" || currentRoute?.startsWith("contest/") == true
                    "blog" -> currentRoute == "blog" || currentRoute?.startsWith("blog/") == true
                    else -> currentRoute == item.route
                }
                
                NavigationBarItem(
                    icon = { 
                        Icon(
                            item.icon, 
                            contentDescription = item.title,
                            tint = if (isSelected) PrimaryModern else TextSecondary
                        ) 
                    },
                    label = { 
                        Text(
                            item.title,
                            color = if (isSelected) PrimaryModern else TextSecondary
                        ) 
                    },
                    selected = isSelected,
                    onClick = { 
                        // Handle navigation logic for detail screens
                        when (item.route) {
                            "contests" -> {
                                // If we're already on a contest detail screen, pop back to contests list
                                if (currentRoute?.startsWith("contest/") == true) {
                                    navController.popBackStack("contests", inclusive = false)
                                } else {
                                    onItemSelected(item)
                                }
                            }
                            "blog" -> {
                                // If we're already on a blog detail screen, pop back to blog list
                                if (currentRoute?.startsWith("blog/") == true) {
                                    navController.popBackStack("blog", inclusive = false)
                                } else {
                                    onItemSelected(item)
                                }
                            }
                            else -> onItemSelected(item)
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryModern,
                        selectedTextColor = PrimaryModern,
                        indicatorColor = PrimaryModern.copy(alpha = 0.1f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    }
}
