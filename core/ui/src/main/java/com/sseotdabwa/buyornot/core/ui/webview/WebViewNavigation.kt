package com.sseotdabwa.buyornot.core.ui.webview

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.net.URLEncoder

private const val WEBVIEW_ROUTE = "webview"

internal const val TERMS_URL = "https://littlemoom.notion.site/buy-or-not-service-term?pvs=143"
internal const val PRIVACY_URL = "https://littlemoom.notion.site/buy-or-not-privacy-term?pvs=143"
internal const val FEEDBACK_URL = "https://docs.google.com/forms/d/e/1FAIpQLScG0GStvzog1HVZjAP9OpHl85azcez2OdAr7YwrI7rvCqInsg/viewform"

fun NavController.navigateToWebView(
    title: String,
    url: String,
) {
    val encodedUrl = URLEncoder.encode(url, "UTF-8")
    this.navigate("$WEBVIEW_ROUTE?title=$title&url=$encodedUrl")
}

fun NavController.navigateToTerms() {
    navigateToWebView("서비스 약관", TERMS_URL)
}

fun NavController.navigateToPrivacyPolicy() {
    navigateToWebView("개인정보처리방침", PRIVACY_URL)
}

fun NavController.navigateToFeedBack() {
    navigateToWebView("의견 남기기", FEEDBACK_URL)
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
