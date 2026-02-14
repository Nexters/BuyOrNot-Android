package com.sseotdabwa.buyornot.feature.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotChip
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotDivider
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotDividerSize
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotSnackBarHost
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotTab
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotTabRow
import com.sseotdabwa.buyornot.core.designsystem.components.ExpandableFloatingActionButton
import com.sseotdabwa.buyornot.core.designsystem.components.FabOption
import com.sseotdabwa.buyornot.core.designsystem.components.FeedCard
import com.sseotdabwa.buyornot.core.designsystem.components.HomeTopBar
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.components.showBuyOrNotSnackBar
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 홈 화면의 탭 정의
 */
internal enum class HomeTab { FEED, REVIEW }

/**
 * 필터 칩의 종류
 */
internal enum class FilterChip(
    val label: String,
) {
    ALL("전체"),
    IN_PROGRESS("진행중 투표"),
    ENDED("마감된 투표"),
}

/**
 * 홈 화면의 UI 상태를 관리하는 데이터 클래스 (MVI State)
 *
 * @property selectedTab 현재 선택된 탭
 * @property selectedFilter 현재 선택된 필터 칩
 * @property isBannerVisible 배너 표시 여부
 * @property feeds 피드 목록 (실제로는 ViewModel에서 관리)
 */
@Immutable
internal data class HomeUiState(
    val selectedTab: HomeTab = HomeTab.FEED,
    val selectedFilter: FilterChip = FilterChip.ALL,
    val isBannerVisible: Boolean = true,
    val feeds: List<FeedItem> = emptyList(),
)

/**
 * 피드 아이템 데이터 모델
 */
@Immutable
internal data class FeedItem(
    val id: String,
    val profileImageUrl: String,
    val nickname: String,
    val category: String,
    val createdAt: String,
    val content: String,
    val productImageUrl: String,
    val price: String,
    val imageAspectRatio: ImageAspectRatio,
    val isVoteEnded: Boolean,
    val userVotedOptionIndex: Int?,
    val buyVoteCount: Int,
    val maybeVoteCount: Int,
    val totalVoteCount: Int,
    // 본인 게시글 여부
    val isOwner: Boolean = false,
)

/**
 * 홈 화면에서 발생하는 사용자 액션 (MVI Intent)
 */
internal sealed interface HomeIntent {
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

    data class OnImageExpandClicked(
        val imageUrl: String,
    ) : HomeIntent

    data object OnNotificationClicked : HomeIntent

    data object OnProfileClicked : HomeIntent

    data object OnCreateVoteClicked : HomeIntent

    data object OnCreateReviewClicked : HomeIntent

    data class OnDeleteClicked(
        val feedId: String,
    ) : HomeIntent

    data class OnReportClicked(
        val feedId: String,
    ) : HomeIntent
}

/**
 * 홈 화면 루트 컴포저블
 * TODO: ViewModel을 통한 상태 관리로 전환 필요
 */
@Composable
fun HomeScreen() {
    // TODO: 실제로는 ViewModel에서 가져올 데이터 (임시 더미 데이터)
    val dummyFeeds =
        remember {
            List(15) { index ->
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
                    isOwner = index % 3 == 0, // 3개 중 1개는 내 게시글
                )
            }
        }

    // TODO: ViewModel에서 가져올 상태 (현재는 임시로 remember 사용)
    var uiState by rememberSaveable(stateSaver = homeUiStateSaver()) {
        mutableStateOf(HomeUiState(feeds = dummyFeeds))
    }

    // 화면 전용 일시적 상태 (ViewModel에서 관리하지 않음)
    var isFabExpanded by remember { mutableStateOf(false) }
    var expandedImageUrl by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    HomeScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        isFabExpanded = isFabExpanded,
        expandedImageUrl = expandedImageUrl,
        onIntent = { intent ->
            // TODO: ViewModel Intent 처리로 전환
            when (intent) {
                is HomeIntent.OnTabSelected -> {
                    uiState = uiState.copy(selectedTab = intent.tab)
                }
                is HomeIntent.OnFilterSelected -> {
                    uiState = uiState.copy(selectedFilter = intent.filter)
                }
                is HomeIntent.OnBannerDismissed -> {
                    uiState = uiState.copy(isBannerVisible = false)
                }
                is HomeIntent.OnImageExpandClicked -> {
                    expandedImageUrl = intent.imageUrl
                }
                is HomeIntent.OnVoteClicked -> {
                    // TODO: ViewModel에서 처리
                }
                is HomeIntent.OnNotificationClicked -> {
                    // TODO: 알림 화면으로 이동
                }
                is HomeIntent.OnProfileClicked -> {
                    // TODO: 프로필 화면으로 이동
                }
                is HomeIntent.OnCreateVoteClicked -> {
                    // TODO: 투표 생성 화면으로 이동
                }
                is HomeIntent.OnCreateReviewClicked -> {
                    // TODO: 리뷰 생성 화면으로 이동
                }
                is HomeIntent.OnDeleteClicked -> {
                    // 삭제 처리 (임시: 리스트에서 제거)
                    uiState =
                        uiState.copy(
                            feeds = uiState.feeds.filter { it.id != intent.feedId },
                        )
                    scope.launch {
                        showBuyOrNotSnackBar(snackbarHostState, "삭제가 완료되었습니다.", BuyOrNotIcons.CheckCircle)
                    }
                }
                is HomeIntent.OnReportClicked -> {
                    scope.launch {
                        showBuyOrNotSnackBar(
                            snackbarHostState = snackbarHostState,
                            message = "신고가 완료되었습니다.",
                            iconResource = BuyOrNotIcons.CheckCircle,
                        )
                    }
                }
            }
        },
        onFabExpandedChange = { isFabExpanded = it },
        onImageDismiss = { expandedImageUrl = null },
    )
}

/**
 * 홈 화면 UI 컨텐츠 (상태를 받아서 렌더링만 담당)
 */
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    isFabExpanded: Boolean,
    expandedImageUrl: String?,
    onIntent: (HomeIntent) -> Unit,
    onFabExpandedChange: (Boolean) -> Unit,
    onImageDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val density = LocalDensity.current
    val topBarHeight = 56.dp
    val topBarHeightPx = with(density) { topBarHeight.toPx() }

    // TopBar 오프셋 상태 (0 = 보임, -topBarHeightPx = 숨김)
    var topBarOffsetHeightPx by remember { mutableStateOf(0f) }

    val nestedScrollConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    val delta = available.y
                    val newOffset = topBarOffsetHeightPx + delta
                    topBarOffsetHeightPx = newOffset.coerceIn(-topBarHeightPx, 0f)
                    return Offset.Zero
                }
            }
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .offset {
                    IntOffset(x = 0, y = topBarOffsetHeightPx.roundToInt())
                },
    ) {
        Scaffold(
            snackbarHost = { BuyOrNotSnackBarHost(snackbarHostState) },
            topBar = {
                HomeTopBar(
                    onNotificationClick = { onIntent(HomeIntent.OnNotificationClicked) },
                    onProfileClick = { onIntent(HomeIntent.OnProfileClicked) },
                )
            },
            floatingActionButton = {
                HomeFab(
                    expanded = isFabExpanded,
                    onExpandedChange = onFabExpandedChange,
                    onIntent = onIntent,
                )
            },
            containerColor = BuyOrNotTheme.colors.gray0,
        ) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                HomeTabSection(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { onIntent(HomeIntent.OnTabSelected(it)) },
                )

                HomeFeedList(
                    uiState = uiState,
                    onIntent = onIntent,
                )
            }

            // FAB 확장 시 배경 딤 처리
            FabDimOverlay(
                visible = isFabExpanded,
                onDismiss = { onFabExpandedChange(false) },
            )
        }

        // 이미지 확대 레이어 (최상단)
        expandedImageUrl?.let { url ->
            FullScreenImageOverlay(
                imageUrl = url,
                onDismiss = onImageDismiss,
            )
        }
    }
}

/**
 * 홈 화면의 탭 섹션 컴포넌트
 */
@Composable
private fun HomeTabSection(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        BuyOrNotTabRow(
            selectedTabIndex = HomeTab.entries.indexOf(selectedTab),
            modifier = Modifier.padding(start = 20.dp),
        ) {
            BuyOrNotTab(
                title = "투표 피드",
                selected = selectedTab == HomeTab.FEED,
                onClick = { onTabSelected(HomeTab.FEED) },
            )
            BuyOrNotTab(
                title = "내 투표",
                selected = selectedTab == HomeTab.REVIEW,
                onClick = { onTabSelected(HomeTab.REVIEW) },
            )
        }

        BuyOrNotDivider(
            size = BuyOrNotDividerSize.Small,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

/**
 * FAB (Floating Action Button) 컴포넌트
 */
@Composable
private fun HomeFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onIntent: (HomeIntent) -> Unit,
) {
    val fabOptions =
        listOf(
            FabOption(
                icon = BuyOrNotIcons.Vote.asImageVector(),
                label = "투표 등록",
                onClick = { onIntent(HomeIntent.OnCreateVoteClicked) },
            ),
            FabOption(
                icon = BuyOrNotIcons.Bag.asImageVector(),
                label = "상품 리뷰",
                onClick = { onIntent(HomeIntent.OnCreateReviewClicked) },
            ),
        )

    ExpandableFloatingActionButton(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        options = fabOptions,
    )
}

/**
 * FAB 확장 시 뒷배경 딤 처리 오버레이
 */
@Composable
private fun FabDimOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onDismiss() },
        )
    }
}

/**
 * 홈 화면의 메인 피드 리스트 컴포넌트
 */
@Composable
private fun HomeFeedList(
    uiState: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 탭에 따라 피드 필터링
    val filteredFeeds =
        when (uiState.selectedTab) {
            HomeTab.FEED -> uiState.feeds // 전체 피드
            HomeTab.REVIEW -> uiState.feeds.filter { it.isOwner } // 내 투표만
        }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 필터 칩
        item {
            FilterChipRow(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { onIntent(HomeIntent.OnFilterSelected(it)) },
            )
        }

        // 배너 (투표 피드 탭에만 표시)
        if (uiState.isBannerVisible && uiState.selectedTab == HomeTab.FEED) {
            item {
                HomeBannerSection(
                    onDismiss = { onIntent(HomeIntent.OnBannerDismissed) },
                    onClick = { /* TODO: 배너 클릭 처리 */ },
                )
            }
        }

        // 피드 아이템들 (실제 데이터 기반)
        items(filteredFeeds.size) { index ->
            val feed = filteredFeeds[index]
            FeedItemCard(
                feed = feed,
                onExpandClick = { url -> onIntent(HomeIntent.OnImageExpandClicked(url)) },
                onVote = { feedId, optionIndex ->
                    onIntent(HomeIntent.OnVoteClicked(feedId, optionIndex))
                },
                onDelete = { feedId ->
                    onIntent(HomeIntent.OnDeleteClicked(feedId))
                },
                onReport = { feedId ->
                    onIntent(HomeIntent.OnReportClicked(feedId))
                },
            )
        }
    }
}

/**
 * 필터 칩 행 컴포넌트
 */
@Composable
private fun FilterChipRow(
    selectedFilter: FilterChip,
    onFilterSelected: (FilterChip) -> Unit,
) {
    LazyRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(FilterChip.entries.size) { index ->
            val chip = FilterChip.entries[index]
            BuyOrNotChip(
                text = chip.label,
                isSelected = selectedFilter == chip,
                onClick = { onFilterSelected(chip) },
            )
        }
    }
}

/**
 * 홈 배너 섹션 (배너 + 구분선 + 간격)
 */
@Composable
private fun HomeBannerSection(
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    Column {
        Spacer(modifier = Modifier.height(20.dp))

        HomeBanner(
            modifier = Modifier.padding(horizontal = 20.dp),
            onDismiss = onDismiss,
            onClick = onClick,
        )

        Spacer(modifier = Modifier.height(20.dp))

        BuyOrNotDivider(
            size = BuyOrNotDividerSize.Small,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * 개별 피드 카드 아이템
 */
@Composable
private fun FeedItemCard(
    feed: FeedItem,
    onExpandClick: (String) -> Unit,
    onVote: (String, Int) -> Unit,
    onDelete: (String) -> Unit,
    onReport: (String) -> Unit,
) {
    var userVotedOption by remember(feed.id) { mutableStateOf(feed.userVotedOptionIndex) }

    Column {
        Spacer(modifier = Modifier.height(20.dp))

        FeedCard(
            profileImageUrl = feed.profileImageUrl,
            nickname = feed.nickname,
            category = feed.category,
            createdAt = feed.createdAt,
            content = feed.content,
            productImageUrl = feed.productImageUrl,
            price = feed.price,
            imageAspectRatio = feed.imageAspectRatio,
            isVoteEnded = feed.isVoteEnded,
            userVotedOptionIndex = userVotedOption,
            buyVoteCount = feed.buyVoteCount,
            maybeVoteCount = feed.maybeVoteCount,
            totalVoteCount = feed.totalVoteCount,
            isOwner = feed.isOwner,
            onExpandClick = onExpandClick,
            onVote = { option ->
                userVotedOption = option
                onVote(feed.id, option)
            },
            onDeleteClick = { onDelete(feed.id) },
            onReportClick = { onReport(feed.id) },
        )

        Spacer(modifier = Modifier.height(20.dp))

        BuyOrNotDivider(
            size = BuyOrNotDividerSize.Small,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

/**
 * HomeUiState를 위한 Saver (화면 회전 시 상태 보존)
 */
private fun homeUiStateSaver() =
    androidx.compose.runtime.saveable.Saver<HomeUiState, Map<String, Any>>(
        save = { state ->
            mapOf(
                "selectedTab" to state.selectedTab.name,
                "selectedFilter" to state.selectedFilter.name,
                "isBannerVisible" to state.isBannerVisible,
            )
        },
        restore = { map ->
            HomeUiState(
                selectedTab = HomeTab.valueOf(map["selectedTab"] as String),
                selectedFilter = FilterChip.valueOf(map["selectedFilter"] as String),
                isBannerVisible = map["isBannerVisible"] as Boolean,
            )
        },
    )

/**
 * 피드 이미지 확대 오버레이 컴포넌트
 */
@Composable
private fun FullScreenImageOverlay(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Expanded Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )

        // 상단 닫기 버튼
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 10.dp, top = 10.dp)
                    .size(40.dp)
                    .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BuyOrNotIcons.Close.asImageVector(),
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(name = "HomeScreen Preview", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    BuyOrNotTheme {
        HomeScreen()
    }
}
