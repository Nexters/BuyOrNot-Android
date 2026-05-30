package com.sseotdabwa.buyornot.feature.upload.navigation

import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.sseotdabwa.buyornot.core.ui.crop.navigateToEdit
import com.sseotdabwa.buyornot.feature.upload.ui.UploadIntent
import com.sseotdabwa.buyornot.feature.upload.ui.UploadViewModel
import kotlinx.serialization.Serializable
import com.sseotdabwa.buyornot.feature.upload.ui.UploadRoute as UploadRouteComposable

@Serializable
data object UploadRoute

fun NavController.navigateToUpload(navOptions: NavOptions? = null) {
    navigate(UploadRoute, navOptions)
}

fun NavGraphBuilder.uploadScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = {},
    onNavigateToHomeReview: () -> Unit = {},
) {
    composable<UploadRoute> { backStackEntry ->
        val viewModel = hiltViewModel<UploadViewModel>()

        val cropResult by backStackEntry.savedStateHandle
            .getStateFlow<String?>("editResult", null)
            .collectAsStateWithLifecycle()

        LaunchedEffect(cropResult) {
            val result = cropResult ?: return@LaunchedEffect
            backStackEntry.savedStateHandle.remove<String>("editResult")
            if (result == "SKIPPED") {
                viewModel.handleIntent(UploadIntent.CropSkipped)
            } else {
                viewModel.handleIntent(UploadIntent.CropConfirmed(Uri.parse(result)))
            }
        }

        UploadRouteComposable(
            onNavigateBack = onNavigateBack,
            onNavigateToHomeReview = onNavigateToHomeReview,
            onNavigateToCrop = { uri -> navController.navigateToEdit(uri) },
            viewModel = viewModel,
        )
    }
}
