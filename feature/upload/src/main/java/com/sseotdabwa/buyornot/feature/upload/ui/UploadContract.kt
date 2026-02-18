package com.sseotdabwa.buyornot.feature.upload.ui

import android.content.Context
import android.net.Uri
import com.sseotdabwa.buyornot.domain.model.FeedCategory

data class UploadUiState(
    val isLoading: Boolean = false,
    val selectedImageUri: Uri? = null,
    val category: FeedCategory? = null,
    val price: String = "",
    val content: String = "",
)

sealed interface UploadIntent {
    data class UpdateCategory(
        val category: FeedCategory,
    ) : UploadIntent

    data class UpdatePrice(
        val price: String,
    ) : UploadIntent

    data class UpdateContent(
        val content: String,
    ) : UploadIntent

    data class SelectImage(
        val uri: Uri?,
    ) : UploadIntent

    data class Submit(
        val context: Context,
    ) : UploadIntent // Context 전달
}

sealed interface UploadSideEffect {
    data class ShowSnackbar(
        val message: String,
    ) : UploadSideEffect

    data object NavigateBack : UploadSideEffect
}
