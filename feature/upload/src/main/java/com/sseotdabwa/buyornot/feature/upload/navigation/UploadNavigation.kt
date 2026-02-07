package com.sseotdabwa.buyornot.feature.upload.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.sseotdabwa.buyornot.feature.upload.ui.UploadScreen

const val UPLOAD_ROUTE = "upload"

fun NavController.navigateToUpload(navOptions: NavOptions? = null) {
    navigate(UPLOAD_ROUTE, navOptions)
}

fun NavGraphBuilder.uploadScreen(onNavigateBack: () -> Unit = {}) {
    composable(route = UPLOAD_ROUTE) {
        UploadScreen(onNavigateBack = onNavigateBack)
    }
}
