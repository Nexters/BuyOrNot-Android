package com.sseotdabwa.buyornot.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.navigation.BottomSheetNavigator
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sseotdabwa.buyornot.core.designsystem.components.ExpandableFloatingActionButton
import com.sseotdabwa.buyornot.core.designsystem.components.FabOption
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.feature.auth.navigation.AUTH_ROUTE
import com.sseotdabwa.buyornot.feature.home.navigation.HOME_ROUTE
import com.sseotdabwa.buyornot.feature.mypage.navigation.MYPAGE_ROUTE
import com.sseotdabwa.buyornot.feature.upload.navigation.UPLOAD_ROUTE
import com.sseotdabwa.buyornot.navigation.BuyOrNotNavHost

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BuyOrNotApp() {
    val sheetState =
        rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            skipHalfExpanded = true,
        )
    val bottomSheetNavigator =
        remember(sheetState) {
            BottomSheetNavigator(sheetState = sheetState)
        }
    val navController = rememberNavController(bottomSheetNavigator)
    var isFabExpanded by remember { mutableStateOf(false) }

    val destinations =
        listOf(
            TopLevelDestination(route = HOME_ROUTE, label = "홈", icon = Icons.Filled.Home),
            TopLevelDestination(route = MYPAGE_ROUTE, label = "마이페이지", icon = Icons.Filled.AccountCircle),
            TopLevelDestination(route = AUTH_ROUTE, label = "로그인", icon = Icons.Filled.Login),
        )

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetBackgroundColor = Color.Transparent,
    ) {
        Scaffold(
            bottomBar = {
                BuyOrNotBottomBar(navController = navController, destinations = destinations)
            },
            floatingActionButton = {
                ExpandableFloatingActionButton(
                    expanded = isFabExpanded,
                    onExpandedChange = { isFabExpanded = it },
                    options =
                        listOf(
                            FabOption(
                                icon = BuyOrNotIcons.Bag.asImageVector(),
                                label = "업로드",
                                onClick = {
                                    navController.navigate(UPLOAD_ROUTE)
                                },
                            ),
                        ),
                )
            },
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                BuyOrNotNavHost(
                    navController = navController,
                )
            }
        }
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
