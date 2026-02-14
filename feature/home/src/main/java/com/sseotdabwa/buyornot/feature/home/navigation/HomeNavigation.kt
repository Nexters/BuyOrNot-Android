package com.sseotdabwa.buyornot.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

const val HOME_ROUTE = "home"

fun NavGraphBuilder.homeScreen() {
    composable(route = HOME_ROUTE) {
        // HomeRoute()
    }
}

fun NavHostController.navigateToHome() {
    navigate(HOME_ROUTE)
}
