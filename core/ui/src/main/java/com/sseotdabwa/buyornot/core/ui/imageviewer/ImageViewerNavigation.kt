package com.sseotdabwa.buyornot.core.ui.imageviewer

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val IMAGE_VIEWER_ROUTE = "image_viewer"

private const val KEY_IMAGE_URLS = "image_viewer_urls"
private const val KEY_INITIAL_PAGE = "image_viewer_initial_page"

fun NavController.navigateToImageViewer(
    imageUrls: List<String>,
    initialPage: Int = 0,
) {
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

        ImageViewerScreen(
            imageUrls = imageUrls,
            initialPage = initialPage,
            onBackClick = onBackClick,
        )
    }
}
