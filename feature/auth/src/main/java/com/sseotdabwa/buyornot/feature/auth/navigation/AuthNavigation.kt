package com.sseotdabwa.buyornot.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sseotdabwa.buyornot.feature.auth.ui.AuthRoute

const val AUTH_ROUTE = "auth"

fun NavGraphBuilder.authScreen() {
    composable(route = AUTH_ROUTE) {
        AuthRoute()
    }
}
