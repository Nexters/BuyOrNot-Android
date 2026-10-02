package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.api.CommentApiService
import com.sseotdabwa.buyornot.core.network.dto.request.CommentRequest
import com.sseotdabwa.buyornot.core.network.dto.response.BaseResponse
import com.sseotdabwa.buyornot.core.network.dto.response.CommentCreateResponse
import com.sseotdabwa.buyornot.core.network.dto.response.CommentDto
import com.sseotdabwa.buyornot.core.network.dto.response.CommentListResponse
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.CommentAuthorType
import com.sseotdabwa.buyornot.domain.model.CommentErrorCode
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CommentRepositoryImplTest {
    private val api = FakeCommentApiService()
    private val repository = CommentRepositoryImpl(api)

    @Test
    fun `댓글_목록_응답을_도메인_모델로_변환한다`() =
        runTest {
            api.listResponse =
                CommentListResponse(
                    content =
                        listOf(
                            commentDto(id = 1, authorType = "MEMBER", voteChoice = "YES", isMine = true),
                            commentDto(id = 2, authorType = "GUEST", voteChoice = null),
                        ),
                    nextCursor = 2,
                    hasNext = true,
                )

            val page = repository.getComments(feedId = 10, sort = CommentSort.LATEST)

            assertEquals("LATEST", api.lastSort)
            assertEquals(2L, page.nextCursor)
            assertEquals(true, page.hasNext)
            assertEquals(CommentAuthorType.MEMBER, page.comments[0].authorType)
            assertEquals(VoteChoice.YES, page.comments[0].voteChoice)
            assertEquals(true, page.comments[0].isMine)
            assertEquals(CommentAuthorType.GUEST, page.comments[1].authorType)
            assertNull(page.comments[1].voteChoice)
        }

    @Test
    fun `서버가_거절하면_에러_코드와_서버_메시지를_담은_ApiException을_던진다`() =
        runTest {
            api.createError =
                httpException(
                    code = 400,
                    body = """{"errorCode":"COMMENT_011","message":"부적절한 표현을 수정한 뒤 다시 등록해주세요."}""",
                )

            val error = assertFailsWith<ApiException> { repository.createComment(feedId = 10, content = "나쁜말") }

            assertEquals(CommentErrorCode.PROFANITY, error.code)
            assertEquals("부적절한 표현을 수정한 뒤 다시 등록해주세요.", error.message)
        }

    @Test
    fun `댓글_작성에_성공하면_해당_피드_id를_변경_신호로_보낸다`() =
        runTest(UnconfinedTestDispatcher()) {
            val change = async { repository.commentChanges.first() }

            repository.createComment(feedId = 10, content = "저도 고민돼요")

            assertEquals(10L, change.await())
            assertEquals("저도 고민돼요", api.lastCreateRequest?.content)
        }

    @Test
    fun `댓글_삭제에_성공하면_해당_피드_id를_변경_신호로_보낸다`() =
        runTest(UnconfinedTestDispatcher()) {
            val change = async { repository.commentChanges.first() }

            repository.deleteComment(feedId = 7, commentId = 1)

            assertEquals(7L, change.await())
        }

    private fun commentDto(
        id: Long,
        authorType: String,
        voteChoice: String?,
        isMine: Boolean = false,
    ) = CommentDto(
        id = id,
        authorType = authorType,
        nickname = "참새방앗간12456",
        profileImage = null,
        content = "저도 예전에 사려고 했다가...",
        isMine = isMine,
        isAuthor = false,
        voteChoice = voteChoice,
        createdAt = "2026-09-20T10:35:17",
    )

    private fun httpException(
        code: Int,
        body: String,
    ) = HttpException(Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())))
}

private class FakeCommentApiService : CommentApiService {
    var listResponse = CommentListResponse(content = emptyList(), nextCursor = null, hasNext = false)
    var createError: HttpException? = null
    var lastSort: String? = null
    var lastCreateRequest: CommentRequest? = null

    override suspend fun getComments(
        feedId: Long,
        cursor: Long?,
        size: Int,
        sort: String?,
    ): BaseResponse<CommentListResponse> {
        lastSort = sort
        return BaseResponse(data = listResponse, status = "200")
    }

    override suspend fun createComment(
        feedId: Long,
        request: CommentRequest,
    ): BaseResponse<CommentCreateResponse> {
        createError?.let { throw it }
        lastCreateRequest = request
        return BaseResponse(data = CommentCreateResponse(id = 101), status = "201")
    }

    override suspend fun deleteComment(
        feedId: Long,
        commentId: Long,
    ): BaseResponse<Unit> = BaseResponse(status = "200")

    override suspend fun reportComment(
        feedId: Long,
        commentId: Long,
    ): BaseResponse<Unit> = BaseResponse(status = "200")
}
