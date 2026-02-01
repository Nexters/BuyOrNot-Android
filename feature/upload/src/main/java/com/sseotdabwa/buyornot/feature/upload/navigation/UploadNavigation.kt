package com.sseotdabwa.buyornot.feature.upload.navigation

import androidx.compose.material.navigation.bottomSheet
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.sseotdabwa.buyornot.feature.upload.ui.UploadScreen

const val UPLOAD_ROUTE = "upload"

fun NavController.navigateToUpload(navOptions: NavOptions? = null) {
    navigate(UPLOAD_ROUTE, navOptions)
}

fun NavGraphBuilder.uploadBottomSheet(onDismiss: () -> Unit = {}) {
    bottomSheet(route = UPLOAD_ROUTE) {
        UploadScreen(onDismiss = onDismiss)
    }
}
