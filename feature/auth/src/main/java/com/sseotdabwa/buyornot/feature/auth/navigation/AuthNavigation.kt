package com.sseotdabwa.buyornot.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import com.sseotdabwa.buyornot.feature.auth.ui.AuthRoute as AuthScreen
import com.sseotdabwa.buyornot.feature.auth.ui.NicknameSetupRoute as NicknameSetupScreen
import com.sseotdabwa.buyornot.feature.auth.ui.SplashRoute as SplashScreen

@Serializable
data object SplashRoute

@Serializable
data object AuthRoute

@Serializable
data object NicknameSetupRoute

fun NavGraphBuilder.splashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToNicknameSetup: () -> Unit,
    onFinish: () -> Unit,
) {
    composable<SplashRoute> {
        SplashScreen(
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToHome = onNavigateToHome,
            onNavigateToNicknameSetup = onNavigateToNicknameSetup,
            onFinish = onFinish,
        )
    }
}

fun NavGraphBuilder.authScreen(
    onLoginSuccess: () -> Unit,
    onNicknameRequired: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    composable<AuthRoute> {
        AuthScreen(
            onLoginSuccess = onLoginSuccess,
            onNicknameRequired = onNicknameRequired,
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
        )
    }
}

fun NavGraphBuilder.nicknameSetupScreen(onSetupComplete: () -> Unit) {
    composable<NicknameSetupRoute> {
        NicknameSetupScreen(onSetupComplete = onSetupComplete)
    }
}

fun NavHostController.navigateToNicknameSetup(navOptions: NavOptions? = null) {
    navigate(NicknameSetupRoute, navOptions)
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
