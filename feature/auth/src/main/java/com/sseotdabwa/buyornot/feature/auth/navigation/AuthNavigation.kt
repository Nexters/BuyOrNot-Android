package com.sseotdabwa.buyornot.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import com.sseotdabwa.buyornot.feature.auth.ui.AuthRoute as AuthScreen
import com.sseotdabwa.buyornot.feature.auth.ui.SplashRoute as SplashScreen

@Serializable
data object SplashRoute

@Serializable
data object AuthRoute

fun NavGraphBuilder.splashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onFinish: () -> Unit,
) {
    composable<SplashRoute> {
        SplashScreen(
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToHome = onNavigateToHome,
            onFinish = onFinish,
        )
    }
}

fun NavGraphBuilder.authScreen(
    onLoginSuccess: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    composable<AuthRoute> {
        AuthScreen(
            onLoginSuccess = onLoginSuccess,
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
        )
    }
}

fun NavHostController.navigateToLogin() {
    navigate(AuthRoute) {
        popUpTo<SplashRoute> {
            inclusive = true
        }
    }
}

fun NavHostController.navigateForceToLogin() {
    navigate(AuthRoute) {
        popUpTo(graph.id) {
            inclusive = true
        }
    }
}
