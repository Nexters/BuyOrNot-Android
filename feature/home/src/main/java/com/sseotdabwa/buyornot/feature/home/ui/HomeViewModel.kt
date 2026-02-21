package com.sseotdabwa.buyornot.feature.home.ui

import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.Feed
import com.sseotdabwa.buyornot.domain.model.FeedStatus
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.model.VoteChoice
import com.sseotdabwa.buyornot.domain.repository.FeedRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
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
) : BaseViewModel<HomeUiState, HomeIntent, HomeSideEffect>(HomeUiState()) {
    private var cachedFeeds: List<FeedItem> = emptyList()

    init {
        observeUserType()
        loadFeeds()
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
                }
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
        }
    }

    private fun handleTabSelection(tab: HomeTab) {
        //updateState { it.copy(selectedTab = tab, feeds = emptyList(), hasError = false, isLoading = true) }
        updateState { it.copy(selectedTab = tab, isLoading = true) }
        loadFeeds()
    }

    private fun handleFilterSelection(filter: FilterChip) {
        updateState { it.copy(selectedFilter = filter) }
        applyFiltering()
    }

    private fun handleBannerDismiss() {
        updateState { it.copy(isBannerVisible = false) }
    }

    private fun handleVote(
        feedId: String,
        optionIndex: Int,
    ) {
        viewModelScope.launch {
            try {
                // optionIndex: 0 = YES, 1 = NO
                val choice = if (optionIndex == 0) VoteChoice.YES else VoteChoice.NO

                // 사용자 타입에 따라 회원/비회원 투표 API 호출
                val voteResult =
                    when (uiState.value.userType) {
                        UserType.SOCIAL -> feedRepository.voteFeed(feedId.toLong(), choice)
                        UserType.GUEST -> feedRepository.voteGuestFeed(feedId.toLong(), choice)
                    }

                // UI 업데이트: 투표 결과를 반영
                val updatedFeeds =
                    uiState.value.feeds.map { feed ->
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
                updateState { it.copy(feeds = updatedFeeds) }

                // 캐시도 업데이트
                cachedFeeds =
                    cachedFeeds.map { feed ->
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
            } catch (e: Exception) {
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
            try {
                feedRepository.deleteFeed(feedId.toLong())

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
            } catch (e: Exception) {
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
            try {
                feedRepository.reportFeed(feedId.toLong())
                sendSideEffect(
                    HomeSideEffect.ShowSnackbar(
                        message = "신고가 완료되었습니다.",
                        icon = BuyOrNotIcons.CheckCircle,
                    ),
                )
            } catch (e: Exception) {
                // 400 에러 (본인 피드 또는 이미 신고된 피드)에 대한 처리
                val errorMessage = when {
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
     */
    private fun loadFeeds() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, hasError = false) }
            try {
                val currentTab = uiState.value.selectedTab
                // 필터 없이 해당 탭의 전체 데이터를 가져옴
                val feeds = when (currentTab) {
                    HomeTab.FEED -> feedRepository.getFeedList(feedStatus = null) // 전체 가져오기
                    HomeTab.REVIEW -> feedRepository.getMyFeeds()
                }

                // 원본 데이터를 캐시에 저장
                // REVIEW 탭일 때는 모든 피드가 본인 피드이므로 isOwner = true
                val isMyFeed = currentTab == HomeTab.REVIEW
                cachedFeeds = feeds.map { it.toFeedItem(isMyFeed) }

                // 현재 선택된 필터에 맞춰 UI 상태 업데이트
                applyFiltering()
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false, hasError = true) }
            }
        }
    }

    private fun applyFiltering() {
        val currentFilter = uiState.value.selectedFilter

        val filteredList = when (currentFilter) {
            FilterChip.ALL -> cachedFeeds
            FilterChip.IN_PROGRESS -> cachedFeeds.filter { !it.isVoteEnded }
            FilterChip.ENDED -> cachedFeeds.filter { it.isVoteEnded }
        }

        updateState { it.copy(feeds = filteredList, isLoading = false) }
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
            category = category,
            createdAt = createdAt,
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

