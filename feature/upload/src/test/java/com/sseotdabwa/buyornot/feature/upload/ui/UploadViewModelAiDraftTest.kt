package com.sseotdabwa.buyornot.feature.upload.ui

import android.net.Uri
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.domain.model.FeedAiDraft
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
// android.net.Uri가 필요해 Robolectric으로 돌리고, 로컬 JDK 17에서도 뜨는 SDK로 고정한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UploadViewModelAiDraftTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val feedRepository = FakeFeedRepository()
    private val uploader = FakeFeedImageUploader()
    private val photos = listOf(Uri.parse("content://media/1"), Uri.parse("content://media/2"))

    private fun createViewModel() =
        UploadViewModel(
            feedRepository = feedRepository,
            userPreferencesRepository = FakeUserPreferencesRepository(),
            analytics = FakeAnalytics(),
            feedImageUploader = uploader,
        )

    @Test
    fun `AI로_글쓰기를_켜고_확인하면_올린_사진_key로_초안을_받아_등록_화면을_채우고_완료를_알린다`() =
        runTest {
            feedRepository.aiDraft =
                FeedAiDraft(title = "가습기 살까요", content = "방이 건조해요", category = FeedCategory.INTERIOR, price = 29_000)
            val viewModel = createViewModel()

            viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

            val state = viewModel.uiState.value
            assertEquals(listOf(listOf("key-content://media/1", "key-content://media/2")), feedRepository.aiDraftRequests)
            assertEquals(UploadStep.FORM, state.step)
            assertNull(state.aiWriting)
            assertEquals(photos, state.selectedImageUris)
            assertEquals("가습기 살까요", state.title)
            assertEquals("방이 건조해요", state.content)
            assertEquals(FeedCategory.INTERIOR, state.category)
            assertEquals("29000", state.price)
            assertEquals("29,000", state.priceFieldValue.text)
            assertEquals(
                UploadSideEffect.ShowSnackbar("AI로 글 작성을 완료했어요!", icon = BuyOrNotIcons.CheckCircle),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `사진에서_가격을_찾지_못하면_가격은_비워_둔다`() {
        feedRepository.aiDraft = FeedAiDraft(title = "제목", content = "내용", category = FeedCategory.ETC, price = null)
        val viewModel = createViewModel()

        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

        val state = viewModel.uiState.value
        assertEquals("", state.price)
        assertEquals("", state.priceFieldValue.text)
    }

    @Test
    fun `게시할_수_없는_범위의_가격은_채우지_않는다`() {
        feedRepository.aiDraft = FeedAiDraft(title = "제목", content = "내용", category = FeedCategory.ETC, price = 3_000_000_000)
        val viewModel = createViewModel()

        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

        assertEquals("", viewModel.uiState.value.price)
        assertEquals("제목", viewModel.uiState.value.title)
    }

    @Test
    fun `초안의_제목과_내용이_모두_없으면_사진만_두고_실패를_알린다`() =
        runTest {
            feedRepository.aiDraft = FeedAiDraft(title = null, content = null, category = FeedCategory.FOOD, price = 5_000)
            val viewModel = createViewModel()

            viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

            val state = viewModel.uiState.value
            assertEquals(UploadStep.FORM, state.step)
            assertEquals(photos, state.selectedImageUris)
            assertNull(state.category)
            assertEquals("", state.price)
            assertEquals(
                UploadSideEffect.ShowSnackbar("AI 글 작성을 실패했어요. 내용을 입력해 주세요."),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `초안_요청이_실패하면_사진만_두고_실패를_알린다`() =
        runTest {
            feedRepository.aiDraftError = IllegalStateException("FEED_017")
            val viewModel = createViewModel()

            viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

            val state = viewModel.uiState.value
            assertEquals(UploadStep.FORM, state.step)
            assertNull(state.aiWriting)
            assertEquals(photos, state.selectedImageUris)
            assertEquals("", state.title)
            assertEquals(
                UploadSideEffect.ShowSnackbar("AI 글 작성을 실패했어요. 내용을 입력해 주세요."),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `초안_응답을_기다리는_동안은_작성_단계로_막아_둔다`() {
        val gate = CompletableDeferred<Unit>()
        feedRepository.beforeAiDraft = { gate.await() }
        feedRepository.aiDraft = FeedAiDraft(title = "제목", content = null, category = null, price = null)
        val viewModel = createViewModel()

        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

        val waiting = viewModel.uiState.value
        assertEquals(UploadStep.ALBUM, waiting.step)
        assertEquals(AiWriting(imageCount = 2, phase = AiWritingPhase.DRAFTING), waiting.aiWriting)

        gate.complete(Unit)

        assertEquals(UploadStep.FORM, viewModel.uiState.value.step)
        assertEquals("제목", viewModel.uiState.value.title)
    }

    @Test
    fun `AI_글_작성을_취소하면_앨범에_남고_이미_올린_사진은_다시_확인할_때_재사용한다`() {
        val gate = CompletableDeferred<Unit>()
        feedRepository.beforeAiDraft = { gate.await() }
        val viewModel = createViewModel()
        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

        viewModel.handleIntent(UploadIntent.CancelAiWriting)

        val cancelled = viewModel.uiState.value
        assertEquals(UploadStep.ALBUM, cancelled.step)
        assertNull(cancelled.aiWriting)
        assertTrue(cancelled.selectedImages.isEmpty())

        feedRepository.beforeAiDraft = {}
        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

        assertEquals(photos, uploader.uploaded)
        assertEquals(UploadStep.FORM, viewModel.uiState.value.step)
    }

    @Test
    fun `AI로_글쓰기를_끄면_초안_요청_없이_사진만_채워_등록_화면으로_간다`() {
        val viewModel = createViewModel()

        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = false))

        val state = viewModel.uiState.value
        assertEquals(UploadStep.FORM, state.step)
        assertEquals(photos, state.selectedImageUris)
        assertTrue(feedRepository.aiDraftRequests.isEmpty())
        assertTrue(uploader.uploaded.isEmpty())
    }

    @Test
    fun `게시할_때_AI_단계에서_올린_사진은_다시_올리지_않는다`() {
        feedRepository.aiDraft = FeedAiDraft(title = "제목", content = "내용", category = FeedCategory.ETC, price = 1_000)
        val viewModel = createViewModel()
        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

        viewModel.handleIntent(UploadIntent.Submit)

        assertEquals(photos, uploader.uploaded)
        assertEquals(listOf("key-content://media/1", "key-content://media/2"), feedRepository.createdFeeds.single().imageKeys)
    }
}
