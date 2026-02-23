package com.sseotdabwa.buyornot.feature.upload.ui

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import com.sseotdabwa.buyornot.domain.model.FeedCategory

data class UploadUiState(
    val isLoading: Boolean = false,
    val selectedImageUri: Uri? = null,
    val category: FeedCategory? = null,
    val price: String = "",
    val priceFieldValue: TextFieldValue = TextFieldValue(""),
    val content: String = "",
    val showCategorySheet: Boolean = false,
    val showExitDialog: Boolean = false,
    val categories: List<FeedCategory> = FeedCategory.entries,
) {
    val hasInput: Boolean
        get() = selectedImageUri != null || category != null || price.isNotEmpty() || content.isNotEmpty()
}

sealed interface UploadIntent {
    data class UpdateCategory(
        val category: FeedCategory,
    ) : UploadIntent

    data class UpdatePrice(
        val digits: String,
        val textFieldValue: TextFieldValue,
    ) : UploadIntent

    data class UpdateContent(
        val content: String,
    ) : UploadIntent

    data class SelectImage(
        val uri: Uri?,
    ) : UploadIntent

    data class Submit(
        val context: Context,
    ) : UploadIntent

    data class UpdateCategorySheetVisibility(
        val isVisible: Boolean,
    ) : UploadIntent

    data class UpdateExitDialogVisibility(
        val isVisible: Boolean,
    ) : UploadIntent
}

sealed interface UploadSideEffect {
    data class ShowSnackbar(
        val message: String,
    ) : UploadSideEffect

    data object NavigateBack : UploadSideEffect

    data object NavigateToHomeReview : UploadSideEffect
}
