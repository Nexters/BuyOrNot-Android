package com.sseotdabwa.buyornot.core.ui.crop

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

const val EDIT_RESULT_KEY = "editResult"
const val EDIT_RESULT_SKIPPED = "SKIPPED"

@Serializable
data class EditRoute(
    val encodedUri: String,
)

fun NavController.navigateToEdit(uri: Uri) {
    navigate(EditRoute(encodedUri = uri.toString()))
}

fun NavGraphBuilder.editScreen(navController: NavController) {
    composable<EditRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EditRoute>()
        val imageUri = Uri.parse(route.encodedUri)
        EditScreen(
            imageUri = imageUri,
            onConfirm = { editedUri ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(EDIT_RESULT_KEY, editedUri.toString())
                navController.popBackStack()
            },
            onCancel = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(EDIT_RESULT_KEY, EDIT_RESULT_SKIPPED)
                navController.popBackStack()
            },
        )
    }
}
