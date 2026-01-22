package com.sseotdabwa.buyornot.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sseotdabwa.buyornot.feature.home.ui.HomeRoute

const val HOME_ROUTE = "home"

fun NavGraphBuilder.homeScreen() {
    composable(route = HOME_ROUTE) {
        HomeRoute()
    }
}
