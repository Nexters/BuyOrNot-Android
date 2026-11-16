package com.sseotdabwa.buyornot.feature.upload.ui

import android.net.Uri
import android.os.SystemClock
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec
import com.sseotdabwa.buyornot.domain.model.FeedAiDraft
import com.sseotdabwa.buyornot.domain.model.FeedImage
import com.sseotdabwa.buyornot.domain.model.MAX_FEED_IMAGE_COUNT
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.feature.upload.upload.FeedImageUploader
import com.sseotdabwa.buyornot.feature.upload.upload.UnreadableImageException
import com.sseotdabwa.buyornot.feature.upload.util.LinkValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analytics: Analytics,
    private val feedImageUploader: FeedImageUploader,
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

    // AI 초안을 만들려고 미리 올린 사진. 게시할 때 같은 사진을 다시 올리지 않으려고 화면에 보이는 Uri로 찾는다.
    private val uploadedImages = mutableMapOf<Uri, FeedImage>()

    private var aiWritingJob: Job? = null

    // 이탈·완료 이벤트에 붙일 작성 방식. 앨범에서 확인하기 전에는 정해지지 않는다.
    private var writingMode: String? = null

    // 게시 때 사용자가 AI 초안의 어느 필드를 고쳤는지 비교하려고, 초안이 채운 값을 필드 이름으로 들고 있다.
    private var aiFilledValues: Map<String, String> = emptyMap()
    private var aiStartedAtMillis = 0L

    companion object {
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
            is UploadIntent.ConfirmAlbum -> confirmAlbum(intent.uris, intent.useAi)
            UploadIntent.CancelAiWriting -> cancelAiWriting()
            is UploadIntent.LeaveAlbum -> leaveAlbum(intent.selectedCount)
            is UploadIntent.StartCropQueue -> startCropQueue(intent.uris)
            is UploadIntent.CropConfirmed -> onCropConfirmed(intent.croppedUri, intent.editSpec)
            is UploadIntent.CropSkipped -> onCropSkipped()
            is UploadIntent.StartReCrop -> startReCrop(intent.index)
            is UploadIntent.RemoveImage ->
                updateState {
                    it.copy(selectedImages = it.selectedImages.filterIndexed { i, _ -> i != intent.index })
                }
            UploadIntent.Submit -> submitFeed()
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
                            step = AnalyticsEvent.VoteCreateAbandoned.Step.FORM,
                            writingMode = writingMode,
                        ),
                    )
                }
            }
        }
    }

    private fun confirmAlbum(
        uris: List<Uri>,
        useAi: Boolean,
    ) {
        if (uris.isEmpty() || currentState.aiWriting != null) return
        val images = uris.take(MAX_FEED_IMAGE_COUNT).map { ImageEntry(originalUri = it, displayUri = it) }
        analytics.track(AnalyticsEvent.VoteCreatePhotosConfirmed(imageCount = images.size, useAi = useAi))
        if (!useAi) {
            writingMode = AnalyticsEvent.WritingMode.MANUAL
            updateState { it.copy(step = UploadStep.FORM, selectedImages = images) }
            return
        }
        aiStartedAtMillis = SystemClock.elapsedRealtime()
        updateState {
            it.copy(selectedImages = images, aiWriting = AiWriting(imageCount = images.size, phase = AiWritingPhase.UPLOADING))
        }
        aiWritingJob =
            viewModelScope.launch {
                runCatchingCancellable {
                    val uploaded = images.map { uploadOnce(it.displayUri) }
                    updateState { it.copy(aiWriting = it.aiWriting?.copy(phase = AiWritingPhase.DRAFTING)) }
                    feedRepository.createAiDraft(uploaded.map { it.s3ObjectKey })
                }.onSuccess { draft ->
                    // 제목과 내용이 모두 없으면 서버가 초안을 만들지 못한 것이다.
                    val usable = draft.takeIf { it.title != null || it.content != null }
                    finishAiWriting(draft = usable)
                    trackAiDraft(
                        result =
                            if (usable !=
                                null
                            ) {
                                AnalyticsEvent.AiDraftCompleted.Result.SUCCESS
                            } else {
                                AnalyticsEvent.AiDraftCompleted.Result.EMPTY
                            },
                        imageCount = images.size,
                    )
                }.onFailure { e ->
                    Timber.e(e, "Failed to create AI draft")
                    finishAiWriting(draft = null)
                    trackAiDraft(result = AnalyticsEvent.AiDraftCompleted.Result.FAILED, imageCount = images.size)
                }
            }
    }

    // 이미 올린 사진은 다시 확인하면 그대로 쓴다.
    private fun cancelAiWriting() {
        val aiWriting = currentState.aiWriting ?: return
        aiWritingJob?.cancel()
        aiWritingJob = null
        writingMode = null
        updateState { it.copy(aiWriting = null, selectedImages = emptyList()) }
        trackAiDraft(result = AnalyticsEvent.AiDraftCompleted.Result.CANCELLED, imageCount = aiWriting.imageCount)
    }

    private fun trackAiDraft(
        result: String,
        imageCount: Int,
    ) {
        analytics.track(
            AnalyticsEvent.AiDraftCompleted(
                result = result,
                imageCount = imageCount,
                durationMs = SystemClock.elapsedRealtime() - aiStartedAtMillis,
                filledFields = aiFilledValues.keys.toList().takeIf { result == AnalyticsEvent.AiDraftCompleted.Result.SUCCESS },
            ),
        )
    }

    // 사진을 고르지 않고 나가면 입력이 없는 이탈이라 기존 규칙대로 세지 않는다.
    private fun leaveAlbum(selectedCount: Int) {
        sendSideEffect(UploadSideEffect.NavigateBack)
        if (selectedCount == 0) return
        analytics.track(
            AnalyticsEvent.VoteCreateAbandoned(
                filledFields = listOf("images"),
                lastStep = null,
                step = AnalyticsEvent.VoteCreateAbandoned.Step.ALBUM,
                writingMode = null,
            ),
        )
    }

    // 초안이 없으면 사진만 둔 채 직접 쓰도록 안내한다.
    private fun finishAiWriting(draft: FeedAiDraft?) {
        updateState { state ->
            val filled =
                draft?.let {
                    // 게시할 때 Int로 보내므로 그 범위를 넘는 가격은 채우지 않는다.
                    val price = it.price?.takeIf { value -> value in 1..Int.MAX_VALUE }?.toString()
                    state.copy(
                        title = it.title?.take(MAX_TITLE_LENGTH) ?: state.title,
                        content = it.content?.take(MAX_CONTENT_LENGTH) ?: state.content,
                        category = it.category ?: state.category,
                        price = price ?: state.price,
                        priceFieldValue =
                            price?.let { digits ->
                                val formatted = DecimalFormat("#,###").format(digits.toLong())
                                TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                            } ?: state.priceFieldValue,
                    )
                } ?: state
            filled.copy(step = UploadStep.FORM, aiWriting = null)
        }
        writingMode = if (draft != null) AnalyticsEvent.WritingMode.AI else AnalyticsEvent.WritingMode.AI_FAILED
        aiFilledValues =
            if (draft == null) {
                emptyMap()
            } else {
                currentState.let { state ->
                    buildMap {
                        if (draft.title != null) put("title", state.title)
                        if (draft.content != null) put("content", state.content)
                        if (draft.category != null) state.category?.let { put("category", it.name) }
                        if (state.price.isNotEmpty() && draft.price != null) put("price", state.price)
                    }
                }
            }
        sendSideEffect(
            if (draft != null) {
                UploadSideEffect.ShowSnackbar("AI로 글 작성을 완료했어요!", icon = BuyOrNotIcons.CheckCircle)
            } else {
                UploadSideEffect.ShowSnackbar("AI 글 작성을 실패했어요. 내용을 입력해 주세요.")
            },
        )
    }

    private fun startCropQueue(uris: List<Uri>) {
        val remaining = MAX_FEED_IMAGE_COUNT - currentState.selectedImages.size
        val queue = uris.take(remaining)
        val hasOverflow = queue.size < uris.size
        if (hasOverflow) sendSideEffect(UploadSideEffect.ShowSnackbar("최대 ${MAX_FEED_IMAGE_COUNT}장까지 추가할 수 있어요"))
        if (queue.isEmpty()) return
        val first = queue.first()
        updateState { it.copy(cropQueue = queue.drop(1), currentCropOriginal = first, recropIndex = null) }
        sendSideEffect(UploadSideEffect.LaunchCrop(first))
    }

    private fun onCropConfirmed(
        croppedUri: Uri,
        editSpec: EditSpec,
    ) {
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
                    set(idx, ImageEntry(originalUri = original, displayUri = croppedUri, editSpec = editSpec))
                }
            updateState { it.copy(selectedImages = updated, recropIndex = null, currentCropOriginal = null) }
        } else {
            val original = state.currentCropOriginal ?: return
            val newEntry = ImageEntry(originalUri = original, displayUri = croppedUri, editSpec = editSpec)
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
        val newEntry = ImageEntry(originalUri = original, displayUri = original, editSpec = EditSpec())
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
        val entry = images[index]
        updateState { it.copy(recropIndex = index, currentCropOriginal = entry.originalUri) }
        sendSideEffect(UploadSideEffect.LaunchCrop(entry.originalUri, entry.editSpec))
    }

    private fun submitFeed() {
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

            runCatchingCancellable {
                val feedImages = uris.map { uploadOnce(it) }

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
                        writingMode = writingMode ?: AnalyticsEvent.WritingMode.MANUAL,
                        aiEditedFields = aiEditedFields().takeIf { writingMode == AnalyticsEvent.WritingMode.AI },
                    ),
                )
            }.onFailure { e ->
                updateState { it.copy(isLoading = false) }
                val message = if (e is UnreadableImageException) "이미지를 읽을 수 없습니다." else "업로드에 실패했습니다."
                sendSideEffect(UploadSideEffect.ShowSnackbar(message))
            }
        }
    }

    private fun aiEditedFields(): List<String> {
        val state = currentState
        val current =
            mapOf(
                "title" to state.title,
                "content" to state.content,
                "category" to state.category?.name.orEmpty(),
                "price" to state.price,
            )
        return aiFilledValues.filter { (field, value) -> current[field] != value }.keys.toList()
    }

    private suspend fun uploadOnce(uri: Uri): FeedImage =
        uploadedImages[uri] ?: feedImageUploader.upload(uri).also { uploadedImages[uri] = it }
}
