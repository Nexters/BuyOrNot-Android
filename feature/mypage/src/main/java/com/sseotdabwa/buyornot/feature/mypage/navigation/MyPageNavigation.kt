package com.sseotdabwa.buyornot.feature.mypage.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.sseotdabwa.buyornot.core.ui.webview.navigateToFeedBack
import com.sseotdabwa.buyornot.core.ui.webview.navigateToPrivacyPolicy
import com.sseotdabwa.buyornot.core.ui.webview.navigateToTerms
import kotlinx.serialization.Serializable
import com.sseotdabwa.buyornot.feature.mypage.ui.AccountSettingRoute as AccountSettingScreen
import com.sseotdabwa.buyornot.feature.mypage.ui.BlockedAccountsRoute as BlockedAccountsScreen
import com.sseotdabwa.buyornot.feature.mypage.ui.MyPageRoute as MyPageScreen
import com.sseotdabwa.buyornot.feature.mypage.ui.PolicyRoute as PolicyScreen
import com.sseotdabwa.buyornot.feature.mypage.ui.WithdrawalRoute as WithdrawalScreen

@Serializable
data object MyPageGraph

@Serializable
data object MyPageMainRoute

@Serializable
data object AccountSettingRoute

@Serializable
data object PolicyRoute

@Serializable
data object WithdrawalRoute

@Serializable
data object BlockedAccountsRoute

fun NavController.navigateToMyPage() {
    navigate(MyPageGraph)
}

fun NavController.navigateToAccountSetting() {
    navigate(AccountSettingRoute)
}

fun NavController.navigateToPolicy() {
    navigate(PolicyRoute)
}

fun NavController.navigateToWithdrawal() {
    navigate(WithdrawalRoute)
}

fun NavController.navigateToBlockedAccounts() {
    navigate(BlockedAccountsRoute)
}

fun NavGraphBuilder.myPageGraph(
    navController: NavHostController,
    versionName: String,
    onNavigateToLogin: () -> Unit,
) {
    navigation<MyPageGraph>(startDestination = MyPageMainRoute) {
        composable<MyPageMainRoute> {
            MyPageScreen(
                versionName = versionName,
                onBackClick = navController::popBackStack,
                onAccountSettingClick = navController::navigateToAccountSetting,
                onBlockedAccountsClick = navController::navigateToBlockedAccounts,
                onPolicyClick = navController::navigateToPolicy,
                onFeedbackClick = navController::navigateToFeedBack,
            )
        }

        composable<AccountSettingRoute> {
            AccountSettingScreen(
                onBackClick = navController::popBackStack,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToWithdrawal = navController::navigateToWithdrawal,
            )
        }

        composable<PolicyRoute> {
            PolicyScreen(
                onBackClick = navController::popBackStack,
                onNavigateToTerms = navController::navigateToTerms,
                onNavigateToPrivacyPolicy = navController::navigateToPrivacyPolicy,
            )
        }

        composable<WithdrawalRoute> {
            WithdrawalScreen(
                onBackClick = navController::popBackStack,
                onNavigateToLogin = onNavigateToLogin,
            )
        }

        composable<BlockedAccountsRoute> {
            BlockedAccountsScreen(
                onBackClick = navController::popBackStack,
            )
        }
    }
}
