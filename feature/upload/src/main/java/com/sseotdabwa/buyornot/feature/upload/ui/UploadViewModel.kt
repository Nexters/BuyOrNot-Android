package com.sseotdabwa.buyornot.feature.upload.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.FeedImage
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.feature.upload.util.LinkValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analytics: Analytics,
) : BaseViewModel<UploadUiState, UploadIntent, UploadSideEffect>(UploadUiState()) {
    init {
        viewModelScope.launch {
            val userType = userPreferencesRepository.userType.first()
            analytics.track(
                AnalyticsEvent.VoteCreateStarted(
                    entrySource = "home",
                    isLoggedIn = userType == UserType.SOCIAL,
                ),
            )
        }
    }

    companion object {
        private const val MAX_IMAGE_COUNT = 3
        private const val MAX_TITLE_LENGTH = 40
        private const val MAX_CONTENT_LENGTH = 100
    }

    override fun handleIntent(intent: UploadIntent) {
        when (intent) {
            is UploadIntent.UpdateCategory ->
                updateState {
                    it.copy(category = intent.category, showCategorySheet = false, lastTouchedField = "category")
                }
            is UploadIntent.UpdatePrice ->
                updateState {
                    it.copy(price = intent.digits, priceFieldValue = intent.textFieldValue, lastTouchedField = "price")
                }
            is UploadIntent.UpdateLink ->
                updateState { it.copy(link = intent.link, lastTouchedField = "link") }
            is UploadIntent.UpdateTitle -> {
                if (intent.title.length <= MAX_TITLE_LENGTH) {
                    updateState { it.copy(title = intent.title, lastTouchedField = "title") }
                }
            }
            is UploadIntent.UpdateContent -> {
                if (intent.content.length <= MAX_CONTENT_LENGTH) {
                    updateState { it.copy(content = intent.content, lastTouchedField = "content") }
                }
            }
            is UploadIntent.StartCropQueue -> startCropQueue(intent.uris)
            is UploadIntent.CropConfirmed -> onCropConfirmed(intent.croppedUri)
            is UploadIntent.CropSkipped -> onCropSkipped()
            is UploadIntent.StartReCrop -> startReCrop(intent.index)
            is UploadIntent.RemoveImage ->
                updateState {
                    it.copy(selectedImages = it.selectedImages.filterIndexed { i, _ -> i != intent.index })
                }
            is UploadIntent.Submit -> submitFeed(intent.context)
            is UploadIntent.UpdateCategorySheetVisibility ->
                updateState {
                    it.copy(showCategorySheet = intent.isVisible)
                }
            is UploadIntent.UpdateExitDialogVisibility ->
                updateState {
                    it.copy(showExitDialog = intent.isVisible)
                }
            is UploadIntent.UpdatePhotoPickerSheetVisibility ->
                updateState {
                    it.copy(showPhotoPickerSheet = intent.isVisible)
                }
            UploadIntent.NavigateBack -> {
                updateState {
                    it.copy(showExitDialog = false, showCategorySheet = false)
                }
                sendSideEffect(UploadSideEffect.NavigateBack)
                if (currentState.hasInput) {
                    analytics.track(
                        AnalyticsEvent.VoteCreateAbandoned(
                            filledFields = currentState.filledFields,
                            lastStep = currentState.lastTouchedField,
                        ),
                    )
                }
            }
        }
    }

    private fun startCropQueue(uris: List<Uri>) {
        val remaining = MAX_IMAGE_COUNT - currentState.selectedImages.size
        val queue = uris.take(remaining)
        val hasOverflow = queue.size < uris.size
        if (hasOverflow) sendSideEffect(UploadSideEffect.ShowSnackbar("최대 ${MAX_IMAGE_COUNT}장까지 추가할 수 있어요"))
        if (queue.isEmpty()) return
        val first = queue.first()
        updateState { it.copy(cropQueue = queue.drop(1), currentCropOriginal = first, recropIndex = null) }
        sendSideEffect(UploadSideEffect.LaunchCrop(first))
    }

    private fun onCropConfirmed(croppedUri: Uri) {
        val state = currentState
        if (state.recropIndex != null) {
            val idx = state.recropIndex
            if (idx !in state.selectedImages.indices) {
                updateState { it.copy(recropIndex = null, currentCropOriginal = null) }
                return
            }
            val original = state.selectedImages[idx].originalUri
            val updated =
                state.selectedImages.toMutableList().apply {
                    set(idx, ImageEntry(originalUri = original, displayUri = croppedUri))
                }
            updateState { it.copy(selectedImages = updated, recropIndex = null, currentCropOriginal = null) }
        } else {
            val original = state.currentCropOriginal ?: return
            val newEntry = ImageEntry(originalUri = original, displayUri = croppedUri)
            updateState { it.copy(selectedImages = it.selectedImages + newEntry) }
            advanceCropQueue()
        }
    }

    private fun onCropSkipped() {
        val state = currentState
        if (state.recropIndex != null) {
            updateState { it.copy(recropIndex = null, currentCropOriginal = null) }
            return
        }
        val original = state.currentCropOriginal ?: return
        val newEntry = ImageEntry(originalUri = original, displayUri = original)
        updateState { it.copy(selectedImages = it.selectedImages + newEntry) }
        advanceCropQueue()
    }

    private fun advanceCropQueue() {
        val queue = currentState.cropQueue
        if (queue.isEmpty()) {
            updateState { it.copy(currentCropOriginal = null) }
            return
        }
        val next = queue.first()
        updateState { it.copy(cropQueue = queue.drop(1), currentCropOriginal = next) }
        sendSideEffect(UploadSideEffect.LaunchCrop(next))
    }

    private fun startReCrop(index: Int) {
        val images = currentState.selectedImages
        if (index !in images.indices) return
        val source = images[index].displayUri
        updateState { it.copy(recropIndex = index, currentCropOriginal = source) }
        sendSideEffect(UploadSideEffect.LaunchCrop(source))
    }

    private fun submitFeed(context: Context) {
        if (currentState.isLoading) return

        val title = currentState.title.trim().takeIf { it.isNotBlank() }
        val link = currentState.link.trim().takeIf { it.isNotBlank() }

        if (!LinkValidator.isValid(link.orEmpty())) {
            sendSideEffect(UploadSideEffect.ShowSnackbar("링크 주소를 다시 확인해 주세요."))
            return
        }

        val uris = currentState.selectedImageUris
        if (uris.isEmpty()) return
        val category = currentState.category ?: return
        val price = currentState.price.toIntOrNull() ?: return
        val content = currentState.content

        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            val dimensionMap = uris.associateWith { getImageDimensions(context, it) }
            if (dimensionMap.values.any { (w, h) -> w <= 0 || h <= 0 }) {
                updateState { it.copy(isLoading = false) }
                sendSideEffect(UploadSideEffect.ShowSnackbar("이미지를 읽을 수 없습니다."))
                return@launch
            }

            runCatchingCancellable {
                val feedImages =
                    uris.map { uri ->
                        val (width, height) = dimensionMap.getValue(uri)
                        val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
                        val fileName = getFileName(context, uri) ?: "upload_image.jpg"
                        val uploadInfo = feedRepository.getPresignedUrl(fileName, contentType)
                        val bytes =
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                ?: throw Exception("파일을 읽을 수 없습니다.")
                        feedRepository.uploadImage(uploadInfo.uploadUrl, bytes, contentType)
                        FeedImage(
                            s3ObjectKey = uploadInfo.s3ObjectKey,
                            imageUrl = uploadInfo.viewUrl,
                            imageWidth = width,
                            imageHeight = height,
                        )
                    }

                feedRepository.createFeed(
                    category = category,
                    price = price,
                    content = content,
                    images = feedImages,
                    title = title,
                    link = link,
                )
            }.onSuccess { feedId ->
                updateState { it.copy(isLoading = false) }
                sendSideEffect(UploadSideEffect.NavigateToHomeReview)
                analytics.track(
                    AnalyticsEvent.VoteCreateCompleted(
                        itemId = feedId,
                        voteTitle = currentState.title,
                        optionCount = currentState.selectedImageUris.size,
                    ),
                )
            }.onFailure {
                updateState { it.copy(isLoading = false) }
                sendSideEffect(UploadSideEffect.ShowSnackbar("업로드에 실패했습니다."))
            }
        }
    }

    private fun getFileName(
        context: Context,
        uri: Uri,
    ): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) name = it.getString(nameIndex)
            }
        }
        return name
    }

    private fun getImageDimensions(
        context: Context,
        uri: Uri,
    ): Pair<Int, Int> =
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(inputStream, null, options)
                options.outWidth to options.outHeight
            } ?: (0 to 0)
        } catch (e: Exception) {
            0 to 0
        }
}
