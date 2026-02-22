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
import com.sseotdabwa.buyornot.feature.home.viewmodel.FeedItem
import com.sseotdabwa.buyornot.feature.home.viewmodel.FilterChip
import com.sseotdabwa.buyornot.feature.home.viewmodel.HomeIntent
import com.sseotdabwa.buyornot.feature.home.viewmodel.HomeSideEffect
import com.sseotdabwa.buyornot.feature.home.viewmodel.HomeTab
import com.sseotdabwa.buyornot.feature.home.viewmodel.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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
    private var cachedFeeds: List<FeedItem> = emptyList()
    private var currentUserId: Long? = null
    private var isUserIdLoaded = false // ID 로드 완료 여부 추적

    init {
        observeUserType()
        loadInitialData()
    }

    /**
     * 초기 데이터 로드: 사용자 ID를 먼저 로드한 후 피드 로드
     * 경쟁 조건을 방지하기 위해 순차적으로 실행
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            // 먼저 사용자 ID를 동기적으로 로드
            loadCurrentUserIdSuspend()
            isUserIdLoaded = true
            // 사용자 ID 로드 완료 후 피드 로드
            loadFeeds()
        }
    }

    /**
     * suspend 함수로 사용자 ID를 동기적으로 로드
     */
    private suspend fun loadCurrentUserIdSuspend() {
        runCatchingCancellable {
            if (uiState.value.userType == UserType.SOCIAL) {
                userRepository.getMyProfile().id
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

    private fun observeUserType() {
        viewModelScope.launch {
            userPreferencesRepository.userType
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = UserType.GUEST,
                ).collect { userType ->
                    updateState { it.copy(userType = userType) }
                    // 사용자 타입이 변경되면 userId를 다시 로드하고 피드도 갱신
                    if (userType == UserType.SOCIAL) {
                        loadUserIdAndRefreshFeeds()
                    } else {
                        currentUserId = null
                        isUserIdLoaded = true
                        // 게스트 전환 시 탭을 무조건 FEED로 변경
                        updateState { it.copy(selectedTab = HomeTab.FEED) }
                        loadFeeds(HomeTab.FEED)
                    }
                }
        }
    }

    /**
     * 사용자 ID를 로드하고 피드를 갱신
     * 로그인 후 자신의 피드에 대한 isOwner를 올바르게 설정
     */
    private fun loadUserIdAndRefreshFeeds() {
        viewModelScope.launch {
            loadCurrentUserIdSuspend()
            isUserIdLoaded = true
            // 사용자 ID 로드 완료 후 피드 갱신
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
        }
    }

    private fun handleTabSelection(tab: HomeTab) {
        // 게스트일 때는 내 투표 탭 선택 불가
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
        cachedFeeds = emptyList()
        // 탭이 변경되므로 loadFeeds에 명시적으로 탭을 전달
        loadFeeds(tab)
    }

    private fun handleFilterSelection(filter: FilterChip) {
        updateState { it.copy(selectedFilter = filter, hasError = false) }
        applyFiltering()
    }

    private fun handleBannerDismiss() {
        updateState { it.copy(isBannerVisible = false) }
    }

    private fun handleNextPage() {
        if (currentState.isNextPageLoading || !currentState.hasNextPage) return

        viewModelScope.launch {
            updateState { it.copy(isNextPageLoading = true) }
            val requestedTab = currentState.selectedTab

            runCatchingCancellable {
                when (requestedTab) {
                    HomeTab.FEED ->
                        feedRepository.getFeedList(
                            cursor = currentState.nextCursor,
                            feedStatus = null,
                        )
                    HomeTab.MY_FEED ->
                        feedRepository.getMyFeeds(
                            cursor = currentState.nextCursor,
                            feedStatus = null,
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
                cachedFeeds = cachedFeeds + newItems

                updateState {
                    it.copy(
                        isNextPageLoading = false,
                        hasNextPage = feedList.hasNext,
                        nextCursor = feedList.nextCursor,
                    )
                }
                applyFiltering()
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
        val previousFeeds = uiState.value.feeds
        val previousCachedFeeds = cachedFeeds

        // 1. 낙관적 업데이트 (Optimistic Update)
        // API 호출 전 UI를 즉시 업데이트하여 사용자 경험 개선
        val optimisticUpdate = { feeds: List<FeedItem> ->
            feeds.map { feed ->
                if (feed.id == feedId) {
                    val isYes = optionIndex == 0
                    feed.copy(
                        userVotedOptionIndex = optionIndex,
                        buyVoteCount = if (isYes) feed.buyVoteCount + 1 else feed.buyVoteCount,
                        maybeVoteCount = if (!isYes) feed.maybeVoteCount + 1 else feed.maybeVoteCount,
                        totalVoteCount = feed.totalVoteCount + 1,
                    )
                } else {
                    feed
                }
            }
        }

        updateState { it.copy(feeds = optimisticUpdate(it.feeds)) }
        cachedFeeds = optimisticUpdate(cachedFeeds)

        viewModelScope.launch {
            // optionIndex: 0 = YES, 1 = NO
            val choice = if (optionIndex == 0) VoteChoice.YES else VoteChoice.NO

            runCatchingCancellable {
                // 사용자 타입에 따라 회원/비회원 투표 API 호출
                when (uiState.value.userType) {
                    UserType.SOCIAL -> feedRepository.voteFeed(feedId.toLong(), choice)
                    UserType.GUEST -> feedRepository.voteGuestFeed(feedId.toLong(), choice)
                }
            }.onSuccess { voteResult ->
                // 2. 최종 업데이트: 서버 응답 데이터를 기반으로 UI 확정
                val finalUpdate = { feeds: List<FeedItem> ->
                    feeds.map { feed ->
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
                }

                updateState { it.copy(feeds = finalUpdate(it.feeds)) }
                cachedFeeds = finalUpdate(cachedFeeds)
            }.onFailure { e ->
                Log.e("HomeViewModel", "Failed to vote feed: $feedId", e)
                // 3. 롤백 (Rollback): 에러 발생 시 원래 상태로 복구
                updateState { it.copy(feeds = previousFeeds) }
                cachedFeeds = previousCachedFeeds

                // 에러 발생 시 스낵바로 알림
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

    private fun handleDelete(feedId: String) {
        viewModelScope.launch {
            runCatchingCancellable {
                feedRepository.deleteFeed(feedId.toLong())
            }.onSuccess {
                // UI에서 피드 제거
                val updatedFeeds = uiState.value.feeds.filter { it.id != feedId }
                updateState { it.copy(feeds = updatedFeeds) }

                // 캐시에서도 제거
                cachedFeeds = cachedFeeds.filter { it.id != feedId }

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
                // 400 에러 (본인 피드 또는 이미 신고된 피드)에 대한 처리
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
     * @param tab 로드할 탭 (null이면 현재 선택된 탭 사용)
     */
    private fun loadFeeds(tab: HomeTab? = null) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, hasError = false) }

            runCatchingCancellable {
                // ID가 아직 로드되지 않았으면 먼저 로드
                if (!isUserIdLoaded && uiState.value.userType == UserType.SOCIAL) {
                    loadCurrentUserIdSuspend()
                    isUserIdLoaded = true
                }

                val currentTab = tab ?: uiState.value.selectedTab
                // 필터 없이 해당 탭의 전체 데이터를 가져옴
                when (currentTab) {
                    HomeTab.FEED -> feedRepository.getFeedList(feedStatus = null)
                    HomeTab.MY_FEED -> feedRepository.getMyFeeds(feedStatus = null)
                }
            }.onSuccess { feedList ->
                // 원본 데이터를 캐시에 저장
                // 각 피드의 작성자 ID와 현재 사용자 ID를 비교하여 isOwner 설정
                cachedFeeds =
                    feedList.feeds.map { feed ->
                        val isOwner = currentUserId != null && feed.author.userId == currentUserId
                        feed.toFeedItem(isOwner)
                    }

                updateState {
                    it.copy(
                        hasNextPage = feedList.hasNext,
                        nextCursor = feedList.nextCursor,
                    )
                }

                // 현재 선택된 필터에 맞춰 UI 상태 업데이트
                // 탭 파라미터를 전달하여 즉시 반영되도록 함
                applyFiltering(tab ?: uiState.value.selectedTab)
            }.onFailure { e ->
                Log.e("HomeViewModel", "Failed to load feeds", e)
                updateState { it.copy(isLoading = false, hasError = true) }
            }
        }
    }

    /**
     * 피드 필터링 적용
     * @param tab 필터링할 탭 (null이면 현재 선택된 탭 사용)
     */
    private fun applyFiltering(tab: HomeTab? = null) {
        val currentFilter = uiState.value.selectedFilter
        val currentTab = tab ?: uiState.value.selectedTab

        // 1단계: 필터 칩에 따라 필터링
        val chipFilteredList =
            when (currentFilter) {
                FilterChip.ALL -> cachedFeeds
                FilterChip.IN_PROGRESS -> cachedFeeds.filter { !it.isVoteEnded }
                FilterChip.ENDED -> cachedFeeds.filter { it.isVoteEnded }
            }

        // 2단계: 탭에 따라 추가 필터링
        val finalFilteredList =
            when (currentTab) {
                HomeTab.FEED -> chipFilteredList // 투표 피드: 모든 피드 표시
                HomeTab.MY_FEED -> chipFilteredList.filter { it.isOwner } // 내 투표: 본인 피드만 표시
            }

        updateState { it.copy(feeds = finalFilteredList, isLoading = false, hasError = false) }
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
            price = price.toString(),
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
