package com.sseotdabwa.buyornot.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sseotdabwa.buyornot.feature.auth.navigation.AUTH_ROUTE
import com.sseotdabwa.buyornot.feature.home.navigation.HOME_ROUTE
import com.sseotdabwa.buyornot.feature.mypage.navigation.MYPAGE_ROUTE
import com.sseotdabwa.buyornot.feature.upload.navigation.UPLOAD_ROUTE
import com.sseotdabwa.buyornot.navigation.BuyOrNotNavHost

@Composable
fun BuyOrNotApp() {
    val navController = rememberNavController()
    val destinations =
        listOf(
            TopLevelDestination(route = HOME_ROUTE, label = "홈", icon = Icons.Filled.Home),
            TopLevelDestination(route = UPLOAD_ROUTE, label = "업로드", icon = Icons.Filled.UploadFile),
            TopLevelDestination(route = MYPAGE_ROUTE, label = "마이페이지", icon = Icons.Filled.AccountCircle),
            TopLevelDestination(route = AUTH_ROUTE, label = "로그인", icon = Icons.Filled.Login),
        )

    Scaffold(
        bottomBar = {
            BuyOrNotBottomBar(navController = navController, destinations = destinations)
        },
    ) { innerPadding ->
        BuyOrNotNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun BuyOrNotBottomBar(
    navController: NavHostController,
    destinations: List<TopLevelDestination>,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        destinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(imageVector = destination.icon, contentDescription = destination.label)
                },
                label = { Text(text = destination.label) },
            )
        }
    }
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)
