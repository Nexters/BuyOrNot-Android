package com.sseotdabwa.buyornot.feature.mypage.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
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

fun NavGraphBuilder.myPageGraph(
    navController: NavController,
    versionName: String,
    onNavigateBack: () -> Unit,
    onAccountSettingClick: () -> Unit,
    onPolicyClick: () -> Unit,
) {
    navigation(
        startDestination = MyPageScreens.Main.route,
        route = MyPageScreens.Graph.route,
    ) {
        composable(MyPageScreens.Main.route) {
            MyPageRoute(
                versionName = versionName,
                onNavigateBack = onNavigateBack,
                onAccountSettingClick = onAccountSettingClick,
                onPolicyClick = onPolicyClick,
            )
        }

        composable(MyPageScreens.AccountSetting.route) {
            AccountSettingRoute(onBackClick = { navController.popBackStack() })
        }

        composable(MyPageScreens.Policy.route) {
            PolicyRoute(onBackClick = { navController.popBackStack() })
        }

        composable(MyPageScreens.Withdrawal.route) {
            WithdrawalRoute(onBackClick = { navController.popBackStack() })
        }
    }
}
