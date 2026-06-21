package com.sseotdabwa.buyornot.feature.upload.ui

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec
import com.sseotdabwa.buyornot.domain.model.FeedCategory

data class ImageEntry(
    val originalUri: Uri,
    val displayUri: Uri,
    val editSpec: EditSpec = EditSpec(),
)

data class UploadUiState(
    val isLoading: Boolean = false,
    val selectedImages: List<ImageEntry> = emptyList(),
    val cropQueue: List<Uri> = emptyList(),
    val currentCropOriginal: Uri? = null,
    val recropIndex: Int? = null,
    val category: FeedCategory? = null,
    val price: String = "",
    val priceFieldValue: TextFieldValue = TextFieldValue(""),
    val link: String = "",
    val title: String = "",
    val content: String = "",
    val showCategorySheet: Boolean = false,
    val showExitDialog: Boolean = false,
    val showPhotoPickerSheet: Boolean = false,
    val categories: List<FeedCategory> = FeedCategory.entries,
    val lastTouchedField: String? = null,
) {
    val selectedImageUris: List<Uri>
        get() = selectedImages.map { it.displayUri }

    val hasInput: Boolean
        get() =
            selectedImages.isNotEmpty() ||
                category != null ||
                price.isNotEmpty() ||
                link.isNotEmpty() ||
                title.isNotEmpty() ||
                content.isNotEmpty()

    val filledFields: List<String>
        get() =
            buildList {
                if (selectedImageUris.isNotEmpty()) add("images")
                if (category != null) add("category")
                if (price.isNotEmpty()) add("price")
                if (link.isNotEmpty()) add("link")
                if (title.isNotEmpty()) add("title")
                if (content.isNotEmpty()) add("content")
            }
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

    data class StartCropQueue(
        val uris: List<Uri>,
    ) : UploadIntent

    data class CropConfirmed(
        val croppedUri: Uri,
        val editSpec: EditSpec,
    ) : UploadIntent

    data object CropSkipped : UploadIntent

    data class StartReCrop(
        val index: Int,
    ) : UploadIntent

    data class RemoveImage(
        val index: Int,
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

    data class UpdatePhotoPickerSheetVisibility(
        val isVisible: Boolean,
    ) : UploadIntent

    data object NavigateBack : UploadIntent
}

sealed interface UploadSideEffect {
    data class ShowSnackbar(
        val message: String,
    ) : UploadSideEffect

    data class LaunchCrop(
        val uri: Uri,
        val editSpec: EditSpec = EditSpec(),
    ) : UploadSideEffect

    data object NavigateBack : UploadSideEffect

    data object NavigateToHomeReview : UploadSideEffect
}
