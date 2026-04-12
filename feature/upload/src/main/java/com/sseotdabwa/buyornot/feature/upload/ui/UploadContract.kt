package com.sseotdabwa.buyornot.feature.upload.ui

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import com.sseotdabwa.buyornot.domain.model.FeedCategory

data class UploadUiState(
    val isLoading: Boolean = false,
    val selectedImageUris: List<Uri> = emptyList(),
    val category: FeedCategory? = null,
    val price: String = "",
    val priceFieldValue: TextFieldValue = TextFieldValue(""),
    val link: String = "",
    val title: String = "",
    val content: String = "",
    val showCategorySheet: Boolean = false,
    val showExitDialog: Boolean = false,
    val categories: List<FeedCategory> = FeedCategory.entries,
) {
    val hasInput: Boolean
        get() =
            selectedImageUris.isNotEmpty() ||
                category != null ||
                price.isNotEmpty() ||
                link.isNotEmpty() ||
                title.isNotEmpty() ||
                content.isNotEmpty()
}

sealed interface UploadIntent {
    data class UpdateCategory(
        val category: FeedCategory,
    ) : UploadIntent

    data class UpdatePrice(
        val digits: String,
        val textFieldValue: TextFieldValue,
    ) : UploadIntent

    data class UpdateLink(
        val link: String,
    ) : UploadIntent

    data class UpdateTitle(
        val title: String,
    ) : UploadIntent

    data class UpdateContent(
        val content: String,
    ) : UploadIntent

    data class AddImage(
        val uri: Uri,
    ) : UploadIntent

    data class RemoveImage(
        val uri: Uri,
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

    data object NavigateBack : UploadIntent
}

sealed interface UploadSideEffect {
    data class ShowSnackbar(
        val message: String,
    ) : UploadSideEffect

    data object NavigateBack : UploadSideEffect

    data object NavigateToHomeReview : UploadSideEffect
}
