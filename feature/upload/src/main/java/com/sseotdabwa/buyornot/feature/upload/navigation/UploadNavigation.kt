package com.sseotdabwa.buyornot.feature.upload.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import com.sseotdabwa.buyornot.feature.upload.ui.UploadRoute as UploadScreen

@Serializable
data object UploadRoute

fun NavController.navigateToUpload(navOptions: NavOptions? = null) {
    navigate(UploadRoute, navOptions)
}

fun NavGraphBuilder.uploadScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHomeReview: () -> Unit = {},
) {
    composable<UploadRoute> {
        UploadScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToHomeReview = onNavigateToHomeReview,
        )
    }
}
