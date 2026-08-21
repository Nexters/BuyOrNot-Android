package com.sseotdabwa.buyornot.feature.home.ui

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent
import com.sseotdabwa.buyornot.core.analytics.performance.Performance
import com.sseotdabwa.buyornot.core.analytics.performance.TraceNames
import com.sseotdabwa.buyornot.core.common.util.TimeUtils
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.FeedStatus
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.NotificationRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
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
    private val notificationRepository: NotificationRepository,
    private val analytics: Analytics,
    private val performance: Performance,
) : BaseViewModel<HomeUiState, HomeIntent, HomeSideEffect>(HomeUiState()) {
    // 최초 피드 로딩 구간만 계측한다. loadFeeds()는 로그인 상태에서 init과
    // userPreferences collect 양쪽에서 겹쳐 호출되므로, 중복 start/stop을 흘려보내는
    // SingleShotPerfTrace에 의존해 첫 구간만 남긴다.
    private val feedFirstLoadTrace = performance.newTrace(TraceNames.FEED_FIRST_LOAD)

    private var currentUserId: Long? = null
    private var isUserIdLoaded = false
    private var unreadCountJob: Job? = null

    // 피드 로딩 요청 세대. 새 로드/새로고침(탭·필터·카테고리 변경 포함) 시 증가시키고,
    // 페이지네이션은 요청 시작 시점의 세대와 일치할 때만 결과를 병합해
    // 진행 중이던 이전 페이지 응답이 최신 목록/커서를 덮어쓰지 않도록 한다. (PR #129 리뷰)
    private var feedGeneration = 0

    init {
        observeUserPreferences()
        observeFeedCreated()
        loadInitialData()
    }

    /**
     * 업로드 완료 후 방금 올린 글이 필터에 가려지지 않도록 내 피드 · 전체로 되돌린다.
     *
     * 업로드는 Home이 살아있는 상태에서 일어나므로 화면 재진입 시점의 라우트 인자로는
     * 감지할 수 없다(같은 탭에서 업로드하면 인자가 그대로여서 변화가 없다). 그래서
     * 네비게이션이 아니라 저장소의 생성 신호를 구독한다.
     *
     * drop(1)은 구독 시작 시 전달되는 StateFlow의 현재 값을 건너뛴다. 이것이 없으면
     * ViewModel이 새로 생성될 때마다 과거 업로드로 탭이 바뀐다.
     */
    private fun observeFeedCreated() {
        viewModelScope.launch {
            feedRepository.feedCreatedRevision
                .drop(1)
                .collect { handleTabSelection(HomeTab.MY_FEED) }
        }
    }

    /**
     * 피드 목록의 첫 프레임이 나간 시점에 UI가 호출한다.
     *
     * 데이터가 상태에 반영된 시점에 끊으면 LazyColumn 컴포지션·레이아웃 비용이 지표에서 빠져
     * 체감 시간보다 짧게 나온다. 실패 경로는 이미 종료된 상태이므로 이 호출이 무시된다.
     */
    fun onFeedFirstContentRendered() {
        feedFirstLoadTrace.stop()
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
                            loadUnreadCount()
                        } else {
                            unreadCountJob?.cancel()
                            currentUserId = null
                            isUserIdLoaded = true
                            updateState { it.copy(selectedTab = HomeTab.FEED, unreadNotificationCount = 0) }
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

    /**
     * 안 읽은 알림 수를 조회해 배지 상태에 반영한다.
     * 로그인(SOCIAL) 상태에서만 호출하며, 실패 시 silent(이전 값 유지, UI 오류 없음).
     */
    private fun loadUnreadCount() {
        if (uiState.value.userType != UserType.SOCIAL) return

        // 겹치는 요청 시 이전 요청을 취소해 오래된 응답이 최신 값을 덮어쓰지 않도록 한다.
        unreadCountJob?.cancel()
        unreadCountJob =
            viewModelScope.launch {
                runCatchingCancellable {
                    notificationRepository.getUnreadCount()
                }.onSuccess { count ->
                    // 로그아웃 등으로 세션이 바뀐 뒤 도착한 응답은 무시한다.
                    if (uiState.value.userType == UserType.SOCIAL) {
                        updateState { it.copy(unreadNotificationCount = count) }
                    }
                }.onFailure { e ->
                    Log.e("HomeViewModel", "Failed to load unread notification count", e)
                }
            }
    }

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.OnTabSelected -> handleTabSelection(intent.tab)
            is HomeIntent.OnFilterSelected -> handleFilterSelection(intent.filter)
            is HomeIntent.OnBannerDismissed -> handleBannerDismiss()
            is HomeIntent.OnVoteClicked -> handleVote(intent.feedId, intent.optionIndex)
            is HomeIntent.ShowDeleteDialog -> updateState { it.copy(showDeleteDialog = true, deletingFeedId = intent.feedId) }
            is HomeIntent.DismissDeleteDialog -> updateState { it.copy(showDeleteDialog = false, deletingFeedId = null) }
            is HomeIntent.OnDeleteConfirmed -> {
                updateState { it.copy(showDeleteDialog = false, deletingFeedId = null) }
                handleDelete(intent.feedId)
            }
            is HomeIntent.OnReportClicked -> handleReport(intent.feedId)
            is HomeIntent.ShowBlockDialog -> handleShowBlockDialog(intent.feedId)
            is HomeIntent.DismissBlockDialog ->
                updateState {
                    it.copy(
                        showBlockDialog = false,
                        blockingNickname = null,
                        blockingUserId = null,
                    )
                }
            is HomeIntent.OnBlockConfirmed -> handleBlockConfirmed()
            is HomeIntent.LoadFeeds -> loadFeeds()
            is HomeIntent.RefreshUnreadCount -> loadUnreadCount()
            is HomeIntent.LoadNextPage -> handleNextPage()
            is HomeIntent.Refresh -> handleRefresh()
            is HomeIntent.OnCategoryToggled -> handleCategoryToggled(intent.category)
            is HomeIntent.OnAllCategorySelected -> {
                updateState { it.copy(selectedCategories = emptySet()) }
                loadFeeds()
            }
            is HomeIntent.ShowSortSheet -> updateState { it.copy(showSortSheet = true) }
            is HomeIntent.DismissSortSheet -> updateState { it.copy(showSortSheet = false) }
            is HomeIntent.DismissTooltip -> updateState { it.copy(isTooltipDismissed = true) }
            is HomeIntent.OnFeedScreenEntered ->
                analytics.track(
                    AnalyticsEvent.FeedViewed(
                        firstVisibleItemIndex = intent.firstVisibleItemIndex,
                    ),
                )
            is HomeIntent.OnFeedScreenExited ->
                analytics.track(
                    AnalyticsEvent.FeedExited(
                        timeSpentSeconds = intent.timeSpentSeconds,
                        lastVisibleItemIndex = intent.lastVisibleItemIndex,
                    ),
                )
        }
    }

    private fun handleTabSelection(tab: HomeTab) {
        if (uiState.value.userType == UserType.GUEST && tab == HomeTab.MY_FEED) return

        updateState {
            it.copy(
                selectedTab = tab,
                selectedCategories = emptySet(),
                selectedFilter = FilterChip.ALL,
                isLoading = true,
                hasError = false,
                feeds = emptyList(),
                allFeeds = emptyList(),
                hasNextPage = false,
                nextCursor = null,
                isNextPageLoading = false,
            )
        }
        loadFeeds(tab = tab)
    }

    private fun handleFilterSelection(filter: FilterChip) {
        updateState {
            it.copy(
                selectedFilter = filter,
                hasError = false,
                hasNextPage = false,
                nextCursor = null,
            )
        }
        loadFeeds(clearFeeds = false)
    }

    private fun handleBannerDismiss() {
        updateState { it.copy(isBannerVisible = false) }
    }

    private fun handleCategoryToggled(category: FeedCategory) {
        updateState { state ->
            val updated =
                if (category in state.selectedCategories) {
                    state.selectedCategories - category
                } else {
                    state.selectedCategories + category
                }
            state.copy(selectedCategories = updated)
        }
        loadFeeds()
    }

    private fun handleNextPage() {
        if (currentState.isNextPageLoading || !currentState.hasNextPage) return

        val requestGeneration = feedGeneration
        viewModelScope.launch {
            updateState { it.copy(isNextPageLoading = true) }
            val requestedTab = currentState.selectedTab
            val requestedFilter = currentState.selectedFilter

            val requestedCategories = currentState.selectedCategories
            val requestedCategory = requestedCategories.map { it.name }.takeIf { it.isNotEmpty() }
            runCatchingCancellable {
                when (requestedTab) {
                    HomeTab.FEED ->
                        feedRepository.getFeedList(
                            cursor = currentState.nextCursor,
                            feedStatus = requestedFilter.toFeedStatus(),
                            category = requestedCategory,
                        )
                    HomeTab.MY_FEED ->
                        feedRepository.getMyFeeds(
                            cursor = currentState.nextCursor,
                            feedStatus = requestedFilter.toFeedStatus(),
                        )
                }
            }.onSuccess { feedList ->
                // 요청 중 새 로드/새로고침/필터·카테고리·탭 변경이 있었으면(세대 불일치)
                // 오래된 페이지 병합과 hasNextPage/nextCursor 갱신을 건너뛴다. (PR #129 리뷰)
                if (feedGeneration != requestGeneration) {
                    updateState { it.copy(isNextPageLoading = false) }
                    return@launch
                }

                val newItems =
                    feedList.feeds.map { feed ->
                        val isOwner = currentUserId != null && feed.author.userId == currentUserId
                        feed.toFeedItem(isOwner)
                    }

                // 커서 기반 페이지네이션에서 페이지 경계가 겹치면 동일 feedId가 중복될 수 있어
                // LazyColumn 중복 key 크래시가 발생한다. id 기준으로 중복을 제거한다. (이슈 #128)
                val newAllFeeds = (currentState.allFeeds + newItems).distinctBy { it.id }

                updateState {
                    it.copy(
                        allFeeds = newAllFeeds,
                        feeds = applyCategories(newAllFeeds, it.selectedCategories),
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
            targetFeed.isOwner || targetFeed.isVoteEnded -> return
            targetFeed.userVotedOptionIndex != null -> return
        }

        // 1. 낙관적 업데이트 (Optimistic Update)
        updateState { state ->
            val newAllFeeds = optimisticVoteUpdate(state.allFeeds, feedId, optionIndex)
            state.copy(
                allFeeds = newAllFeeds,
                feeds = applyCategories(newAllFeeds, state.selectedCategories),
            )
        }

        viewModelScope.launch {
            val choice = if (optionIndex == 0) VoteChoice.YES else VoteChoice.NO

            // UI는 위에서 낙관적으로 이미 갱신됐으므로, 이 trace는 사용자 체감 시간이 아닌
            // 서버 왕복 시간을 재고 롤백이 얼마나 늦게 발생하는지 보기 위한 것이다.
            val voteTrace =
                performance.newTrace(TraceNames.VOTE_REQUEST).apply {
                    putAttribute("user_type", uiState.value.userType.name)
                    putAttribute("choice", choice.name)
                    start()
                }

            runCatchingCancellable {
                when (uiState.value.userType) {
                    UserType.SOCIAL -> feedRepository.voteFeed(feedId.toLong(), choice)
                    UserType.GUEST -> feedRepository.voteGuestFeed(feedId.toLong(), choice)
                }
            }.onSuccess { voteResult ->
                voteTrace.putAttribute("result", "success")
                voteTrace.stop()
                // 2. 최종 업데이트: 서버 응답으로 확정
                updateState { state ->
                    val newAllFeeds =
                        state.allFeeds.map { feed ->
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
                        }
                    state.copy(
                        allFeeds = newAllFeeds,
                        feeds = applyCategories(newAllFeeds, state.selectedCategories),
                    )
                }
                analytics.track(
                    AnalyticsEvent.VoteSubmitted(
                        feedId = targetFeed.id.toLong(),
                        voteChoice = if (optionIndex == 0) "YES" else "NO",
                        feedCategory =
                            FeedCategory.entries
                                .find { it.displayName == targetFeed.category }
                                ?.name
                                ?: targetFeed.category,
                    ),
                )
            }.onFailure { e ->
                voteTrace.putAttribute("result", "error")
                voteTrace.stop()
                Log.e("HomeViewModel", "Failed to vote feed: $feedId", e)
                // 3. 롤백 (Rollback): 해당 피드만 원복, 나머지 동시 변경사항 보존
                updateState { state ->
                    val newAllFeeds =
                        state.allFeeds.map { feed ->
                            if (feed.id == feedId) targetFeed else feed
                        }
                    state.copy(
                        allFeeds = newAllFeeds,
                        feeds = applyCategories(newAllFeeds, state.selectedCategories),
                    )
                }

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
                updateState {
                    it.copy(
                        allFeeds = it.allFeeds.filter { feed -> feed.id != feedId },
                        feeds = it.feeds.filter { feed -> feed.id != feedId },
                    )
                }
                sendSideEffect(
                    HomeSideEffect.ShowSnackbar(
                        message = "삭제가 완료되었습니다.",
                        icon = null,
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

    private fun handleShowBlockDialog(feedId: String) {
        val feed = uiState.value.feeds.find { it.id == feedId } ?: return
        updateState {
            it.copy(
                showBlockDialog = true,
                blockingNickname = feed.nickname,
                blockingUserId = feed.authorUserId,
            )
        }
    }

    private fun handleBlockConfirmed() {
        val userId = uiState.value.blockingUserId ?: return
        val nickname = uiState.value.blockingNickname ?: return
        updateState { it.copy(showBlockDialog = false, blockingNickname = null, blockingUserId = null) }
        viewModelScope.launch {
            runCatchingCancellable {
                userRepository.blockUser(userId)
            }.onSuccess {
                sendSideEffect(
                    HomeSideEffect.ShowSnackbar(
                        message = "${nickname}님이 차단되었어요.",
                        icon = null,
                    ),
                )
                updateState {
                    it.copy(
                        allFeeds = it.allFeeds.filter { feed -> feed.authorUserId != userId },
                        feeds = it.feeds.filter { feed -> feed.authorUserId != userId },
                    )
                }
            }.onFailure { e ->
                Log.e("HomeViewModel", "Failed to block user: $userId", e)
                sendSideEffect(
                    HomeSideEffect.ShowSnackbar(
                        message = "차단에 실패했습니다.",
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
        // 새 로드 컨텍스트 시작 → 진행 중이던 페이지네이션 응답 무효화
        feedGeneration++
        viewModelScope.launch {
            feedFirstLoadTrace.start()
            if (clearFeeds) {
                updateState {
                    it.copy(
                        isLoading = true,
                        hasError = false,
                        feeds = emptyList(),
                        allFeeds = emptyList(),
                    )
                }
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
                val selectedCategories = uiState.value.selectedCategories
                val category = selectedCategories.map { it.name }.takeIf { it.isNotEmpty() }
                when (currentTab) {
                    HomeTab.FEED -> feedRepository.getFeedList(feedStatus = feedStatus, category = category)
                    HomeTab.MY_FEED -> feedRepository.getMyFeeds(feedStatus = feedStatus)
                }
            }.onSuccess { feedList ->
                val newFeeds =
                    feedList.feeds
                        .map { feed ->
                            val isOwner = currentUserId != null && feed.author.userId == currentUserId
                            feed.toFeedItem(isOwner)
                        }
                        // LazyColumn key 유일성 보장: 동일 feedId 중복 방지 (이슈 #128)
                        .distinctBy { it.id }
                updateState {
                    it.copy(
                        allFeeds = newFeeds,
                        feeds = applyCategories(newFeeds, it.selectedCategories),
                        isLoading = false,
                        hasError = false,
                        hasNextPage = feedList.hasNext,
                        nextCursor = feedList.nextCursor,
                    )
                }
                feedFirstLoadTrace.putAttribute("result", "success")
                feedFirstLoadTrace.putMetric("feed_count", newFeeds.size.toLong())
                // stop()은 목록이 실제로 그려진 뒤 onFeedFirstContentRendered()에서 호출한다.
            }.onFailure { e ->
                Log.e("HomeViewModel", "Failed to load feeds", e)
                updateState { it.copy(isLoading = false, hasError = true) }
                feedFirstLoadTrace.putAttribute("result", "error")
                feedFirstLoadTrace.stop()
            }
        }
    }

    /**
     * Pull To Refresh: API를 호출해 데이터를 갱신합니다.
     * isLoading 대신 isRefreshing을 사용해 기존 피드를 유지한 채로 갱신합니다.
     */
    private fun handleRefresh() {
        if (currentState.isRefreshing) return

        // 새로고침도 새 로드 컨텍스트 → 진행 중이던 페이지네이션 응답 무효화
        feedGeneration++
        viewModelScope.launch {
            updateState { it.copy(isRefreshing = true, hasError = false) }

            val currentTab = currentState.selectedTab
            val feedStatus = currentState.selectedFilter.toFeedStatus()
            val selectedCategories = currentState.selectedCategories
            val category = selectedCategories.map { it.name }.takeIf { it.isNotEmpty() }
            runCatchingCancellable {
                when (currentTab) {
                    HomeTab.FEED -> feedRepository.getFeedList(feedStatus = feedStatus, category = category)
                    HomeTab.MY_FEED -> feedRepository.getMyFeeds(feedStatus = feedStatus)
                }
            }.onSuccess { feedList ->
                val refreshedFeeds =
                    feedList.feeds
                        .map { feed ->
                            val isOwner = currentUserId != null && feed.author.userId == currentUserId
                            feed.toFeedItem(isOwner)
                        }
                        // LazyColumn key 유일성 보장: 새로고침 응답의 중복 feedId 방지 (이슈 #128)
                        .distinctBy { it.id }
                updateState {
                    it.copy(
                        allFeeds = refreshedFeeds,
                        feeds = applyCategories(refreshedFeeds, it.selectedCategories),
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
     * allFeeds에 카테고리 필터를 로컬 적용한다.
     * categories가 비어있으면 전체 반환 (전체 = 아무것도 선택 안 됨).
     */
    private fun applyCategories(
        feeds: List<FeedItem>,
        categories: Set<FeedCategory>,
    ): List<FeedItem> =
        if (categories.isEmpty()) {
            feeds
        } else {
            feeds.filter { feed ->
                categories.any { it.displayName == feed.category }
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
    private fun Feed.toFeedItem(isOwner: Boolean): FeedItem {
        val aspectRatios =
            images.map { image ->
                when {
                    image.imageWidth > image.imageHeight -> ImageAspectRatio.LANDSCAPE
                    image.imageWidth < image.imageHeight -> ImageAspectRatio.PORTRAIT
                    else -> ImageAspectRatio.SQUARE
                }
            }

        return FeedItem(
            id = feedId.toString(),
            profileImageUrl = author.profileImage ?: "",
            nickname = author.nickname,
            category = category.displayName,
            createdAt = TimeUtils.formatRelativeTime(createdAt),
            title = title,
            content = content,
            productImageUrls = viewUrls,
            price = price,
            imageAspectRatios = aspectRatios,
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
            authorUserId = author.userId,
            productLink = productLink,
        )
    }
}
