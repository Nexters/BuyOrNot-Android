package com.sseotdabwa.buyornot.feature.home.ui

import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent
import com.sseotdabwa.buyornot.core.analytics.performance.NoOpPerfTrace
import com.sseotdabwa.buyornot.core.analytics.performance.PerfTrace
import com.sseotdabwa.buyornot.core.analytics.performance.Performance
import com.sseotdabwa.buyornot.domain.model.Author
import com.sseotdabwa.buyornot.domain.model.BlockedUser
import com.sseotdabwa.buyornot.domain.model.CommentAuthorType
import com.sseotdabwa.buyornot.domain.model.CommentPage
import com.sseotdabwa.buyornot.domain.model.CommentPreview
import com.sseotdabwa.buyornot.domain.model.CommentSort
import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.FeedImage
import com.sseotdabwa.buyornot.domain.model.FeedStatus
import com.sseotdabwa.buyornot.domain.model.Notification
import com.sseotdabwa.buyornot.domain.model.UploadInfo
import com.sseotdabwa.buyornot.domain.model.UserPreferences
import com.sseotdabwa.buyornot.domain.model.UserProfile
import com.sseotdabwa.buyornot.domain.model.UserToken
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import com.sseotdabwa.buyornot.domain.model.VoteResult
import com.sseotdabwa.buyornot.domain.repository.CommentRepository
import com.sseotdabwa.buyornot.domain.repository.FeedList
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.NotificationRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

const val MY_USER_ID = 1L

fun testFeed(
    feedId: Long,
    commentCount: Int = 0,
    latestComment: CommentPreview? = null,
) = Feed(
    feedId = feedId,
    title = "장화 살지말지 고민됩니다",
    content = "지금 할인중인데 살까",
    price = "31,900",
    category = FeedCategory.ETC,
    yesCount = 3,
    noCount = 1,
    totalCount = 4,
    feedStatus = FeedStatus.OPEN,
    images = listOf(FeedImage(s3ObjectKey = "", imageUrl = "https://cdn/$feedId.jpg", imageWidth = 1, imageHeight = 1)),
    author = Author(userId = 2L, nickname = "참새방앗간12456", profileImage = null),
    createdAt = "2026-09-20T10:35:17",
    hasVoted = false,
    myVoteChoice = null,
    commentCount = commentCount,
    latestComment = latestComment,
)

fun testCommentPreview(content: String) =
    CommentPreview(
        authorType = CommentAuthorType.MEMBER,
        nickname = "토봉이날다12456",
        profileImage = null,
        content = content,
        isAuthor = false,
        voteChoice = VoteChoice.YES,
    )

class FakeFeedRepository(
    var feeds: List<Feed>,
) : FeedRepository {
    var voteImageUrl: String? = null

    override val feedCreatedRevision: StateFlow<Long> = MutableStateFlow(0L)

    override suspend fun getFeedList(
        cursor: Long?,
        size: Int,
        feedStatus: String?,
        category: List<String>?,
    ): FeedList = FeedList(feeds = feeds, nextCursor = null, hasNext = false)

    override suspend fun getFeed(feedId: Long): Feed = feeds.first { it.feedId == feedId }

    override suspend fun voteFeed(
        feedId: Long,
        choice: VoteChoice,
    ): VoteResult = VoteResult(feedId = feedId, choice = choice, yesCount = 4, noCount = 1, totalCount = 5, feedImageUrl = voteImageUrl)

    override suspend fun voteGuestFeed(
        feedId: Long,
        choice: VoteChoice,
    ): VoteResult = voteFeed(feedId, choice)

    override suspend fun getMyFeeds(
        cursor: Long?,
        size: Int,
        feedStatus: String?,
    ): FeedList = FeedList(feeds = emptyList(), nextCursor = null, hasNext = false)

    override suspend fun getPresignedUrl(
        fileName: String,
        contentType: String,
    ): UploadInfo = error("not used")

    override suspend fun uploadImage(
        url: String,
        bytes: ByteArray,
        contentType: String,
    ) = error("not used")

    override suspend fun createFeed(
        category: FeedCategory,
        price: Int,
        content: String,
        images: List<FeedImage>,
        title: String?,
        link: String?,
    ): Long = error("not used")

    override suspend fun deleteFeed(feedId: Long) = Unit

    override suspend fun reportFeed(feedId: Long) = Unit
}

class FakeCommentRepository : CommentRepository {
    override val commentChanges = MutableSharedFlow<Long>()

    override suspend fun getComments(
        feedId: Long,
        cursor: Long?,
        size: Int,
        sort: CommentSort,
    ): CommentPage = error("not used")

    override suspend fun createComment(
        feedId: Long,
        content: String,
    ): Long = error("not used")

    override suspend fun deleteComment(
        feedId: Long,
        commentId: Long,
    ) = error("not used")

    override suspend fun reportComment(
        feedId: Long,
        commentId: Long,
    ) = error("not used")
}

class FakeUserRepository : UserRepository {
    override suspend fun getMyProfile(): UserProfile =
        UserProfile(id = MY_USER_ID, nickname = "서따봐", profileImage = "", socialAccount = "KAKAO", email = "")

    override suspend fun deleteMyAccount() = Unit

    override suspend fun updateProfile(
        nickname: String?,
        profileImage: String?,
    ): UserProfile = error("not used")

    override suspend fun updateFcmToken(fcmToken: String) = Unit

    override suspend fun getBlockedUsers(): List<BlockedUser> = emptyList()

    override suspend fun blockUser(userId: Long) = Unit

    override suspend fun unblockUser(userId: Long) = Unit

    override suspend fun notifyAppOpened() = Unit
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
    override val userPreferences: Flow<UserPreferences> =
        flowOf(UserPreferences(displayName = "서따봐", profileImageUrl = "", userType = UserType.SOCIAL))
    override val userToken: Flow<UserToken> = emptyFlow()
    override val userType: Flow<UserType> = flowOf(UserType.SOCIAL)
    override val userId: Flow<Long> = flowOf(MY_USER_ID)

    override suspend fun updateUserType(userType: UserType) = Unit

    override suspend fun updateUserId(userId: Long) = Unit

    override suspend fun updateDisplayName(newName: String) = Unit

    override suspend fun updateProfileImageUrl(newUrl: String) = Unit
}

class FakeNotificationRepository : NotificationRepository {
    override suspend fun getNotifications(type: String?): List<Notification> = emptyList()

    override suspend fun markAsRead(notificationId: Long) = Unit

    override suspend fun getUnreadCount(): Int = 0
}

class FakeAnalytics : Analytics {
    override fun track(event: AnalyticsEvent) = Unit

    override fun identify(userId: String?) = Unit
}

class FakePerformance : Performance {
    override fun newTrace(name: String): PerfTrace = NoOpPerfTrace
}
