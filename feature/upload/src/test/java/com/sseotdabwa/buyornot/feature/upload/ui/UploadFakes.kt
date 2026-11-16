package com.sseotdabwa.buyornot.feature.upload.ui

import android.net.Uri
import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent
import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedAiDraft
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.FeedImage
import com.sseotdabwa.buyornot.domain.model.ImageSource
import com.sseotdabwa.buyornot.domain.model.UploadInfo
import com.sseotdabwa.buyornot.domain.model.UserPreferences
import com.sseotdabwa.buyornot.domain.model.UserToken
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import com.sseotdabwa.buyornot.domain.model.VoteResult
import com.sseotdabwa.buyornot.domain.repository.FeedList
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.HotFeedList
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.feature.upload.upload.FeedImageUploader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class FakeFeedImageUploader : FeedImageUploader {
    val uploaded = mutableListOf<Uri>()

    override suspend fun upload(uri: Uri): FeedImage {
        uploaded += uri
        return FeedImage(s3ObjectKey = "key-$uri", image = ImageSource("https://cdn/$uri"), imageWidth = 100, imageHeight = 100)
    }
}

data class CreatedFeed(
    val category: FeedCategory,
    val price: Int,
    val content: String,
    val imageKeys: List<String>,
    val title: String?,
)

class FakeFeedRepository : FeedRepository {
    var aiDraft: FeedAiDraft = FeedAiDraft(title = null, content = null, category = null, price = null)
    var aiDraftError: Throwable? = null

    /** 초안 응답을 붙잡아 두고 싶을 때 바꿔 끼운다. */
    var beforeAiDraft: suspend () -> Unit = {}
    val aiDraftRequests = mutableListOf<List<String>>()
    val createdFeeds = mutableListOf<CreatedFeed>()

    override val feedCreatedRevision: StateFlow<Long> = MutableStateFlow(0L)

    override suspend fun createAiDraft(s3ObjectKeys: List<String>): FeedAiDraft {
        aiDraftRequests += s3ObjectKeys
        beforeAiDraft()
        aiDraftError?.let { throw it }
        return aiDraft
    }

    override suspend fun createFeed(
        category: FeedCategory,
        price: Int,
        content: String,
        images: List<FeedImage>,
        title: String?,
        link: String?,
    ): Long {
        createdFeeds += CreatedFeed(category, price, content, images.map { it.s3ObjectKey }, title)
        return 1L
    }

    override suspend fun getFeedList(
        cursor: Long?,
        size: Int,
        feedStatus: String?,
        category: List<String>?,
    ): FeedList = error("not used")

    override suspend fun getHotFeedList(
        cursor: String?,
        size: Int,
        category: List<String>?,
    ): HotFeedList = error("not used")

    override suspend fun getFeed(feedId: Long): Feed = error("not used")

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

    override suspend fun deleteFeed(feedId: Long) = Unit

    override suspend fun reportFeed(feedId: Long) = Unit

    override suspend fun voteFeed(
        feedId: Long,
        choice: VoteChoice,
    ): VoteResult = error("not used")
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
    override val userPreferences: Flow<UserPreferences> = emptyFlow()
    override val userToken: Flow<UserToken> = emptyFlow()
    override val userType: Flow<UserType> = flowOf(UserType.SOCIAL)
    override val userId: Flow<Long> = flowOf(1L)

    override suspend fun updateUserType(userType: UserType) = Unit

    override suspend fun updateUserId(userId: Long) = Unit

    override suspend fun updateDisplayName(newName: String) = Unit

    override suspend fun updateProfileImage(newImage: ImageSource) = Unit
}

class FakeAnalytics : Analytics {
    val events = mutableListOf<AnalyticsEvent>()

    override fun track(event: AnalyticsEvent) {
        events += event
    }

    override fun identify(userId: String?) = Unit
}
