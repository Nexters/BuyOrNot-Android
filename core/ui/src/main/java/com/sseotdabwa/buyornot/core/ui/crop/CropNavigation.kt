package com.sseotdabwa.buyornot.core.ui.crop

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
data class CropRoute(
    val encodedUri: String,
)

fun NavController.navigateToCrop(uri: Uri) {
    navigate(CropRoute(encodedUri = uri.toString()))
}

fun NavGraphBuilder.cropScreen(navController: NavController) {
    composable<CropRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<CropRoute>()
        val imageUri = Uri.parse(route.encodedUri)
        CropScreen(
            imageUri = imageUri,
            onConfirm = { croppedUri ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("cropResult", croppedUri.toString())
                navController.popBackStack()
            },
            onCancel = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("cropResult", "SKIPPED")
                navController.popBackStack()
            },
        )
    }
}
