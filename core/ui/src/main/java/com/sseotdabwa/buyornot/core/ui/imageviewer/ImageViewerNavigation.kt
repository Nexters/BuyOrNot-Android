package com.sseotdabwa.buyornot.core.ui.imageviewer

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val IMAGE_VIEWER_ROUTE = "image_viewer"

private const val KEY_IMAGE_URLS = "image_viewer_urls"
private const val KEY_INITIAL_PAGE = "image_viewer_initial_page"
private const val TAG = "ImageViewer"

fun NavController.navigateToImageViewer(
    imageUrls: List<String>,
    initialPage: Int = 0,
) {
    Log.d(TAG, "navigateToImageViewer: urls=${imageUrls.size}개, page=$initialPage")
    imageUrls.forEachIndexed { i, url -> Log.d(TAG, "  [$i] $url") }

    // navigate 먼저 → currentBackStackEntry가 image_viewer로 바뀐 뒤 set
    navigate(IMAGE_VIEWER_ROUTE)
    currentBackStackEntry?.savedStateHandle?.apply {
        set(KEY_IMAGE_URLS, imageUrls)
        set(KEY_INITIAL_PAGE, initialPage)
    }
}

fun NavGraphBuilder.imageViewerScreen(onBackClick: () -> Unit) {
    composable(route = IMAGE_VIEWER_ROUTE) { backStackEntry ->
        val savedStateHandle = backStackEntry.savedStateHandle
        val imageUrls = savedStateHandle.get<List<String>>(KEY_IMAGE_URLS) ?: emptyList()
        val initialPage = savedStateHandle.get<Int>(KEY_INITIAL_PAGE) ?: 0

        Log.d(TAG, "imageViewerScreen: urls=${imageUrls.size}개, page=$initialPage")
        imageUrls.forEachIndexed { i, url -> Log.d(TAG, "  [$i] $url") }

        ImageViewerScreen(
            imageUrls = imageUrls,
            initialPage = initialPage,
            onBackClick = onBackClick,
        )
    }
}
