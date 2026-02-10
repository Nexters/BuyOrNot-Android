package com.sseotdabwa.buyornot.feature.mypage.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.sseotdabwa.buyornot.feature.mypage.ui.AccountSettingRoute
import com.sseotdabwa.buyornot.feature.mypage.ui.MyPageRoute
import com.sseotdabwa.buyornot.feature.mypage.ui.PolicyRoute
import com.sseotdabwa.buyornot.feature.mypage.ui.WebViewRoute
import com.sseotdabwa.buyornot.feature.mypage.ui.WithdrawalRoute
import java.net.URLDecoder
import java.net.URLEncoder

sealed class MyPageScreens(
    val route: String,
) {
    object Graph : MyPageScreens("mypage_graph")

    object Main : MyPageScreens("mypage_main")

    object AccountSetting : MyPageScreens("account_setting")

    object Policy : MyPageScreens("policy")

    object WebView : MyPageScreens("webview")

    object Withdrawal : MyPageScreens("withdrawal")
}

fun NavController.navigateToWebView(
    title: String,
    url: String,
) {
    val encodedUrl = URLEncoder.encode(url, "UTF-8")
    this.navigate("${MyPageScreens.WebView.route}?title=$title&url=$encodedUrl")
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
    navController: NavController,
    versionName: String,
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
                onWithdrawalClick = navController::navigateToWithdrawal,
            )
        }

        composable(MyPageScreens.Policy.route) {
            PolicyRoute(
                onBackClick = navController::popBackStack,
                onNavigateToWebView = { title, url ->
                    navController.navigateToWebView(title, url)
                },
            )
        }

        composable(
            route = "${MyPageScreens.WebView.route}?title={title}&url={url}",
            arguments =
            listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("url") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            val url = backStackEntry.arguments?.getString("url") ?: ""
            WebViewRoute(
                title = title,
                url = URLDecoder.decode(url, "UTF-8"),
                onBackClick = navController::popBackStack,
            )
        }

        composable(MyPageScreens.Withdrawal.route) {
            WithdrawalRoute(
                onBackClick = navController::popBackStack,
            )
        }
    }
}
