package com.sseotdabwa.buyornot.feature.home.ui

import androidx.compose.runtime.Immutable
import com.sseotdabwa.buyornot.core.designsystem.components.FeedCommentPreview
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.GUEST_AUTHOR_USER_ID
import com.sseotdabwa.buyornot.domain.model.UserType

/**
 * 홈 화면의 탭 정의
 */
enum class HomeTab { FEED, MY_FEED }

/**
 * 필터 칩의 종류
 */
enum class FilterChip(
    val label: String,
) {
    ALL("전체"),
    IN_PROGRESS("진행중 투표"),
    ENDED("마감된 투표"),
}

/**
 * 피드 아이템 데이터 모델
 */
@Immutable
data class FeedItem(
    val id: String,
    val profileImageUrl: String,
    val nickname: String,
    val category: String,
    val createdAt: String,
    val title: String,
    val content: String,
    val productImageUrls: List<String>,
    val price: String,
    val imageAspectRatios: List<ImageAspectRatio>,
    val isVoteEnded: Boolean,
    val userVotedOptionIndex: Int?,
    val buyVoteCount: Int,
    val maybeVoteCount: Int,
    val totalVoteCount: Int,
    val isOwner: Boolean,
    val authorUserId: Long,
    val productLink: String? = null,
    val commentCount: Int = 0,
    /** 댓글이 없으면 null — 카드의 thread 라인과 미리보기를 숨긴다. */
    val latestComment: FeedCommentPreview? = null,
) {
    /** 비회원 작성 글. 차단 대상 유저가 없다 — [GUEST_AUTHOR_USER_ID] 참고. */
    val isGuestAuthor: Boolean get() = authorUserId == GUEST_AUTHOR_USER_ID
}

/**
 * 홈 화면의 UI 상태 (MVI State)
 *
 * @property userType 현재 사용자 타입 (GUEST/SOCIAL)
 * @property selectedTab 현재 선택된 탭
 * @property selectedFilter 현재 선택된 필터 칩
 * @property isBannerVisible 배너 표시 여부
 * @property feeds 피드 목록
 * @property isLoading 로딩 상태
 * @property isNextPageLoading 다음 페이지 로딩 상태
 * @property hasNextPage 다음 페이지 존재 여부
 * @property nextCursor 다음 페이지를 위한 커서
 * @property hasError 에러 발생 여부
 */
@Immutable
data class HomeUiState(
    val userType: UserType = UserType.GUEST,
    val selectedTab: HomeTab = HomeTab.FEED,
    val selectedFilter: FilterChip = FilterChip.ALL,
    val isBannerVisible: Boolean = true,
    val voterProfileImageUrl: String = "",
    val allFeeds: List<FeedItem> = emptyList(),
    val selectedCategories: Set<FeedCategory> = emptySet(),
    val feeds: List<FeedItem> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isNextPageLoading: Boolean = false,
    val hasNextPage: Boolean = false,
    val nextCursor: Long? = null,
    val hasError: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deletingFeedId: String? = null,
    val showBlockDialog: Boolean = false,
    val blockingNickname: String? = null,
    val blockingUserId: Long? = null,
    val showSortSheet: Boolean = false,
    val isTooltipDismissed: Boolean = false,
    val unreadNotificationCount: Int = 0,
)

/**
 * 홈 화면에서 발생하는 사용자 액션 (MVI Intent)
 */
sealed interface HomeIntent {
    data class OnTabSelected(
        val tab: HomeTab,
    ) : HomeIntent

    data class OnFilterSelected(
        val filter: FilterChip,
    ) : HomeIntent

    data object OnBannerDismissed : HomeIntent

    data class OnVoteClicked(
        val feedId: String,
        val optionIndex: Int,
    ) : HomeIntent

    data class ShowDeleteDialog(
        val feedId: String,
    ) : HomeIntent

    data object DismissDeleteDialog : HomeIntent

    data class OnDeleteConfirmed(
        val feedId: String,
    ) : HomeIntent

    data class OnReportClicked(
        val feedId: String,
    ) : HomeIntent

    /** 카드의 댓글 수·최신 댓글 미리보기 탭 */
    data class OnCommentClicked(
        val feedId: String,
    ) : HomeIntent

    data class OnShareClicked(
        val feedId: String,
        val isOwner: Boolean,
    ) : HomeIntent

    data object LoadFeeds : HomeIntent

    data object RefreshUnreadCount : HomeIntent

    data object LoadNextPage : HomeIntent

    data object Refresh : HomeIntent

    data class ShowBlockDialog(
        val feedId: String,
    ) : HomeIntent

    data object DismissBlockDialog : HomeIntent

    data object OnBlockConfirmed : HomeIntent

    data class OnCategoryToggled(
        val category: FeedCategory,
    ) : HomeIntent

    data object OnAllCategorySelected : HomeIntent

    data object ShowSortSheet : HomeIntent

    data object DismissSortSheet : HomeIntent

    data object DismissTooltip : HomeIntent

    data class OnFeedScreenEntered(
        val firstVisibleItemIndex: Int,
    ) : HomeIntent

    data class OnFeedScreenExited(
        val lastVisibleItemIndex: Int,
        val timeSpentSeconds: Float,
    ) : HomeIntent
}

/**
 * 홈 화면의 일회성 이벤트 (MVI SideEffect)
 */
sealed interface HomeSideEffect {
    data class ShowSnackbar(
        val message: String,
        val icon: IconResource? = null,
    ) : HomeSideEffect

    data object NavigateToNotification : HomeSideEffect

    data object NavigateToProfile : HomeSideEffect

    data object NavigateToUpload : HomeSideEffect

    /**
     * 투표 완료 스낵바 — [의견 남기기]를 누르면 댓글 입력창으로 이동한다.
     * @property canComment 비회원은 댓글을 쓸 수 없어 [의견 남기기]를 숨긴다.
     */
    data class ShowVoteCompletedSnackbar(
        val feedId: Long,
        val imageUrl: String?,
        val canComment: Boolean,
    ) : HomeSideEffect

    data class NavigateToFeedComments(
        val feedId: Long,
        val focusCommentInput: Boolean,
    ) : HomeSideEffect
}
