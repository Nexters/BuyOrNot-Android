package com.sseotdabwa.buyornot.core.ui

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle

@Composable
fun WebViewRoute(
    title: String,
    url: String,
    onBackClick: () -> Unit,
) {
    WebViewScreen(title = title, url = url, onBackClick = onBackClick)
}

@Composable
fun WebViewScreen(
    title: String,
    url: String,
    onBackClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        BackTopBarWithTitle(
            title = title,
            onBackClick = onBackClick,
        )
        AndroidView(
            factory = {
                WebView(it).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.setSupportMultipleWindows(false)
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    loadUrl(url)
                }
            },
            update = { webView ->
                if (webView.url != url) {
                    webView.loadUrl(url)
                }
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.destroy()
            },
        )
    }
}
