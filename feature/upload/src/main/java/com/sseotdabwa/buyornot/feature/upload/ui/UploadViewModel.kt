package com.sseotdabwa.buyornot.feature.upload.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
) : BaseViewModel<UploadUiState, UploadIntent, UploadSideEffect>(UploadUiState()) {
    override fun handleIntent(intent: UploadIntent) {
        when (intent) {
            is UploadIntent.UpdateCategory -> updateState { it.copy(category = intent.category) }
            is UploadIntent.UpdatePrice -> updateState { it.copy(price = intent.price) }
            is UploadIntent.UpdateContent -> updateState { it.copy(content = intent.content) }
            is UploadIntent.SelectImage -> updateState { it.copy(selectedImageUri = intent.uri) }
            is UploadIntent.Submit -> submitFeed(intent.context)
        }
    }

    private fun submitFeed(context: Context) {
        val uri = currentState.selectedImageUri ?: return
        val category = currentState.category ?: return
        val price = currentState.price.toIntOrNull() ?: 0
        val content = currentState.content

        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            runCatchingCancellable {
                // 0. Get image dimensions
                val (width, height) = getImageDimensions(context, uri)

                // 1. Get file name and content type
                val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val fileName = getFileName(context, uri) ?: "upload_image.jpg"

                // 2. Get Presigned URL
                val uploadInfo = feedRepository.getPresignedUrl(fileName, contentType)

                // 3. Upload to S3
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: throw Exception("파일을 읽을 수 없습니다.")
                inputStream.close()
                feedRepository.uploadImage(uploadInfo.uploadUrl, bytes, contentType)

                // 4. Create Feed
                feedRepository.createFeed(
                    category = category,
                    price = price,
                    content = content,
                    s3ObjectKey = uploadInfo.s3ObjectKey,
                    imageWidth = width,
                    imageHeight = height,
                )
            }.onSuccess {
                updateState { it.copy(isLoading = false) }
                sendSideEffect(UploadSideEffect.NavigateBack)
            }.onFailure { throwable ->
                updateState { it.copy(isLoading = false) }
                Log.d("UploadViewModel", "${throwable.message} ${throwable.cause}")
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
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
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
                val options =
                    BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                BitmapFactory.decodeStream(inputStream, null, options)
                options.outWidth to options.outHeight
            } ?: (0 to 0)
        } catch (e: Exception) {
            0 to 0
        }
}
