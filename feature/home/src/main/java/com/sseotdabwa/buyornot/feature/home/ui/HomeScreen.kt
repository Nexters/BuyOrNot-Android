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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotAlertDialog
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotButtonDefaults
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotChip
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotDivider
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotDividerSize
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotEmptyView
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotErrorView
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotSnackBarHost
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotTab
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotTabRow
import com.sseotdabwa.buyornot.core.designsystem.components.ExpandableFloatingActionButton
import com.sseotdabwa.buyornot.core.designsystem.components.FabOption
import com.sseotdabwa.buyornot.core.designsystem.components.FeedCard
import com.sseotdabwa.buyornot.core.designsystem.components.GuestTopBar
import com.sseotdabwa.buyornot.core.designsystem.components.HomeTopBar
import com.sseotdabwa.buyornot.core.designsystem.components.showBuyOrNotSnackBar
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.domain.model.UserType
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * 홈 화면 루트 컴포저블
 * MVI 패턴을 적용하여 ViewModel을 통해 상태를 관리합니다.
 *
 * @param onLoginClick 비회원일 때 로그인 버튼 클릭 콜백
 * @param onNotificationClick 알림 아이콘 클릭 콜백
 * @param onProfileClick 프로필 아이콘 클릭 콜백
 * @param onUploadClick 업로드 화면으로 이동 콜백
 * @param initialTab 초기 선택 탭 (기본값: FEED)
 * @param viewModel HomeViewModel (Hilt 주입)
 */
@Composable
fun HomeRoute(
    onLoginClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    initialTab: HomeTab = HomeTab.FEED,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 초기 탭 설정
    LaunchedEffect(initialTab) {
        if (uiState.selectedTab != initialTab) {
            viewModel.handleIntent(HomeIntent.OnTabSelected(initialTab))
        }
    }

    // SideEffect 처리
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is HomeSideEffect.ShowSnackbar -> {
                    showBuyOrNotSnackBar(
                        snackbarHostState = snackbarHostState,
                        message = sideEffect.message,
                        iconResource = sideEffect.icon,
                    )
                }
                is HomeSideEffect.NavigateToNotification -> onNotificationClick()
                is HomeSideEffect.NavigateToProfile -> onProfileClick()
                is HomeSideEffect.NavigateToUpload -> onUploadClick()
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onLoginClick = onLoginClick,
        onNotificationClick = onNotificationClick,
        onProfileClick = onProfileClick,
        onUploadClick = onUploadClick,
        onIntent = viewModel::handleIntent,
    )
}

/**
 * 홈 화면 UI 컨텐츠 (상태를 받아서 렌더링만 담당)
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    onLoginClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    // 화면 전용 일시적 상태 (ViewModel에서 관리하지 않음)
    var isFabExpanded by remember { mutableStateOf(false) }

    if (uiState.showBlockDialog && uiState.blockingNickname != null) {
        BuyOrNotAlertDialog(
            onDismissRequest = { onIntent(HomeIntent.DismissBlockDialog) },
            title = "이 글의 사용자를 차단하시겠어요?",
            subText = "${uiState.blockingNickname}님의 투표를 볼 수 없어요.",
            confirmText = "차단하기",
            dismissText = "취소",
            onConfirm = { onIntent(HomeIntent.OnBlockConfirmed) },
            onDismiss = { onIntent(HomeIntent.DismissBlockDialog) },
        )
    }

    if (uiState.showDeleteDialog && uiState.deletingFeedId != null) {
        BuyOrNotAlertDialog(
            onDismissRequest = { onIntent(HomeIntent.DismissDeleteDialog) },
            title = "정말 삭제하시겠어요?",
            subText = "투표 데이터가 모두 사라지며, 복구할 수 없어요.",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = { onIntent(HomeIntent.OnDeleteConfirmed(uiState.deletingFeedId)) },
            onDismiss = { onIntent(HomeIntent.DismissDeleteDialog) },
            confirmButtonColors = BuyOrNotButtonDefaults.destructiveButtonColors(),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { BuyOrNotSnackBarHost(snackbarHostState) },
            floatingActionButton = {
                HomeFab(
                    expanded = isFabExpanded,
                    onExpandedChange = { isFabExpanded = it },
                    onUploadClick = onUploadClick,
                )
            },
            containerColor = BuyOrNotTheme.colors.gray0,
        ) { innerPadding ->
            HomeFeedList(
                uiState = uiState,
                onIntent = onIntent,
                contentPadding = innerPadding,
                onLoginClick = onLoginClick,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
                onUploadClick = onUploadClick,
            )

            FabDimOverlay(
                visible = isFabExpanded,
                onDismiss = { isFabExpanded = false },
            )
        }
    }
}

@Composable
private fun HomeTopBarSection(
    userType: UserType,
    onLoginClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    when (userType) {
        UserType.GUEST -> GuestTopBar(onLoginClick = onLoginClick)
        UserType.SOCIAL -> {
            HomeTopBar(
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
            )
        }
    }
}

/**
 * 홈 화면의 탭 섹션 컴포넌트
 */
@Composable
private fun HomeTabSection(
    userType: UserType,
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = if (userType == UserType.GUEST) listOf(HomeTab.FEED) else HomeTab.entries.toList()

    Column(modifier = modifier) {
        BuyOrNotTabRow(
            selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
            modifier = Modifier.padding(start = 20.dp),
        ) {
            BuyOrNotTab(
                title = "투표 피드",
                selected = selectedTab == HomeTab.FEED,
                onClick = { onTabSelected(HomeTab.FEED) },
            )
            if (userType == UserType.SOCIAL) {
                BuyOrNotTab(
                    title = "내 투표",
                    selected = selectedTab == HomeTab.MY_FEED,
                    onClick = { onTabSelected(HomeTab.MY_FEED) },
                )
            }
        }

        BuyOrNotDivider(
            size = BuyOrNotDividerSize.Small,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

/**
 * FAB (Floating Action Button) 컴포넌트
 * 임시로 클릭 시 바로 업로드 화면으로 이동
 */
@Composable
private fun HomeFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onUploadClick: () -> Unit,
) {
    val fabOptions =
        listOf(
            FabOption(
                icon = BuyOrNotIcons.Vote.asImageVector(),
                label = "투표 등록",
                onClick = { onUploadClick() },
            ),
            FabOption(
                icon = BuyOrNotIcons.Bag.asImageVector(),
                label = "상품 리뷰",
                onClick = { onUploadClick() },
            ),
        )

    ExpandableFloatingActionButton(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        options = fabOptions,
        onMainButtonClick = onUploadClick, // 메인 버튼 클릭 시 바로 업로드 화면으로
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeFeedList(
    uiState: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    contentPadding: PaddingValues,
    onLoginClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ViewModel에서 이미 탭과 필터에 따라 필터링된 피드를 제공
    val filteredFeeds = uiState.feeds
    val listState = rememberLazyListState()

    // 무한 스크롤 구현: 리스트 끝에 도달하면 다음 페이지 로드
    LaunchedEffect(listState, uiState.hasNextPage, uiState.isNextPageLoading) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.filter { lastVisibleIndex ->
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            // 전체 아이템 수에서 3개 전쯤에 도달하면 미리 로드 (0-based index)
            lastVisibleIndex != null && lastVisibleIndex >= totalItemsCount - 3
        }.distinctUntilChanged()
            .collect {
                if (uiState.hasNextPage && !uiState.isNextPageLoading) {
                    onIntent(HomeIntent.LoadNextPage)
                }
            }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { onIntent(HomeIntent.Refresh) },
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                HomeTopBarSection(
                    userType = uiState.userType,
                    onLoginClick = onLoginClick,
                    onNotificationClick = onNotificationClick,
                    onProfileClick = onProfileClick,
                )
            }

            stickyHeader {
                HomeTabSection(
                    userType = uiState.userType,
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { onIntent(HomeIntent.OnTabSelected(it)) },
                    modifier = Modifier.background(BuyOrNotTheme.colors.gray0),
                )
            }

            // 공통 필터 칩 영역
            item {
                Spacer(modifier = Modifier.height(16.dp))
                FilterChipRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { onIntent(HomeIntent.OnFilterSelected(it)) },
                )
                // 배너 (투표 피드 탭이고 isBannerVisible이 true일 때만 표시)
                if (filteredFeeds.isNotEmpty() && uiState.isBannerVisible && uiState.selectedTab == HomeTab.FEED) {
                    Spacer(modifier = Modifier.height(16.dp))

                    HomeBanner(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        onDismiss = { onIntent(HomeIntent.OnBannerDismissed) },
                        onClick = onUploadClick,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BuyOrNotDivider(
                        size = BuyOrNotDividerSize.Small,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }

            when {
                // 1. 데이터가 있으면 로딩 여부와 상관없이 최우선 노출
                filteredFeeds.isNotEmpty() -> {
                    // 피드 리스트 및 배너 노출 로직 (기존과 동일)
                    items(filteredFeeds.size, key = { index -> filteredFeeds[index].id }) { index ->
                        FeedItemCard(
                            feed = filteredFeeds[index],
                            voterProfileImageUrl = uiState.voterProfileImageUrl,
                            isGuest = uiState.userType == UserType.GUEST,
                            modifier = Modifier.animateItem(),
                            onVote = { id, opt -> onIntent(HomeIntent.OnVoteClicked(id, opt)) },
                            onDelete = { id -> onIntent(HomeIntent.ShowDeleteDialog(id)) },
                            onReport = { id -> onIntent(HomeIntent.OnReportClicked(id)) },
                            onBlock = { id -> onIntent(HomeIntent.ShowBlockDialog(id)) },
                        )
                    }

                    // 다음 페이지 로딩 중일 때 표시
                    if (uiState.isNextPageLoading) {
                        item {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    color = BuyOrNotTheme.colors.gray900,
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    }
                }

                // 2. 로딩 중인 단계 (로딩이 끝나기 전까지는 Result를 판단하지 않음)
                uiState.isLoading -> {
                    item {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BuyOrNotTheme.colors.gray900)
                        }
                    }
                }

                // 3. 로딩이 끝난 단계 (isLoading == false)
                uiState.hasError -> {
                    // 통신 실패로 로딩이 끝난 경우
                    item {
                        BuyOrNotErrorView(
                            modifier = Modifier.padding(top = 80.dp),
                            message = "피드를 불러오지 못했어요",
                            onRefreshClick = { onIntent(HomeIntent.LoadFeeds) },
                        )
                    }
                }

                else -> {
                    // [요청사항] 통신은 성공(hasError false)했지만 데이터가 없는 경우
                    item {
                        HomeFeedEmptyView(
                            modifier = Modifier.padding(top = 80.dp),
                        )
                    }
                }
            }
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
        modifier = Modifier.fillMaxWidth(),
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
 * 개별 피드 카드 아이템
 */
@Composable
private fun FeedItemCard(
    feed: FeedItem,
    voterProfileImageUrl: String,
    isGuest: Boolean,
    modifier: Modifier = Modifier,
    onVote: (String, Int) -> Unit,
    onDelete: (String) -> Unit,
    onReport: (String) -> Unit,
    onBlock: (String) -> Unit,
) {
    Column {
        FeedCard(
            modifier = modifier.padding(vertical = 26.dp),
            profileImageUrl = feed.profileImageUrl,
            nickname = feed.nickname,
            category = feed.category,
            createdAt = feed.createdAt,
            title = feed.title,
            content = feed.content,
            productImageUrls = feed.productImageUrls,
            price = feed.price,
            imageAspectRatio = feed.imageAspectRatio,
            isVoteEnded = feed.isVoteEnded,
            userVotedOptionIndex = feed.userVotedOptionIndex,
            buyVoteCount = feed.buyVoteCount,
            maybeVoteCount = feed.maybeVoteCount,
            totalVoteCount = feed.totalVoteCount,
            isOwner = feed.isOwner,
            voterProfileImageUrl = voterProfileImageUrl,
            onVote = { option ->
                onVote(feed.id, option)
            },
            onDeleteClick = { onDelete(feed.id) },
            onReportClick = { onReport(feed.id) },
            onBlockClick = { onBlock(feed.id) },
            showMoreButton = !isGuest,
        )

        BuyOrNotDivider(
            size = BuyOrNotDividerSize.Small,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Composable
fun HomeFeedEmptyView(modifier: Modifier = Modifier) {
    BuyOrNotEmptyView(
        modifier = modifier,
        title = "아직 올린 투표가 없어요",
        description = "고민되는 상품의 투표를 올려보세요!",
        image = BuyOrNotIcons.NoVote.resId,
    )
}

@Preview(name = "HomeScreen Preview", showBackground = false)
@Composable
private fun HomeScreenPreview() {
    BuyOrNotTheme {
        HomeScreen(
            uiState = HomeUiState(),
            onIntent = {},
        )
    }
}
