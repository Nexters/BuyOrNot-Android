package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import com.sseotdabwa.buyornot.domain.model.BlockedUser
import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.FeedImage
import com.sseotdabwa.buyornot.domain.model.UploadInfo
import com.sseotdabwa.buyornot.domain.model.UserPreferences
import com.sseotdabwa.buyornot.domain.model.UserProfile
import com.sseotdabwa.buyornot.domain.model.UserToken
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import com.sseotdabwa.buyornot.domain.model.VoteResult
import com.sseotdabwa.buyornot.domain.repository.FeedList
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import com.sseotdabwa.buyornot.feature.mypage.image.ProfileImageFile
import com.sseotdabwa.buyornot.feature.mypage.image.ProfileImageReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

/** 프로필 수정 흐름에서 호출된 순서를 기록한다. */
class CallLog {
    val calls = mutableListOf<String>()
}

class FakeUserRepository(
    private val log: CallLog,
    var profile: UserProfile =
        UserProfile(
            id = 1,
            nickname = "서따봐",
            profileImage = "https://cdn/old.jpg",
            socialAccount = "KAKAO",
            email = "buyornot@gmail.com",
        ),
) : UserRepository {
    var updateError: Throwable? = null

    /** 기본은 [profile]을 바로 돌려준다. 지연·실패를 흉내 낼 때 바꿔 끼운다. */
    var loadProfile: suspend () -> UserProfile = { profile }
    var updatedNickname: String? = null
    var updatedProfileImage: String? = null
    var updateCount = 0

    override suspend fun getMyProfile(): UserProfile = loadProfile()

    override suspend fun updateProfile(
        nickname: String?,
        profileImage: String?,
    ): UserProfile {
        log.calls += "updateProfile"
        updateCount++
        updatedNickname = nickname
        updatedProfileImage = profileImage
        updateError?.let { throw it }
        profile =
            profile.copy(
                nickname = nickname ?: profile.nickname,
                profileImage = profileImage ?: profile.profileImage,
            )
        return profile
    }

    override suspend fun deleteMyAccount() = Unit

    override suspend fun updateFcmToken(fcmToken: String) = Unit

    override suspend fun getBlockedUsers(): List<BlockedUser> = emptyList()

    override suspend fun blockUser(userId: Long) = Unit

    override suspend fun unblockUser(userId: Long) = Unit

    override suspend fun notifyAppOpened() = Unit
}

class FakeFeedRepository(
    private val log: CallLog,
) : FeedRepository {
    var uploadedUrl: String? = null
    var uploadedBytes: ByteArray? = null

    override suspend fun getPresignedUrl(
        fileName: String,
        contentType: String,
    ): UploadInfo {
        log.calls += "getPresignedUrl"
        return UploadInfo(uploadUrl = "https://s3/put", s3ObjectKey = "key", viewUrl = "https://cdn/new.jpg")
    }

    override suspend fun uploadImage(
        url: String,
        bytes: ByteArray,
        contentType: String,
    ) {
        log.calls += "uploadImage"
        uploadedUrl = url
        uploadedBytes = bytes
    }

    override val feedCreatedRevision: StateFlow<Long> = MutableStateFlow(0L)

    override suspend fun getFeedList(
        cursor: Long?,
        size: Int,
        feedStatus: String?,
        category: List<String>?,
    ): FeedList = error("not used")

    override suspend fun getFeed(feedId: Long): Feed = error("not used")

    override suspend fun getMyFeeds(
        cursor: Long?,
        size: Int,
        feedStatus: String?,
    ): FeedList = error("not used")

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

    override suspend fun voteFeed(
        feedId: Long,
        choice: VoteChoice,
    ): VoteResult = error("not used")

    override suspend fun voteGuestFeed(
        feedId: Long,
        choice: VoteChoice,
    ): VoteResult = error("not used")
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
    var displayName: String? = null
    var profileImageUrl: String? = null

    override val userPreferences: Flow<UserPreferences> = emptyFlow()
    override val userToken: Flow<UserToken> = emptyFlow()
    override val userType: Flow<UserType> = emptyFlow()
    override val userId: Flow<Long> = emptyFlow()

    override suspend fun updateUserType(userType: UserType) = Unit

    override suspend fun updateUserId(userId: Long) = Unit

    override suspend fun updateDisplayName(newName: String) {
        displayName = newName
    }

    override suspend fun updateProfileImageUrl(newUrl: String) {
        profileImageUrl = newUrl
    }
}

class FakeProfileImageReader(
    private val log: CallLog,
) : ProfileImageReader {
    val bytes = byteArrayOf(1, 2, 3)

    override suspend fun read(uri: String): ProfileImageFile {
        log.calls += "read"
        return ProfileImageFile(bytes = bytes, fileName = "cropped.jpg", contentType = "image/jpeg")
    }
}
