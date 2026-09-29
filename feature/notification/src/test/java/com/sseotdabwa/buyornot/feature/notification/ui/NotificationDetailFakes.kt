package com.sseotdabwa.buyornot.feature.notification.ui

import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent
import com.sseotdabwa.buyornot.domain.model.Author
import com.sseotdabwa.buyornot.domain.model.BlockedUser
import com.sseotdabwa.buyornot.domain.model.Comment
import com.sseotdabwa.buyornot.domain.model.CommentAuthorType
import com.sseotdabwa.buyornot.domain.model.CommentPage
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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

const val MY_USER_ID = 1L
const val OTHER_USER_ID = 2L

fun testFeed(
    authorUserId: Long = OTHER_USER_ID,
    hasVoted: Boolean = false,
    myVoteChoice: VoteChoice? = null,
    feedStatus: FeedStatus = FeedStatus.OPEN,
    commentCount: Int = 0,
) = Feed(
    feedId = 10,
    title = "장화 살지말지 고민됩니다",
    content = "지금 할인중인데 살까",
    price = "31,900",
    category = FeedCategory.ETC,
    yesCount = 3,
    noCount = 1,
    totalCount = 4,
    feedStatus = feedStatus,
    images = listOf(FeedImage(s3ObjectKey = "", imageUrl = "https://cdn/1.jpg", imageWidth = 1, imageHeight = 1)),
    author = Author(userId = authorUserId, nickname = "참새방앗간12456", profileImage = null),
    createdAt = "2026-09-20T10:35:17",
    hasVoted = hasVoted,
    myVoteChoice = myVoteChoice,
    commentCount = commentCount,
)

fun testComment(
    id: Long,
    isMine: Boolean = false,
) = Comment(
    id = id,
    authorType = CommentAuthorType.MEMBER,
    nickname = "거북이날다$id",
    profileImage = null,
    content = "댓글 $id",
    isMine = isMine,
    isAuthor = false,
    voteChoice = VoteChoice.YES,
    createdAt = "2026-09-20T10:35:17",
)

class FakeFeedRepository(
    var feed: Feed,
) : FeedRepository {
    var voteCount = 0

    override val feedCreatedRevision: StateFlow<Long> = MutableStateFlow(0L)

    override suspend fun getFeed(feedId: Long): Feed = feed

    override suspend fun voteFeed(
        feedId: Long,
        choice: VoteChoice,
    ): VoteResult {
        voteCount++
        return VoteResult(feedId = feedId, choice = choice, yesCount = 4, noCount = 1, totalCount = 5)
    }

    override suspend fun voteGuestFeed(
        feedId: Long,
        choice: VoteChoice,
    ): VoteResult = voteFeed(feedId, choice)

    override suspend fun getFeedList(
        cursor: Long?,
        size: Int,
        feedStatus: String?,
        category: List<String>?,
    ): FeedList = error("not used")

    override suspend fun getMyFeeds(
        cursor: Long?,
        size: Int,
        feedStatus: String?,
    ): FeedList = error("not used")

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
    /** 정렬별로 돌려줄 첫 페이지. 다음 페이지는 [nextPages]에서 커서로 찾는다. */
    val firstPages = mutableMapOf<CommentSort, CommentPage>()
    val nextPages = mutableMapOf<Long, CommentPage>()
    var createError: Throwable? = null
    var deleteError: Throwable? = null
    var reportError: Throwable? = null
    val getCalls = mutableListOf<Pair<Long?, CommentSort>>()
    val createdContents = mutableListOf<String>()

    /** 목록 응답을 붙잡아 두고 싶을 때 바꿔 끼운다 (늦게 도착하는 응답 흉내). */
    var beforeGet: suspend (cursor: Long?, sort: CommentSort) -> Unit = { _, _ -> }

    override val commentChanges: SharedFlow<Long> = MutableSharedFlow()

    override suspend fun getComments(
        feedId: Long,
        cursor: Long?,
        size: Int,
        sort: CommentSort,
    ): CommentPage {
        getCalls += cursor to sort
        beforeGet(cursor, sort)
        return if (cursor == null) {
            firstPages[sort] ?: CommentPage(emptyList(), nextCursor = null, hasNext = false)
        } else {
            nextPages.getValue(cursor)
        }
    }

    override suspend fun createComment(
        feedId: Long,
        content: String,
    ): Long {
        createError?.let { throw it }
        createdContents += content
        return 101
    }

    override suspend fun deleteComment(
        feedId: Long,
        commentId: Long,
    ) {
        deleteError?.let { throw it }
    }

    override suspend fun reportComment(
        feedId: Long,
        commentId: Long,
    ) {
        reportError?.let { throw it }
    }
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
