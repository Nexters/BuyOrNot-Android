package com.sseotdabwa.buyornot.core.ui.imageviewer

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data class ImageViewerRoute(
    val imageUrls: List<String>,
    val initialPage: Int = 0,
)

fun NavController.navigateToImageViewer(
    imageUrls: List<String>,
    initialPage: Int = 0,
) {
    navigate(ImageViewerRoute(imageUrls = imageUrls, initialPage = initialPage))
}

fun NavGraphBuilder.imageViewerScreen(onBackClick: () -> Unit) {
    composable<ImageViewerRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ImageViewerRoute>()
        ImageViewerScreen(
            imageUrls = route.imageUrls,
            initialPage = route.initialPage,
            onBackClick = onBackClick,
        )
    }
}
