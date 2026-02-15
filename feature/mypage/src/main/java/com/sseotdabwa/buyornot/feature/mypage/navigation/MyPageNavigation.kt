package com.sseotdabwa.buyornot.feature.mypage.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.sseotdabwa.buyornot.core.ui.navigateToPrivacyPolicy
import com.sseotdabwa.buyornot.core.ui.navigateToTerms
import com.sseotdabwa.buyornot.feature.mypage.ui.AccountSettingRoute
import com.sseotdabwa.buyornot.feature.mypage.ui.MyPageRoute
import com.sseotdabwa.buyornot.feature.mypage.ui.PolicyRoute
import com.sseotdabwa.buyornot.feature.mypage.ui.WithdrawalRoute

sealed class MyPageScreens(
    val route: String,
) {
    object Graph : MyPageScreens("mypage_graph")

    object Main : MyPageScreens("mypage_main")

    object AccountSetting : MyPageScreens("account_setting")

    object Policy : MyPageScreens("policy")

    object Withdrawal : MyPageScreens("withdrawal")
}

fun NavController.navigateToMyPage() {
    this.navigate(MyPageScreens.Graph.route)
}

fun NavController.navigateToAccountSetting() {
    this.navigate(MyPageScreens.AccountSetting.route)
}

fun NavController.navigateToPolicy() {
    this.navigate(MyPageScreens.Policy.route)
}

fun NavController.navigateToWithdrawal() {
    this.navigate(MyPageScreens.Withdrawal.route)
}

fun NavGraphBuilder.myPageGraph(
    navController: NavHostController,
    versionName: String,
    onNavigateToLogin: () -> Unit,
) {
    navigation(
        startDestination = MyPageScreens.Main.route,
        route = MyPageScreens.Graph.route,
    ) {
        composable(MyPageScreens.Main.route) {
            MyPageRoute(
                versionName = versionName,
                onBackClick = navController::popBackStack,
                onAccountSettingClick = navController::navigateToAccountSetting,
                onPolicyClick = navController::navigateToPolicy,
            )
        }

        composable(MyPageScreens.AccountSetting.route) {
            AccountSettingRoute(
                onBackClick = navController::popBackStack,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToWithdrawal = navController::navigateToWithdrawal,
            )
        }

        composable(MyPageScreens.Policy.route) {
            PolicyRoute(
                onBackClick = navController::popBackStack,
                onNavigateToTerms = navController::navigateToTerms,
                onNavigateToPrivacyPolicy = navController::navigateToPrivacyPolicy,
            )
        }

        composable(MyPageScreens.Withdrawal.route) {
            WithdrawalRoute(
                onBackClick = navController::popBackStack,
                onNavigateToLogin = onNavigateToLogin,
            )
        }
    }
}
