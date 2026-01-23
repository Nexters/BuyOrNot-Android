package com.sseotdabwa.buyornot.feature.upload.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sseotdabwa.buyornot.feature.upload.ui.UploadRoute

const val UPLOAD_ROUTE = "upload"

fun NavGraphBuilder.uploadScreen() {
    composable(route = UPLOAD_ROUTE) {
        UploadRoute()
    }
}
