package com.sseotdabwa.buyornot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.sseotdabwa.buyornot.feature.auth.navigation.authScreen
import com.sseotdabwa.buyornot.feature.home.navigation.HOME_ROUTE
import com.sseotdabwa.buyornot.feature.home.navigation.homeScreen
import com.sseotdabwa.buyornot.feature.mypage.navigation.myPageScreen
import com.sseotdabwa.buyornot.feature.upload.navigation.uploadBottomSheet

@Composable
fun BuyOrNotNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier = modifier,
    ) {
        homeScreen()
        uploadBottomSheet(
            onDismiss = { navController.popBackStack() },
        )
        myPageScreen()
        authScreen()
    }
}
