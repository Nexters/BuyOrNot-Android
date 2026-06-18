package com.sseotdabwa.buyornot.core.ui.crop

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.sseotdabwa.buyornot.core.ui.crop.state.AspectRatio
import com.sseotdabwa.buyornot.core.ui.crop.state.CropSpec
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec
import com.sseotdabwa.buyornot.core.ui.crop.state.NormalizedRect
import kotlinx.serialization.Serializable

const val EDIT_RESULT_KEY = "editResult"
const val EDIT_RESULT_SPEC_KEY = "editResultSpec"
const val EDIT_RESULT_SKIPPED = "SKIPPED"

@Serializable
data class EditRoute(
    val encodedUri: String,
    val editSpecArg: String = "",
)

fun NavController.navigateToEdit(
    uri: Uri,
    editSpec: EditSpec = EditSpec(),
) {
    navigate(EditRoute(encodedUri = uri.toString(), editSpecArg = editSpec.encodeToArg()))
}

fun NavGraphBuilder.editScreen(navController: NavController) {
    composable<EditRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<EditRoute>()
        val imageUri = Uri.parse(route.encodedUri)
        EditScreen(
            imageUri = imageUri,
            initialSpec = decodeEditSpecArg(route.editSpecArg),
            onConfirm = { editedUri, editSpec ->
                navController.previousBackStackEntry?.savedStateHandle?.apply {
                    set(EDIT_RESULT_KEY, editedUri.toString())
                    set(EDIT_RESULT_SPEC_KEY, editSpec.encodeToArg())
                }
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

/**
 * EditSpec을 네비게이션 인자로 실어 보내기 위한 경량 문자열 인코딩.
 * 형식: "rotationQuarters[;ratioName;left;top;right;bottom]" (crop 없으면 회전값만).
 */
fun EditSpec.encodeToArg(): String =
    buildString {
        append(rotationQuarters)
        crop?.let { c ->
            append(SPEC_DELIMITER)
            append(c.ratio.name)
            append(SPEC_DELIMITER).append(c.rectNormalized.left)
            append(SPEC_DELIMITER).append(c.rectNormalized.top)
            append(SPEC_DELIMITER).append(c.rectNormalized.right)
            append(SPEC_DELIMITER).append(c.rectNormalized.bottom)
        }
    }

fun decodeEditSpecArg(arg: String?): EditSpec {
    if (arg.isNullOrEmpty()) return EditSpec()
    val parts = arg.split(SPEC_DELIMITER)
    val quarters = parts.getOrNull(0)?.toIntOrNull() ?: 0
    if (parts.size < 6) return EditSpec(rotationQuarters = quarters, crop = null)
    val ratio = runCatching { AspectRatio.valueOf(parts[1]) }.getOrDefault(AspectRatio.Free)
    val rect =
        runCatching {
            NormalizedRect(
                left = parts[2].toFloat(),
                top = parts[3].toFloat(),
                right = parts[4].toFloat(),
                bottom = parts[5].toFloat(),
            )
        }.getOrNull() ?: return EditSpec(rotationQuarters = quarters, crop = null)
    return EditSpec(rotationQuarters = quarters, crop = CropSpec(ratio, rect))
}

private const val SPEC_DELIMITER = ";"
