package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.api.FeedApiService
import com.sseotdabwa.buyornot.core.network.dto.request.FeedRequest
import com.sseotdabwa.buyornot.core.network.dto.request.PresignedUrlRequest
import com.sseotdabwa.buyornot.core.network.dto.request.VoteRequest
import com.sseotdabwa.buyornot.core.network.dto.response.AuthorDto
import com.sseotdabwa.buyornot.core.network.dto.response.BaseResponse
import com.sseotdabwa.buyornot.core.network.dto.response.FeedItemDto
import com.sseotdabwa.buyornot.core.network.dto.response.FeedListResponse
import com.sseotdabwa.buyornot.core.network.dto.response.FeedResponse
import com.sseotdabwa.buyornot.core.network.dto.response.HotFeedListResponse
import com.sseotdabwa.buyornot.core.network.dto.response.PresignedUrlResponse
import com.sseotdabwa.buyornot.core.network.dto.response.VoteResponse
import kotlinx.coroutines.test.runTest
import okhttp3.RequestBody
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FeedRepositoryImplTest {
    private val api = FakeFeedApiService()
    private val repository = FeedRepositoryImpl(api)

    @Test
    fun `핫플_투표_응답의_문자열_커서와_마감_시각을_그대로_옮긴다`() =
        runTest {
            api.hotResponse =
                HotFeedListResponse(
                    content = listOf(feedItemDto(feedId = 7, voteClosedAt = "2026-11-03T10:00:00")),
                    nextCursor = "12_7",
                    hasNext = true,
                )

            val hotFeeds = repository.getHotFeedList(cursor = "15_9", category = listOf("FOOD"))

            assertEquals("15_9", api.lastHotCursor)
            assertEquals(listOf("FOOD"), api.lastHotCategory)
            assertEquals("12_7", hotFeeds.nextCursor)
            assertEquals(true, hotFeeds.hasNext)
            assertEquals(7L, hotFeeds.feeds.single().feedId)
            assertEquals("2026-11-03T10:00:00", hotFeeds.feeds.single().voteClosedAt)
        }

    @Test
    fun `피드_단건의_마감_시각을_옮기고_없으면_null로_둔다`() =
        runTest {
            api.feedResponse = feedItemDto(feedId = 1, voteClosedAt = "2026-11-03T10:00:00")
            assertEquals("2026-11-03T10:00:00", repository.getFeed(1).voteClosedAt)

            api.feedResponse = feedItemDto(feedId = 1, voteClosedAt = null)
            assertNull(repository.getFeed(1).voteClosedAt)
        }

    private fun feedItemDto(
        feedId: Long,
        voteClosedAt: String?,
    ) = FeedItemDto(
        feedId = feedId,
        content = "지금 할인중인데 살까",
        price = 31900,
        category = "ETC",
        yesCount = 3,
        noCount = 1,
        totalCount = 4,
        feedStatus = "OPEN",
        images = emptyList(),
        author = AuthorDto(userId = 2, nickname = "참새방앗간12456", profileImage = null),
        createdAt = "2026-11-01T10:00:00",
        hasVoted = false,
        myVoteChoice = null,
        voteClosedAt = voteClosedAt,
    )
}

private class FakeFeedApiService : FeedApiService {
    var hotResponse: HotFeedListResponse? = null
    var lastHotCursor: String? = null
    var lastHotCategory: List<String>? = null
    var feedResponse: FeedItemDto? = null

    override suspend fun getHotFeedList(
        cursor: String?,
        size: Int,
        category: List<String>?,
    ): BaseResponse<HotFeedListResponse> {
        lastHotCursor = cursor
        lastHotCategory = category
        return BaseResponse(data = hotResponse, status = "OK")
    }

    override suspend fun getFeed(feedId: Long): BaseResponse<FeedItemDto> = BaseResponse(data = feedResponse, status = "OK")

    override suspend fun getFeedList(
        cursor: Long?,
        size: Int,
        feedStatus: String?,
        category: List<String>?,
    ): BaseResponse<FeedListResponse> = error("not used")

    override suspend fun getMyFeeds(
        cursor: Long?,
        size: Int,
        feedStatus: String?,
    ): BaseResponse<FeedListResponse> = error("not used")

    override suspend fun getPresignedUrl(request: PresignedUrlRequest): BaseResponse<PresignedUrlResponse> = error("not used")

    override suspend fun uploadImage(
        url: String,
        contentType: String,
        body: RequestBody,
    ): Response<Unit> = error("not used")

    override suspend fun createFeed(request: FeedRequest): BaseResponse<FeedResponse> = error("not used")

    override suspend fun deleteFeed(feedId: Long): BaseResponse<Unit> = error("not used")

    override suspend fun reportFeed(feedId: Long): BaseResponse<Unit> = error("not used")

    override suspend fun voteFeed(
        feedId: Long,
        request: VoteRequest,
    ): BaseResponse<VoteResponse> = error("not used")

    override suspend fun voteGuestFeed(
        feedId: Long,
        request: VoteRequest,
    ): BaseResponse<VoteResponse> = error("not used")
}
