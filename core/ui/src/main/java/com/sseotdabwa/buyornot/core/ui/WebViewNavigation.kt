package com.sseotdabwa.buyornot.core.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.net.URLEncoder

const val WEBVIEW_ROUTE = "webview"

fun NavController.navigateToWebView(
    title: String,
    url: String,
) {
    val encodedUrl = URLEncoder.encode(url, "UTF-8")
    this.navigate("$WEBVIEW_ROUTE?title=$title&url=$encodedUrl")
}

fun NavGraphBuilder.webViewScreen(onBackClick: () -> Unit) {
    composable(
        route = "$WEBVIEW_ROUTE?title={title}&url={url}",
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
            onBackClick = onBackClick,
        )
    }
}
