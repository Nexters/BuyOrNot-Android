package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.lifecycle.SavedStateHandle
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.CommentErrorCode
import com.sseotdabwa.buyornot.domain.model.CommentLike
import com.sseotdabwa.buyornot.domain.model.CommentPage
import com.sseotdabwa.buyornot.domain.model.CommentReplyPage
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.model.CommentThread
import com.sseotdabwa.buyornot.domain.model.FeedStatus
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import com.sseotdabwa.buyornot.domain.repository.CommentThreadChange
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val commentRepository = FakeCommentRepository()

    private fun createViewModel(
        feedRepository: FakeFeedRepository,
        scrollToComments: Boolean = false,
        userRepository: FakeUserRepository = FakeUserRepository(),
    ) = NotificationDetailViewModel(
        savedStateHandle =
            SavedStateHandle(
                mapOf(
                    "feedId" to 10L,
                    NotificationDetailViewModel.KEY_SCROLL_TO_COMMENTS to scrollToComments,
                ),
            ),
        analytics = FakeAnalytics(),
        feedRepository = feedRepository,
        commentRepository = commentRepository,
        notificationRepository = FakeNotificationRepository(),
        userRepository = userRepository,
        userPreferencesRepository = FakeUserPreferencesRepository(),
    )

    @Test
    fun `투표하지_않은_진행중_피드도_댓글을_불러오고_쓸_수_있다`() {
        val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = false)))

        assertEquals(1, commentRepository.getCalls.size)
        assertTrue(viewModel.uiState.value.canViewComments)
        assertTrue(viewModel.uiState.value.canWriteComment)
    }

    @Test
    fun `투표하면_내_댓글에_투표_태그가_붙도록_댓글을_다시_불러온다`() {
        commentRepository.firstPages[CommentSort.REGISTERED] =
            CommentPage(listOf(testComment(1)), nextCursor = null, hasNext = false)
        val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = false)))

        viewModel.handleIntent(NotificationDetailIntent.OnVoteClicked(optionIndex = 0))

        assertEquals(
            VoteChoice.YES,
            viewModel.uiState.value.feed
                ?.myVoteChoice,
        )
        assertEquals(2, commentRepository.getCalls.size)
    }

    @Test
    fun `피드_작성자는_투표하지_않아도_댓글을_보고_쓸_수_있다`() {
        val viewModel = createViewModel(FakeFeedRepository(testFeed(authorUserId = MY_USER_ID, hasVoted = false)))

        assertTrue(viewModel.uiState.value.canViewComments)
        assertTrue(viewModel.uiState.value.canWriteComment)
        assertEquals(1, commentRepository.getCalls.size)
    }

    @Test
    fun `프로필_조회가_실패해도_저장된_id로_작성자를_판정해_댓글을_연다`() {
        val viewModel =
            createViewModel(
                FakeFeedRepository(testFeed(authorUserId = MY_USER_ID, hasVoted = false)),
                userRepository = FakeUserRepository(profileError = IllegalStateException("network")),
            )

        assertTrue(viewModel.uiState.value.isOwner)
        assertTrue(viewModel.uiState.value.canWriteComment)
    }

    @Test
    fun `마감된_피드도_투표하지_않은_회원이_댓글을_보고_쓸_수_있다`() {
        val viewModel = createViewModel(FakeFeedRepository(testFeed(feedStatus = FeedStatus.CLOSED)))

        assertTrue(viewModel.uiState.value.canViewComments)
        assertTrue(viewModel.uiState.value.canWriteComment)
    }

    @Test
    fun `댓글을_등록하면_입력을_비우고_댓글_수를_늘리고_최신순으로_다시_불러온다`() =
        runTest {
            val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = true, commentCount = 2)))
            viewModel.handleIntent(NotificationDetailIntent.OnCommentInputChanged("  저도 고민돼요  "))

            viewModel.handleIntent(NotificationDetailIntent.OnCommentSubmit)

            val state = viewModel.uiState.value
            assertEquals(listOf("저도 고민돼요"), commentRepository.createdContents)
            assertEquals("", state.commentInput)
            assertEquals(3, state.feed?.commentCount)
            assertEquals(2, commentRepository.getCalls.size)
            assertEquals(CommentSort.LATEST, state.commentSort)
            assertEquals(CommentSort.LATEST, commentRepository.getCalls.last().second)
            assertEquals(
                NotificationDetailSideEffect.ShowSnackbar(message = "의견을 남겼어요!"),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `300자를_넘는_댓글은_등록_요청을_보내지_않는다`() {
        val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = true)))
        viewModel.handleIntent(NotificationDetailIntent.OnCommentInputChanged("가".repeat(301)))

        viewModel.handleIntent(NotificationDetailIntent.OnCommentSubmit)

        assertFalse(viewModel.uiState.value.canSubmitComment)
        assertTrue(commentRepository.createdContents.isEmpty())
    }

    @Test
    fun `금칙어로_거절되면_서버_메시지를_보여주고_내용을_고칠_때까지_등록을_막는다`() =
        runTest {
            val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = true)))
            commentRepository.createError =
                ApiException(code = CommentErrorCode.PROFANITY, message = "부적절한 표현을 수정한 뒤 다시 등록해주세요.")
            viewModel.handleIntent(NotificationDetailIntent.OnCommentInputChanged("나쁜말"))

            viewModel.handleIntent(NotificationDetailIntent.OnCommentSubmit)

            assertEquals(
                NotificationDetailSideEffect.ShowSnackbar(message = "부적절한 표현을 수정한 뒤 다시 등록해주세요."),
                viewModel.sideEffect.first(),
            )
            assertEquals("나쁜말", viewModel.uiState.value.commentInput)
            assertFalse(viewModel.uiState.value.canSubmitComment)

            viewModel.handleIntent(NotificationDetailIntent.OnCommentInputChanged("고운말"))

            assertTrue(viewModel.uiState.value.canSubmitComment)
        }

    @Test
    fun `댓글_작성_빈도_제한에_걸리면_서버_메시지를_보여주고_내용을_고칠_때까지_등록을_막는다`() =
        runTest {
            val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = true)))
            commentRepository.createError =
                ApiException(code = CommentErrorCode.RATE_LIMITED, message = "잠시 후 다시 댓글을 남길 수 있어요.")
            viewModel.handleIntent(NotificationDetailIntent.OnCommentInputChanged("저도요"))

            viewModel.handleIntent(NotificationDetailIntent.OnCommentSubmit)

            assertEquals(
                NotificationDetailSideEffect.ShowSnackbar(message = "잠시 후 다시 댓글을 남길 수 있어요."),
                viewModel.sideEffect.first(),
            )
            assertEquals("저도요", viewModel.uiState.value.commentInput)
            assertFalse(viewModel.uiState.value.canSubmitComment)

            viewModel.handleIntent(NotificationDetailIntent.OnCommentInputChanged("저도요!"))

            assertTrue(viewModel.uiState.value.canSubmitComment)
        }

    @Test
    fun `금칙어_반복으로_작성이_제한되면_내용을_고칠_때까지_등록을_막는다`() =
        runTest {
            val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = true)))
            commentRepository.createError =
                ApiException(code = CommentErrorCode.PROFANITY_BLOCKED, message = "잠시 후 다시 댓글을 남길 수 있어요.")
            viewModel.handleIntent(NotificationDetailIntent.OnCommentInputChanged("나쁜말"))

            viewModel.handleIntent(NotificationDetailIntent.OnCommentSubmit)

            assertFalse(viewModel.uiState.value.canSubmitComment)
        }

    @Test
    fun `댓글을_신고하면_아이콘_없는_스낵바로_접수를_알린다`() =
        runTest {
            commentRepository.firstPages[CommentSort.REGISTERED] =
                CommentPage(listOf(testComment(1)), nextCursor = null, hasNext = false)
            val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = true, commentCount = 1)))

            viewModel.handleIntent(NotificationDetailIntent.ShowReportCommentDialog(CommentTarget(commentId = 1)))
            viewModel.handleIntent(NotificationDetailIntent.OnReportCommentConfirmed)

            assertEquals(
                NotificationDetailSideEffect.ShowSnackbar(message = "댓글 신고가 접수되었어요."),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `댓글을_삭제하면_목록에서_빼고_댓글_수를_줄인다`() {
        commentRepository.firstPages[CommentSort.REGISTERED] =
            CommentPage(listOf(testComment(1, isMine = true), testComment(2)), nextCursor = null, hasNext = false)
        val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = true, commentCount = 2)))

        viewModel.handleIntent(NotificationDetailIntent.ShowDeleteCommentDialog(CommentTarget(commentId = 1)))
        viewModel.handleIntent(NotificationDetailIntent.OnDeleteCommentConfirmed)

        val state = viewModel.uiState.value
        assertEquals(listOf(2L), state.comments.map { it.id })
        assertEquals(1, state.feed?.commentCount)
        assertEquals(null, state.deletingComment)
    }

    @Test
    fun `이미_신고한_댓글이면_안내하고_목록은_그대로_둔다`() =
        runTest {
            commentRepository.firstPages[CommentSort.REGISTERED] =
                CommentPage(listOf(testComment(1)), nextCursor = null, hasNext = false)
            commentRepository.reportError = ApiException(code = CommentErrorCode.ALREADY_REPORTED, message = null)
            val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = true, commentCount = 1)))

            viewModel.handleIntent(NotificationDetailIntent.ShowReportCommentDialog(CommentTarget(commentId = 1)))
            viewModel.handleIntent(NotificationDetailIntent.OnReportCommentConfirmed)

            assertEquals(
                NotificationDetailSideEffect.ShowSnackbar(message = "이미 신고한 댓글이에요."),
                viewModel.sideEffect.first(),
            )
            assertEquals(
                listOf(1L),
                viewModel.uiState.value.comments
                    .map { it.id },
            )
        }

    @Test
    fun `다음_페이지를_이어_붙이고_중복된_댓글은_한_번만_남긴다`() {
        commentRepository.firstPages[CommentSort.REGISTERED] =
            CommentPage(listOf(testComment(1), testComment(2)), nextCursor = 2, hasNext = true)
        commentRepository.nextPages[2] =
            CommentPage(listOf(testComment(2), testComment(3)), nextCursor = null, hasNext = false)
        val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = true)))

        viewModel.handleIntent(NotificationDetailIntent.LoadNextComments)

        val state = viewModel.uiState.value
        assertEquals(listOf(1L, 2L, 3L), state.comments.map { it.id })
        assertFalse(state.hasNextComments)
    }

    @Test
    fun `정렬을_바꾸면_늦게_도착한_이전_정렬_응답은_무시한다`() {
        val registeredGate = CompletableDeferred<Unit>()
        commentRepository.firstPages[CommentSort.REGISTERED] =
            CommentPage(listOf(testComment(1)), nextCursor = null, hasNext = false)
        commentRepository.firstPages[CommentSort.LATEST] =
            CommentPage(listOf(testComment(9)), nextCursor = null, hasNext = false)
        commentRepository.beforeGet = { _, sort -> if (sort == CommentSort.REGISTERED) registeredGate.await() }
        val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = true)))

        viewModel.handleIntent(NotificationDetailIntent.OnCommentSortSelected(CommentSort.LATEST))
        registeredGate.complete(Unit)

        assertEquals(
            listOf(9L),
            viewModel.uiState.value.comments
                .map { it.id },
        )
        assertEquals(CommentSort.LATEST, viewModel.uiState.value.commentSort)
    }

    @Test
    fun `댓글_진입점으로_들어오면_한_번만_댓글_영역으로_스크롤한다`() {
        val viewModel = createViewModel(FakeFeedRepository(testFeed(hasVoted = true)), scrollToComments = true)

        assertTrue(viewModel.uiState.value.pendingScrollToComments)
        viewModel.handleIntent(NotificationDetailIntent.OnCommentScrollHandled)

        assertFalse(viewModel.uiState.value.pendingScrollToComments)
    }

    @Test
    fun `공감을_누르면_응답_전에_먼저_반영하고_서버_값으로_맞춘다`() {
        commentRepository.firstPages[CommentSort.REGISTERED] =
            CommentPage(listOf(testComment(1)), nextCursor = null, hasNext = false)
        commentRepository.likeResult = CommentLike(likeCount = 5, isLiked = true)
        val viewModel = createViewModel(FakeFeedRepository(testFeed()))

        viewModel.handleIntent(NotificationDetailIntent.OnLikeClicked(commentId = 1))

        val comment =
            viewModel.uiState.value.comments
                .single()
        assertEquals(listOf(1L), commentRepository.likeCalls)
        assertEquals(5, comment.likeCount)
        assertTrue(comment.isLiked)
    }

    @Test
    fun `공감에_실패하면_원래_상태로_되돌리고_안내한다`() =
        runTest {
            commentRepository.firstPages[CommentSort.REGISTERED] =
                CommentPage(listOf(testComment(1).copy(likeCount = 2, isLiked = true)), nextCursor = null, hasNext = false)
            commentRepository.unlikeError = IllegalStateException("network")
            val viewModel = createViewModel(FakeFeedRepository(testFeed()))

            viewModel.handleIntent(NotificationDetailIntent.OnLikeClicked(commentId = 1))

            val comment =
                viewModel.uiState.value.comments
                    .single()
            assertEquals(2, comment.likeCount)
            assertTrue(comment.isLiked)
            assertEquals(
                NotificationDetailSideEffect.ShowSnackbar(message = "공감에 실패했어요. 다시 시도해 주세요."),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `삭제된_댓글은_공감할_수_없다`() {
        commentRepository.firstPages[CommentSort.REGISTERED] =
            CommentPage(listOf(testComment(1).copy(isDeleted = true, replyCount = 1)), nextCursor = null, hasNext = false)
        val viewModel = createViewModel(FakeFeedRepository(testFeed()))

        viewModel.handleIntent(NotificationDetailIntent.OnLikeClicked(commentId = 1))

        assertTrue(commentRepository.likeCalls.isEmpty())
    }

    @Test
    fun `답글쓰기를_누르면_그_댓글의_답글_페이지로_이동한다`() =
        runTest {
            val viewModel = createViewModel(FakeFeedRepository(testFeed()))

            viewModel.handleIntent(NotificationDetailIntent.OnReplyClicked(commentId = 7))

            assertEquals(
                NotificationDetailSideEffect.NavigateToReplies(feedId = 10, commentId = 7),
                viewModel.sideEffect.first(),
            )
        }

    @Test
    fun `답글을_삭제하면_원댓글의_답글과_전체_댓글_수를_줄인다`() {
        val parent =
            testComment(
                1,
            ).copy(replyCount = 2, replies = listOf(testReply(11, parentId = 1, isMine = true), testReply(12, parentId = 1)))
        commentRepository.firstPages[CommentSort.REGISTERED] = CommentPage(listOf(parent), nextCursor = null, hasNext = false)
        val viewModel = createViewModel(FakeFeedRepository(testFeed(commentCount = 3)))

        viewModel.handleIntent(NotificationDetailIntent.ShowDeleteCommentDialog(CommentTarget(commentId = 1, replyId = 11)))
        viewModel.handleIntent(NotificationDetailIntent.OnDeleteCommentConfirmed)

        val state = viewModel.uiState.value
        assertEquals(listOf(1L to 11L), commentRepository.deleteReplyCalls)
        assertEquals(
            listOf(12L),
            state.comments
                .single()
                .replies
                .map { it.id },
        )
        assertEquals(1, state.comments.single().replyCount)
        assertEquals(2, state.feed?.commentCount)
    }

    @Test
    fun `답글이_남은_원댓글을_삭제하면_삭제된_댓글로_남긴다`() {
        val parent = testComment(1, isMine = true).copy(replyCount = 1, replies = listOf(testReply(11, parentId = 1)))
        commentRepository.firstPages[CommentSort.REGISTERED] = CommentPage(listOf(parent), nextCursor = null, hasNext = false)
        val viewModel = createViewModel(FakeFeedRepository(testFeed(commentCount = 2)))

        viewModel.handleIntent(NotificationDetailIntent.ShowDeleteCommentDialog(CommentTarget(commentId = 1)))
        viewModel.handleIntent(NotificationDetailIntent.OnDeleteCommentConfirmed)

        val comment =
            viewModel.uiState.value.comments
                .single()
        assertTrue(comment.isDeleted)
        assertEquals(listOf(11L), comment.replies.map { it.id })
    }

    @Test
    fun `삭제된_원댓글의_마지막_답글을_지우면_원댓글도_목록에서_뺀다`() {
        val parent = testComment(1).copy(isDeleted = true, replyCount = 1, replies = listOf(testReply(11, parentId = 1, isMine = true)))
        commentRepository.firstPages[CommentSort.REGISTERED] = CommentPage(listOf(parent), nextCursor = null, hasNext = false)
        val viewModel = createViewModel(FakeFeedRepository(testFeed(commentCount = 1)))

        viewModel.handleIntent(NotificationDetailIntent.ShowDeleteCommentDialog(CommentTarget(commentId = 1, replyId = 11)))
        viewModel.handleIntent(NotificationDetailIntent.OnDeleteCommentConfirmed)

        assertTrue(
            viewModel.uiState.value.comments
                .isEmpty(),
        )
    }

    @Test
    fun `다른_화면에서_스레드가_바뀌면_그_댓글만_다시_받아_답글까지_맞춘다`() =
        runTest {
            commentRepository.firstPages[CommentSort.REGISTERED] =
                CommentPage(listOf(testComment(1), testComment(2)), nextCursor = null, hasNext = false)
            commentRepository.threads[1] = CommentThread(testComment(1).copy(replyCount = 1, likeCount = 3), totalCount = 2)
            commentRepository.replyPages[1] = CommentReplyPage(listOf(testReply(11, parentId = 1)), nextCursor = null, hasNext = false)
            val viewModel = createViewModel(FakeFeedRepository(testFeed(commentCount = 2)))

            commentRepository.emitThreadChange(CommentThreadChange(feedId = 10, commentId = 1))

            val state = viewModel.uiState.value
            val refreshed = state.comments.first()
            assertEquals(listOf(11L), refreshed.replies.map { it.id })
            assertEquals(3, refreshed.likeCount)
            assertEquals(3, state.feed?.commentCount)
            assertEquals(2L, state.comments[1].id)
        }

    @Test
    fun `다른_피드의_스레드_변경은_무시한다`() =
        runTest {
            commentRepository.firstPages[CommentSort.REGISTERED] =
                CommentPage(listOf(testComment(1)), nextCursor = null, hasNext = false)
            val viewModel = createViewModel(FakeFeedRepository(testFeed()))

            commentRepository.emitThreadChange(CommentThreadChange(feedId = 99, commentId = 1))

            assertEquals(
                0,
                viewModel.uiState.value.comments
                    .single()
                    .replyCount,
            )
        }
}
