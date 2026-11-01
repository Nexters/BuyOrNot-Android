package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.lifecycle.SavedStateHandle
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.CommentErrorCode
import com.sseotdabwa.buyornot.domain.model.CommentLike
import com.sseotdabwa.buyornot.domain.model.CommentReplyPage
import com.sseotdabwa.buyornot.domain.model.CommentThread
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CommentRepliesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val commentRepository = FakeCommentRepository()

    private fun createViewModel() =
        CommentRepliesViewModel(
            savedStateHandle =
                SavedStateHandle(
                    mapOf(
                        CommentRepliesViewModel.KEY_FEED_ID to 10L,
                        CommentRepliesViewModel.KEY_COMMENT_ID to 1L,
                    ),
                ),
            commentRepository = commentRepository,
            userPreferencesRepository = FakeUserPreferencesRepository(),
        )

    private fun givenThread(
        replyIds: List<Long>,
        isDeleted: Boolean = false,
        myReplyIds: Set<Long> = emptySet(),
    ) {
        commentRepository.threads[1] =
            CommentThread(
                comment = testComment(1).copy(replyCount = replyIds.size, isDeleted = isDeleted),
                totalCount = replyIds.size + if (isDeleted) 0 else 1,
            )
        commentRepository.replyPages[1] =
            CommentReplyPage(
                replies = replyIds.map { testReply(it, parentId = 1, isMine = it in myReplyIds) },
                nextCursor = null,
                hasNext = false,
            )
    }

    @Test
    fun `원댓글과_답글을_함께_불러와_헤더_개수를_채운다`() {
        givenThread(replyIds = listOf(11, 12))

        val state = createViewModel().uiState.value

        assertFalse(state.isLoading)
        assertEquals(1L, state.comment?.id)
        assertEquals(listOf(11L, 12L), state.replies.map { it.id })
        assertEquals(3, state.totalCount)
        assertTrue(state.canWriteReply)
    }

    @Test
    fun `답글을_등록하면_입력을_비우고_알린_뒤_다시_불러와_끝으로_스크롤한다`() =
        runTest {
            givenThread(replyIds = listOf(11))
            val viewModel = createViewModel()
            viewModel.handleIntent(CommentRepliesIntent.OnReplyInputChanged("  저도요  "))
            commentRepository.replyPages[1] =
                CommentReplyPage(listOf(testReply(11, parentId = 1), testReply(12, parentId = 1)), nextCursor = null, hasNext = false)

            viewModel.handleIntent(CommentRepliesIntent.OnReplySubmit)

            val state = viewModel.uiState.value
            assertEquals(listOf(1L to "저도요"), commentRepository.createReplyCalls)
            assertEquals("", state.replyInput)
            assertEquals(listOf(11L, 12L), state.replies.map { it.id })
            assertTrue(state.pendingScrollToLatest)
            assertEquals(CommentRepliesSideEffect.ShowSnackbar(message = "의견을 남겼어요!"), viewModel.sideEffect.first())
        }

    @Test
    fun `금칙어로_거절되면_내용을_고칠_때까지_등록을_막는다`() {
        givenThread(replyIds = emptyList())
        commentRepository.createReplyError = ApiException(code = CommentErrorCode.PROFANITY, message = "부적절한 표현")
        val viewModel = createViewModel()
        viewModel.handleIntent(CommentRepliesIntent.OnReplyInputChanged("나쁜말"))

        viewModel.handleIntent(CommentRepliesIntent.OnReplySubmit)
        assertFalse(viewModel.uiState.value.canSubmitReply)

        viewModel.handleIntent(CommentRepliesIntent.OnReplyInputChanged("고운말"))
        assertTrue(viewModel.uiState.value.canSubmitReply)
    }

    @Test
    fun `삭제된_원댓글에는_답글을_쓸_수_없다`() {
        givenThread(replyIds = listOf(11), isDeleted = true)

        assertFalse(createViewModel().uiState.value.canWriteReply)
    }

    @Test
    fun `답글을_삭제하면_목록과_헤더_개수에서_빼고_이_화면에서_알린다`() =
        runTest {
            givenThread(replyIds = listOf(11, 12), myReplyIds = setOf(11))
            val viewModel = createViewModel()

            viewModel.handleIntent(CommentRepliesIntent.ShowDeleteDialog(CommentTarget(commentId = 1, replyId = 11)))
            viewModel.handleIntent(CommentRepliesIntent.OnDeleteConfirmed)

            val state = viewModel.uiState.value
            assertEquals(listOf(1L to 11L), commentRepository.deleteReplyCalls)
            assertEquals(listOf(12L), state.replies.map { it.id })
            assertEquals(2, state.totalCount)
            assertEquals(CommentRepliesSideEffect.ShowSnackbar(message = "삭제가 완료되었어요."), viewModel.sideEffect.first())
        }

    @Test
    fun `삭제된_원댓글의_마지막_답글을_지우면_안내를_들고_바로_상세로_돌아간다`() =
        runTest {
            givenThread(replyIds = listOf(11), isDeleted = true, myReplyIds = setOf(11))
            val viewModel = createViewModel()

            viewModel.handleIntent(CommentRepliesIntent.ShowDeleteDialog(CommentTarget(commentId = 1, replyId = 11)))
            viewModel.handleIntent(CommentRepliesIntent.OnDeleteConfirmed)

            assertEquals(
                CommentRepliesSideEffect.NavigateBack(message = "삭제가 완료되었어요."),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `원댓글을_신고하면_안내를_들고_바로_상세로_돌아간다`() =
        runTest {
            givenThread(replyIds = listOf(11))
            val viewModel = createViewModel()

            viewModel.handleIntent(CommentRepliesIntent.ShowReportDialog(CommentTarget(commentId = 1)))
            viewModel.handleIntent(CommentRepliesIntent.OnReportConfirmed)

            assertEquals(
                CommentRepliesSideEffect.NavigateBack(message = "댓글 신고가 접수되었어요."),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `원댓글을_공감하면_서버_값으로_맞춘다`() {
        givenThread(replyIds = emptyList())
        commentRepository.likeResult = CommentLike(likeCount = 4, isLiked = true)
        val viewModel = createViewModel()

        viewModel.handleIntent(CommentRepliesIntent.OnLikeClicked)

        val comment = viewModel.uiState.value.comment
        assertEquals(4, comment?.likeCount)
        assertEquals(true, comment?.isLiked)
    }

    @Test
    fun `이미_삭제된_원댓글로_들어오면_알리고_돌아간다`() =
        runTest {
            commentRepository.threadError = ApiException(code = CommentErrorCode.NOT_FOUND, message = null)
            val viewModel = createViewModel()

            assertEquals(
                CommentRepliesSideEffect.NavigateBack(message = "이미 삭제된 댓글이에요."),
                viewModel.sideEffect.first(),
            )
        }
}
