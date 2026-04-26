package com.sseotdabwa.buyornot.core.ui.imageviewer

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
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
    dialog<ImageViewerRoute>(
        dialogProperties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnClickOutside = false,
            ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<ImageViewerRoute>()
        ImageViewerScreen(
            imageUrls = route.imageUrls,
            initialPage = route.initialPage,
            onBackClick = onBackClick,
        )
    }
}
