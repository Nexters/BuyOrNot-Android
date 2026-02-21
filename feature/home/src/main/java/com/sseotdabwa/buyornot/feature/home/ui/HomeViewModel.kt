package com.sseotdabwa.buyornot.feature.home.ui

import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.feature.home.viewmodel.FeedItem
import com.sseotdabwa.buyornot.feature.home.viewmodel.HomeIntent
import com.sseotdabwa.buyornot.feature.home.viewmodel.HomeSideEffect
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
) : BaseViewModel<HomeUiState, HomeIntent, HomeSideEffect>(HomeUiState()) {

    init {
        observeUserType()
        loadDummyFeeds()
    }

    private fun observeUserType() {
        viewModelScope.launch {
            userPreferencesRepository.userType
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = UserType.GUEST
                )
                .collect { userType ->
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
            is HomeIntent.LoadFeeds -> loadDummyFeeds()
        }
    }

    private fun handleTabSelection(tab: com.sseotdabwa.buyornot.feature.home.viewmodel.HomeTab) {
        updateState { it.copy(selectedTab = tab) }
    }

    private fun handleFilterSelection(filter: com.sseotdabwa.buyornot.feature.home.viewmodel.FilterChip) {
        updateState { it.copy(selectedFilter = filter) }
    }

    private fun handleBannerDismiss() {
        updateState { it.copy(isBannerVisible = false) }
    }

    private fun handleVote(feedId: String, optionIndex: Int) {
        // TODO: 서버 연동 시 실제 투표 로직 구현
        viewModelScope.launch {
            val updatedFeeds = uiState.value.feeds.map { feed ->
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
                    icon = BuyOrNotIcons.CheckCircle
                )
            )
        }
    }

    private fun handleReport(feedId: String) {
        viewModelScope.launch {
            // TODO: 서버 연동 시 실제 신고 API 호출 (feedId 사용)
            sendSideEffect(
                HomeSideEffect.ShowSnackbar(
                    message = "신고가 완료되었습니다.",
                    icon = BuyOrNotIcons.CheckCircle
                )
            )
        }
    }

    /**
     * 더미 피드 데이터 로드 (임시)
     * TODO: 서버 연동 시 실제 피드 데이터를 가져오는 로직으로 교체
     */
    private fun loadDummyFeeds() {
        val dummyFeeds = List(15) { index ->
            FeedItem(
                id = "feed_$index",
                profileImageUrl = "https://picsum.photos/seed/p$index/200/200",
                nickname = if (index % 3 == 0) "나" else "유저 $index",
                category = listOf("의류", "뷰티", "디지털", "식품")[index % 4],
                createdAt = "${index + 1}시간 전",
                content = "이 제품 살까요 말까요? 고민되네요!",
                productImageUrl = "https://picsum.photos/seed/item$index/800/${if (index % 2 == 0) 800 else 1000}",
                price = "${(index + 1) * 10000}",
                imageAspectRatio = if (index % 2 == 0) ImageAspectRatio.SQUARE else ImageAspectRatio.PORTRAIT,
                isVoteEnded = index % 5 == 0,
                userVotedOptionIndex = if (index % 3 == 0) index % 2 else null,
                buyVoteCount = 40 + index * 5,
                maybeVoteCount = 20 + index * 2,
                totalVoteCount = 60 + index * 7,
                isOwner = index % 3 == 0,
            )
        }
        updateState { it.copy(feeds = dummyFeeds) }
    }
}
