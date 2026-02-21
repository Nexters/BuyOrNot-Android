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
        updateState { it.copy(selectedTab = tab, feeds = emptyList(), hasError = false, isLoading = true) }
        loadFeeds()
    }

    private fun handleFilterSelection(filter: FilterChip) {
        updateState { it.copy(selectedFilter = filter, feeds = emptyList(), hasError = false, isLoading = true) }
        loadFeeds()
    }

    private fun handleBannerDismiss() {
        updateState { it.copy(isBannerVisible = false) }
    }

    private fun handleVote(
        feedId: String,
        optionIndex: Int,
    ) {
        // TODO: 서버 연동 시 실제 투표 로직 구현
        viewModelScope.launch {
            val updatedFeeds =
                uiState.value.feeds.map { feed ->
                    if (feed.id == feedId) {
                        feed.copy(userVotedOptionIndex = optionIndex)
                    } else {
                        feed
                    }
                }
            updateState { it.copy(feeds = updatedFeeds) }
        }
    }

    private fun handleDelete(feedId: String) {
        viewModelScope.launch {
            // TODO: 서버 연동 시 실제 삭제 API 호출
            val updatedFeeds = uiState.value.feeds.filter { it.id != feedId }
            updateState { it.copy(feeds = updatedFeeds) }
            sendSideEffect(
                HomeSideEffect.ShowSnackbar(
                    message = "삭제가 완료되었습니다.",
                    icon = BuyOrNotIcons.CheckCircle,
                ),
            )
        }
    }

    private fun handleReport(feedId: String) {
        viewModelScope.launch {
            // TODO: 서버 연동 시 실제 신고 API 호출
            sendSideEffect(
                HomeSideEffect.ShowSnackbar(
                    message = "신고가 완료되었습니다.",
                    icon = BuyOrNotIcons.CheckCircle,
                ),
            )
        }
    }

    /**
     * 피드 데이터 로드 (API 연동)
     */
    private fun loadFeeds() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, hasError = false) }
            try {
                val feeds =
                    when (uiState.value.selectedTab) {
                        HomeTab.FEED -> {
                            // 필터에 따른 feedStatus 파라미터 설정
                            val feedStatus =
                                when (uiState.value.selectedFilter) {
                                    FilterChip.ALL -> null
                                    FilterChip.IN_PROGRESS -> "OPEN"
                                    FilterChip.ENDED -> "CLOSED"
                                }
                            feedRepository.getFeedList(feedStatus = feedStatus)
                        }
                        HomeTab.REVIEW -> feedRepository.getMyFeeds()
                    }

                val feedItems = feeds.map { it.toFeedItem() }
                updateState { it.copy(feeds = feedItems, isLoading = false, hasError = false) }
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false, hasError = true) }
            }
        }
    }

    /**
     * Domain Feed를 UI FeedItem으로 변환
     */
    private fun Feed.toFeedItem(): FeedItem {
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
            isOwner = false, // TODO: 로그인한 사용자 ID와 비교하여 설정
        )
    }
}

