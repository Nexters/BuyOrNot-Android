package com.sseotdabwa.buyornot.feature.home.ui

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.TimeUtils
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedStatus
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeScreen을 위한 ViewModel
 * MVI 패턴을 적용하여 HomeUiState, HomeIntent, HomeSideEffect를 관리합니다.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
) : BaseViewModel<HomeUiState, HomeIntent, HomeSideEffect>(HomeUiState()) {
    private var currentUserId: Long? = null
    private var isUserIdLoaded = false

    init {
        observeUserPreferences()
        loadInitialData()
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            var lastUserType: UserType? = null
            userPreferencesRepository.userPreferences
                .collect { preferences ->
                    val userType = preferences.userType
                    updateState {
                        it.copy(
                            userType = userType,
                            voterProfileImageUrl = preferences.profileImageUrl,
                        )
                    }

                    if (lastUserType != userType) {
                        if (userType == UserType.SOCIAL) {
                            loadUserIdAndRefreshFeeds()
                        } else {
                            currentUserId = null
                            isUserIdLoaded = true
                            updateState { it.copy(selectedTab = HomeTab.FEED) }
                            loadFeeds(tab = HomeTab.FEED)
                        }
                        lastUserType = userType
                    }
                }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            loadCurrentUserIdSuspend()
            isUserIdLoaded = true
            loadFeeds()
        }
    }

    private suspend fun loadCurrentUserIdSuspend() {
        runCatchingCancellable {
            if (uiState.value.userType == UserType.SOCIAL) {
                val profile = userRepository.getMyProfile()
                userPreferencesRepository.updateDisplayName(profile.nickname)
                userPreferencesRepository.updateProfileImageUrl(profile.profileImage)
                profile.id
            } else {
                null
            }
        }.onSuccess { id ->
            currentUserId = id
        }.onFailure { e ->
            Log.e("HomeViewModel", "Failed to load current userId", e)
            currentUserId = null
        }
    }

    private fun loadUserIdAndRefreshFeeds() {
        viewModelScope.launch {
            loadCurrentUserIdSuspend()
            isUserIdLoaded = true
            loadFeeds()
        }
    }

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.OnTabSelected -> handleTabSelection(intent.tab)
            is HomeIntent.OnFilterSelected -> handleFilterSelection(intent.filter)
            is HomeIntent.OnBannerDismissed -> handleBannerDismiss()
            is HomeIntent.OnVoteClicked -> handleVote(intent.feedId, intent.optionIndex)
            is HomeIntent.OnDeleteClicked -> handleDelete(intent.feedId)
            is HomeIntent.OnReportClicked -> handleReport(intent.feedId)
            is HomeIntent.LoadFeeds -> loadFeeds()
            is HomeIntent.LoadNextPage -> handleNextPage()
            is HomeIntent.Refresh -> handleRefresh()
        }
    }

    private fun handleTabSelection(tab: HomeTab) {
        if (uiState.value.userType == UserType.GUEST && tab == HomeTab.MY_FEED) return

        updateState {
            it.copy(
                selectedTab = tab,
                isLoading = true,
                hasError = false,
                feeds = emptyList(),
                hasNextPage = false,
                nextCursor = null,
                isNextPageLoading = false,
            )
        }
        loadFeeds(tab = tab)
    }

    private fun handleFilterSelection(filter: FilterChip) {
        updateState { it.copy(selectedFilter = filter, hasError = false) }
        loadFeeds(clearFeeds = false)
    }

    private fun handleBannerDismiss() {
        updateState { it.copy(isBannerVisible = false) }
    }

    private fun handleNextPage() {
        if (currentState.isNextPageLoading || !currentState.hasNextPage) return

        viewModelScope.launch {
            updateState { it.copy(isNextPageLoading = true) }
            val requestedTab = currentState.selectedTab
            val requestedFilter = currentState.selectedFilter

            runCatchingCancellable {
                when (requestedTab) {
                    HomeTab.FEED ->
                        feedRepository.getFeedList(
                            cursor = currentState.nextCursor,
                            feedStatus = requestedFilter.toFeedStatus(),
                        )
                    HomeTab.MY_FEED ->
                        feedRepository.getMyFeeds(
                            cursor = currentState.nextCursor,
                            feedStatus = requestedFilter.toFeedStatus(),
                        )
                }
            }.onSuccess { feedList ->
                if (currentState.selectedTab != requestedTab) {
                    updateState { it.copy(isNextPageLoading = false) }
                    return@launch
                }

                val newItems =
                    feedList.feeds.map { feed ->
                        val isOwner = currentUserId != null && feed.author.userId == currentUserId
                        feed.toFeedItem(isOwner)
                    }

                updateState {
                    it.copy(
                        feeds = it.feeds + newItems,
                        isNextPageLoading = false,
                        hasNextPage = feedList.hasNext,
                        nextCursor = feedList.nextCursor,
                    )
                }
            }.onFailure { e ->
                Log.e("HomeViewModel", "Failed to load next page", e)
                updateState { it.copy(isNextPageLoading = false) }
            }
        }
    }

    private fun handleVote(
        feedId: String,
        optionIndex: Int,
    ) {
        val targetFeed = uiState.value.feeds.find { it.id == feedId } ?: return

        when {
            targetFeed.isOwner -> {
                sendSideEffect(
                    HomeSideEffect.ShowSnackbar(
                        message = "자신의 글에는 투표할 수 없습니다.",
                        icon = null,
                    ),
                )
                return
            }
            targetFeed.isVoteEnded -> return
            targetFeed.userVotedOptionIndex != null -> return
        }

        val previousFeeds = uiState.value.feeds

        // 1. 낙관적 업데이트 (Optimistic Update)
        updateState { it.copy(feeds = optimisticVoteUpdate(it.feeds, feedId, optionIndex)) }

        viewModelScope.launch {
            val choice = if (optionIndex == 0) VoteChoice.YES else VoteChoice.NO

            runCatchingCancellable {
                when (uiState.value.userType) {
                    UserType.SOCIAL -> feedRepository.voteFeed(feedId.toLong(), choice)
                    UserType.GUEST -> feedRepository.voteGuestFeed(feedId.toLong(), choice)
                }
            }.onSuccess { voteResult ->
                // 2. 최종 업데이트: 서버 응답으로 확정
                updateState {
                    it.copy(
                        feeds =
                            it.feeds.map { feed ->
                                if (feed.id == feedId) {
                                    feed.copy(
                                        userVotedOptionIndex = optionIndex,
                                        buyVoteCount = voteResult.yesCount,
                                        maybeVoteCount = voteResult.noCount,
                                        totalVoteCount = voteResult.totalCount,
                                    )
                                } else {
                                    feed
                                }
                            },
                    )
                }
            }.onFailure { e ->
                Log.e("HomeViewModel", "Failed to vote feed: $feedId", e)
                // 3. 롤백 (Rollback)
                updateState { it.copy(feeds = previousFeeds) }

                val errorMessage =
                    when {
                        e.message?.contains("400") == true -> "이미 투표했거나 마감된 피드입니다."
                        e.message?.contains("404") == true -> "피드를 찾을 수 없습니다."
                        else -> "투표에 실패했습니다."
                    }
                sendSideEffect(
                    HomeSideEffect.ShowSnackbar(
                        message = errorMessage,
                        icon = null,
                    ),
                )
            }
        }
    }

    private fun optimisticVoteUpdate(
        feeds: List<FeedItem>,
        feedId: String,
        optionIndex: Int,
    ): List<FeedItem> =
        feeds.map { feed ->
            if (feed.id != feedId) return@map feed
            val isYes = optionIndex == 0
            feed.copy(
                userVotedOptionIndex = optionIndex,
                buyVoteCount = if (isYes) feed.buyVoteCount + 1 else feed.buyVoteCount,
                maybeVoteCount = if (!isYes) feed.maybeVoteCount + 1 else feed.maybeVoteCount,
                totalVoteCount = feed.totalVoteCount + 1,
            )
        }

    private fun handleDelete(feedId: String) {
        viewModelScope.launch {
            runCatchingCancellable {
                feedRepository.deleteFeed(feedId.toLong())
            }.onSuccess {
                updateState { it.copy(feeds = it.feeds.filter { feed -> feed.id != feedId }) }
                sendSideEffect(
                    HomeSideEffect.ShowSnackbar(
                        message = "삭제가 완료되었습니다.",
                        icon = BuyOrNotIcons.CheckCircle,
                    ),
                )
            }.onFailure { e ->
                Log.e("HomeViewModel", "Failed to delete feed: $feedId", e)
                sendSideEffect(
                    HomeSideEffect.ShowSnackbar(
                        message = "삭제에 실패했습니다.",
                        icon = null,
                    ),
                )
            }
        }
    }

    private fun handleReport(feedId: String) {
        viewModelScope.launch {
            runCatchingCancellable {
                feedRepository.reportFeed(feedId.toLong())
            }.onSuccess {
                sendSideEffect(
                    HomeSideEffect.ShowSnackbar(
                        message = "신고가 완료되었습니다.",
                        icon = BuyOrNotIcons.CheckCircle,
                    ),
                )
            }.onFailure { e ->
                Log.e("HomeViewModel", "Failed to report feed: $feedId", e)
                val errorMessage =
                    when {
                        e.message?.contains("400") == true -> "이미 신고한 피드이거나 본인의 피드입니다."
                        else -> "신고에 실패했습니다."
                    }
                sendSideEffect(
                    HomeSideEffect.ShowSnackbar(
                        message = errorMessage,
                        icon = null,
                    ),
                )
            }
        }
    }

    /**
     * 피드 데이터 로드 (API 연동)
     * 탭/필터 변경 시 모두 API를 호출합니다.
     *
     * @param tab 로드할 탭 (null이면 현재 선택된 탭 사용)
     * @param clearFeeds true면 로딩 중 기존 피드를 비워 전체 로딩 UI 표시, false면 기존 피드 유지
     */
    private fun loadFeeds(
        tab: HomeTab? = null,
        clearFeeds: Boolean = true,
    ) {
        viewModelScope.launch {
            if (clearFeeds) {
                updateState { it.copy(isLoading = true, hasError = false, feeds = emptyList()) }
            } else {
                updateState { it.copy(hasError = false) }
            }

            runCatchingCancellable {
                if (!isUserIdLoaded && uiState.value.userType == UserType.SOCIAL) {
                    loadCurrentUserIdSuspend()
                    isUserIdLoaded = true
                }

                val currentTab = tab ?: uiState.value.selectedTab
                val feedStatus = uiState.value.selectedFilter.toFeedStatus()
                when (currentTab) {
                    HomeTab.FEED -> feedRepository.getFeedList(feedStatus = feedStatus)
                    HomeTab.MY_FEED -> feedRepository.getMyFeeds(feedStatus = feedStatus)
                }
            }.onSuccess { feedList ->
                val feeds =
                    feedList.feeds.map { feed ->
                        val isOwner = currentUserId != null && feed.author.userId == currentUserId
                        feed.toFeedItem(isOwner)
                    }
                updateState {
                    it.copy(
                        feeds = feeds,
                        isLoading = false,
                        hasError = false,
                        hasNextPage = feedList.hasNext,
                        nextCursor = feedList.nextCursor,
                    )
                }
            }.onFailure { e ->
                Log.e("HomeViewModel", "Failed to load feeds", e)
                updateState { it.copy(isLoading = false, hasError = true) }
            }
        }
    }

    /**
     * Pull To Refresh: API를 호출해 데이터를 갱신합니다.
     * isLoading 대신 isRefreshing을 사용해 기존 피드를 유지한 채로 갱신합니다.
     */
    private fun handleRefresh() {
        if (currentState.isRefreshing) return

        viewModelScope.launch {
            updateState { it.copy(isRefreshing = true, hasError = false) }

            val currentTab = currentState.selectedTab
            val feedStatus = currentState.selectedFilter.toFeedStatus()
            runCatchingCancellable {
                when (currentTab) {
                    HomeTab.FEED -> feedRepository.getFeedList(feedStatus = feedStatus)
                    HomeTab.MY_FEED -> feedRepository.getMyFeeds(feedStatus = feedStatus)
                }
            }.onSuccess { feedList ->
                val feeds =
                    feedList.feeds.map { feed ->
                        val isOwner = currentUserId != null && feed.author.userId == currentUserId
                        feed.toFeedItem(isOwner)
                    }
                updateState {
                    it.copy(
                        feeds = feeds,
                        isRefreshing = false,
                        hasNextPage = feedList.hasNext,
                        nextCursor = feedList.nextCursor,
                    )
                }
            }.onFailure { e ->
                Log.e("HomeViewModel", "Failed to refresh feeds", e)
                updateState { it.copy(isRefreshing = false, hasError = true) }
            }
        }
    }

    /**
     * FilterChip을 API feedStatus 파라미터로 변환
     */
    private fun FilterChip.toFeedStatus(): String? =
        when (this) {
            FilterChip.ALL -> null
            FilterChip.IN_PROGRESS -> "OPEN"
            FilterChip.ENDED -> "CLOSED"
        }

    /**
     * Domain Feed를 UI FeedItem으로 변환
     */
    private fun Feed.toFeedItem(isOwner: Boolean = false): FeedItem {
        val aspectRatio =
            if (imageWidth == imageHeight) {
                ImageAspectRatio.SQUARE
            } else if (imageHeight > imageWidth) {
                ImageAspectRatio.PORTRAIT
            } else {
                ImageAspectRatio.SQUARE
            }

        return FeedItem(
            id = feedId.toString(),
            profileImageUrl = author.profileImage ?: "",
            nickname = author.nickname,
            category = category.displayName,
            createdAt = TimeUtils.formatRelativeTime(createdAt),
            content = content,
            productImageUrl = viewUrl,
            price = price,
            imageAspectRatio = aspectRatio,
            isVoteEnded = feedStatus == FeedStatus.CLOSED,
            userVotedOptionIndex =
                when (myVoteChoice) {
                    VoteChoice.YES -> 0
                    VoteChoice.NO -> 1
                    null -> null
                },
            buyVoteCount = yesCount,
            maybeVoteCount = noCount,
            totalVoteCount = totalCount,
            isOwner = isOwner,
        )
    }
}
