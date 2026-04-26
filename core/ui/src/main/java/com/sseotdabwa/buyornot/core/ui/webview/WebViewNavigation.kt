package com.sseotdabwa.buyornot.core.ui.webview

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

internal const val TERMS_URL = "https://littlemoom.notion.site/buy-or-not-service-term?pvs=143"
internal const val PRIVACY_URL = "https://littlemoom.notion.site/buy-or-not-privacy-term?pvs=143"
internal const val FEEDBACK_URL = "https://docs.google.com/forms/d/e/1FAIpQLScG0GStvzog1HVZjAP9OpHl85azcez2OdAr7YwrI7rvCqInsg/viewform"

@Serializable
data class WebViewRoute(
    val title: String,
    val url: String,
)

fun NavController.navigateToWebView(
    title: String,
    url: String,
) {
    navigate(WebViewRoute(title = title, url = url))
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
    composable<WebViewRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<WebViewRoute>()
        WebViewScreen(
            title = route.title,
            url = route.url,
            onBackClick = onBackClick,
        )
    }
}
