package com.sseotdabwa.buyornot.feature.upload.ui

import android.net.Uri
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent.AiDraftCompleted
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent.VoteCreateAbandoned
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent.WritingMode
import com.sseotdabwa.buyornot.domain.model.FeedAiDraft
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

// android.net.Uri가 필요해 Robolectric으로 돌리고, 로컬 JDK 17에서도 뜨는 SDK로 고정한다.
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UploadViewModelAnalyticsTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val feedRepository = FakeFeedRepository()
    private val analytics = FakeAnalytics()
    private val photos = listOf(Uri.parse("content://media/1"), Uri.parse("content://media/2"))

    private fun createViewModel() =
        UploadViewModel(
            feedRepository = feedRepository,
            userPreferencesRepository = FakeUserPreferencesRepository(),
            analytics = analytics,
            feedImageUploader = FakeFeedImageUploader(),
        )

    private inline fun <reified T : AnalyticsEvent> events() = analytics.events.filterIsInstance<T>()

    @Test
    fun `AI_초안을_받아_제목만_고쳐_게시하면_AI_작성과_고친_필드를_남긴다`() {
        feedRepository.aiDraft = FeedAiDraft(title = "가습기 살까요", content = "건조해요", category = FeedCategory.INTERIOR, price = 29_000)
        val viewModel = createViewModel()

        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))
        viewModel.handleIntent(UploadIntent.UpdateTitle("가습기 살까 말까"))
        viewModel.handleIntent(UploadIntent.Submit)

        assertEquals(
            listOf(AnalyticsEvent.VoteCreatePhotosConfirmed(imageCount = 2, useAi = true)),
            events<AnalyticsEvent.VoteCreatePhotosConfirmed>(),
        )
        val draft = events<AiDraftCompleted>().single()
        assertEquals(AiDraftCompleted.Result.SUCCESS, draft.result)
        assertEquals(listOf("title", "content", "category", "price"), draft.filledFields)
        val completed = events<AnalyticsEvent.VoteCreateCompleted>().single()
        assertEquals(WritingMode.AI, completed.writingMode)
        assertEquals(listOf("title"), completed.aiEditedFields)
    }

    @Test
    fun `게시_요청_중에_고친_입력은_완료_이벤트에_반영하지_않는다`() {
        feedRepository.aiDraft = FeedAiDraft(title = "가습기 살까요", content = "건조해요", category = FeedCategory.INTERIOR, price = 29_000)
        val gate = CompletableDeferred<Unit>()
        feedRepository.beforeCreateFeed = { gate.await() }
        val viewModel = createViewModel()
        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

        viewModel.handleIntent(UploadIntent.Submit)
        viewModel.handleIntent(UploadIntent.UpdateTitle("요청 중에 바꾼 제목"))
        gate.complete(Unit)

        val completed = events<AnalyticsEvent.VoteCreateCompleted>().single()
        assertEquals("가습기 살까요", completed.voteTitle)
        assertEquals(emptyList(), completed.aiEditedFields)
    }

    @Test
    fun `AI_초안을_받지_못한_채_등록_화면에서_나가면_AI_실패_작성으로_이탈을_남긴다`() {
        feedRepository.aiDraftError = IllegalStateException("timeout")
        val viewModel = createViewModel()

        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))
        viewModel.handleIntent(UploadIntent.NavigateBack)

        val draft = events<AiDraftCompleted>().single()
        assertEquals(AiDraftCompleted.Result.FAILED, draft.result)
        assertNull(draft.filledFields)
        val abandoned = events<VoteCreateAbandoned>().single()
        assertEquals(VoteCreateAbandoned.Step.FORM, abandoned.step)
        assertEquals(WritingMode.AI_FAILED, abandoned.writingMode)
    }

    @Test
    fun `초안의_제목과_내용이_비어_오면_빈_결과로_남긴다`() {
        feedRepository.aiDraft = FeedAiDraft(title = null, content = null, category = FeedCategory.ETC, price = null)
        val viewModel = createViewModel()

        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

        assertEquals(AiDraftCompleted.Result.EMPTY, events<AiDraftCompleted>().single().result)
    }

    @Test
    fun `AI_없이_직접_써서_게시하면_직접_작성으로_남기고_고친_필드는_없다`() {
        val viewModel = createViewModel()
        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = false))
        viewModel.handleIntent(UploadIntent.UpdateCategory(FeedCategory.ETC))
        viewModel.handleIntent(UploadIntent.UpdateTitle("제목"))
        viewModel.handleIntent(
            UploadIntent.UpdatePrice(
                "1000",
                androidx.compose.ui.text.input
                    .TextFieldValue("1,000"),
            ),
        )

        viewModel.handleIntent(UploadIntent.Submit)

        assertTrue(events<AiDraftCompleted>().isEmpty())
        val completed = events<AnalyticsEvent.VoteCreateCompleted>().single()
        assertEquals(WritingMode.MANUAL, completed.writingMode)
        assertNull(completed.aiEditedFields)
    }

    @Test
    fun `AI_초안을_기다리다_취소하면_취소로_남기고_작성_방식은_다시_정해지지_않은_상태가_된다`() {
        val gate = CompletableDeferred<Unit>()
        feedRepository.beforeAiDraft = { gate.await() }
        val viewModel = createViewModel()
        viewModel.handleIntent(UploadIntent.ConfirmAlbum(photos, useAi = true))

        viewModel.handleIntent(UploadIntent.CancelAiWriting)
        viewModel.handleIntent(UploadIntent.LeaveAlbum(selectedCount = 2))

        assertEquals(AiDraftCompleted.Result.CANCELLED, events<AiDraftCompleted>().single().result)
        val abandoned = events<VoteCreateAbandoned>().single()
        assertEquals(VoteCreateAbandoned.Step.ALBUM, abandoned.step)
        assertNull(abandoned.writingMode)
    }

    @Test
    fun `사진을_고르지_않고_앨범에서_나가면_이탈로_세지_않는다`() {
        val viewModel = createViewModel()

        viewModel.handleIntent(UploadIntent.LeaveAlbum(selectedCount = 0))

        assertTrue(events<VoteCreateAbandoned>().isEmpty())
    }
}
