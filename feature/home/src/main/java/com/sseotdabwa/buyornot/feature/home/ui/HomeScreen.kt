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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.sseotdabwa.buyornot.feature.home.viewmodel.FeedItem
import com.sseotdabwa.buyornot.feature.home.viewmodel.FilterChip
import com.sseotdabwa.buyornot.feature.home.viewmodel.HomeIntent
import com.sseotdabwa.buyornot.feature.home.viewmodel.HomeSideEffect
import com.sseotdabwa.buyornot.feature.home.viewmodel.HomeTab
import com.sseotdabwa.buyornot.feature.home.viewmodel.HomeUiState
import kotlin.math.roundToInt

/**
 * 홈 화면 루트 컴포저블
 * MVI 패턴을 적용하여 ViewModel을 통해 상태를 관리합니다.
 *
 * @param onLoginClick 비회원일 때 로그인 버튼 클릭 콜백
 * @param onNotificationClick 알림 아이콘 클릭 콜백
 * @param onProfileClick 프로필 아이콘 클릭 콜백
 * @param onUploadClick 업로드 화면으로 이동 콜백
 * @param viewModel HomeViewModel (Hilt 주입)
 */
@Composable
fun HomeScreen(
    onLoginClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onUploadClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 화면 전용 일시적 상태 (ViewModel에서 관리하지 않음)
    var isFabExpanded by remember { mutableStateOf(false) }

    var isTimeOut by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading, uiState.feeds.isEmpty()) {
        if (uiState.isLoading && uiState.feeds.isEmpty()) {
            // 로딩이 시작되면 타임아웃을 초기화하고 대기합니다.
            isTimeOut = false
            kotlinx.coroutines.delay(5000L) // 5초 가이드
            isTimeOut = true
        } else {
            // 로딩이 끝나거나 데이터가 들어오면 타임아웃을 리셋합니다.
            isTimeOut = false
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

    HomeScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        isFabExpanded = isFabExpanded,
        isTimeOut = isTimeOut,
        onLoginClick = onLoginClick,
        onNotificationClick = onNotificationClick,
        onProfileClick = onProfileClick,
        onUploadClick = onUploadClick,
        onIntent = viewModel::handleIntent,
        onFabExpandedChange = { isFabExpanded = it },
    )
}

/**
 * 홈 화면 UI 컨텐츠 (상태를 받아서 렌더링만 담당)
 */
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    isFabExpanded: Boolean,
    isTimeOut: Boolean,
    onLoginClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onUploadClick: () -> Unit,
    onIntent: (HomeIntent) -> Unit,
    onFabExpandedChange: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val density = LocalDensity.current
    var topBarHeightPx by remember { mutableStateOf(0f) }
    var tabHeightPx by remember { mutableStateOf(0f) }

    val totalHeaderHeight = with(density) { (topBarHeightPx + tabHeightPx).toDp() }

    // TopBar 오프셋 상태 (0 = 보임, -topBarHeightPx = 숨김)
    var topBarOffsetHeightPx by remember { mutableStateOf(0f) }

    val nestedScrollConnection =
        remember(topBarHeightPx) {
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

    LaunchedEffect(topBarHeightPx) {
        topBarOffsetHeightPx = topBarOffsetHeightPx.coerceIn(-topBarHeightPx, 0f)
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
    ) {
        Scaffold(
            snackbarHost = { BuyOrNotSnackBarHost(snackbarHostState) },
            floatingActionButton = {
                HomeFab(
                    expanded = isFabExpanded,
                    onExpandedChange = onFabExpandedChange,
                    onUploadClick = onUploadClick,
                )
            },
            containerColor = BuyOrNotTheme.colors.gray0,
        ) { innerPadding ->
            HomeFeedList(
                uiState = uiState,
                onIntent = onIntent,
                isTimeOut = isTimeOut,
                headerPadding = totalHeaderHeight + innerPadding.calculateTopPadding(),
            )

            FabDimOverlay(
                visible = isFabExpanded,
                onDismiss = { onFabExpandedChange(false) },
            )
        }

        HomeHeader(
            uiState = uiState,
            onLoginClick = onLoginClick,
            onNotificationClick = onNotificationClick,
            onProfileClick = onProfileClick,
            onTabSelected = { onIntent(HomeIntent.OnTabSelected(it)) },
            currentTopBarHeightPx = topBarHeightPx,
            currentTabHeightPx = tabHeightPx,
            onHeightsMeasured = { topBar, tab ->
                topBarHeightPx = topBar
                tabHeightPx = tab
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(x = 0, y = topBarOffsetHeightPx.roundToInt()) }
                    .background(BuyOrNotTheme.colors.gray0),
        )
    }
}

private enum class HomeHeaderSlot {
    TopBar,
    Tab,
}

@Composable
private fun HomeHeader(
    uiState: HomeUiState,
    onLoginClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onTabSelected: (HomeTab) -> Unit,
    currentTopBarHeightPx: Float,
    currentTabHeightPx: Float,
    onHeightsMeasured: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val width = constraints.maxWidth
        val childConstraints =
            if (width == Constraints.Infinity) {
                constraints
            } else {
                Constraints.fixedWidth(width)
            }

        val topBarPlaceable =
            subcompose(HomeHeaderSlot.TopBar) {
                when (uiState.userType) {
                    UserType.GUEST -> {
                        GuestTopBar(
                            onLoginClick = onLoginClick,
                        )
                    }
                    UserType.SOCIAL -> {
                        HomeTopBar(
                            onNotificationClick = onNotificationClick,
                            onProfileClick = onProfileClick,
                        )
                    }
                }
            }.first().measure(childConstraints)

        val tabPlaceable =
            subcompose(HomeHeaderSlot.Tab) {
                HomeTabSection(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = onTabSelected,
                )
            }.first().measure(childConstraints)

        val newTopBarHeight = topBarPlaceable.height.toFloat()
        val newTabHeight = tabPlaceable.height.toFloat()

        if (newTopBarHeight != currentTopBarHeightPx || newTabHeight != currentTabHeightPx) {
            onHeightsMeasured(newTopBarHeight, newTabHeight)
        }

        val layoutWidth =
            if (width == Constraints.Infinity) {
                topBarPlaceable.width.coerceAtLeast(tabPlaceable.width)
            } else {
                width
            }

        layout(width = layoutWidth, height = topBarPlaceable.height + tabPlaceable.height) {
            topBarPlaceable.place(0, 0)
            tabPlaceable.place(0, topBarPlaceable.height)
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
@Composable
private fun HomeFeedList(
    uiState: HomeUiState,
    isTimeOut: Boolean,
    onIntent: (HomeIntent) -> Unit,
    headerPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val filteredFeeds = uiState.feeds

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = headerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 공통 필터 칩 영역
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FilterChipRow(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { onIntent(HomeIntent.OnFilterSelected(it)) },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        when {
            // 1. 데이터가 있으면 로딩 여부와 상관없이 최우선 노출
            filteredFeeds.isNotEmpty() -> {
                // 피드 리스트 및 배너 노출 로직 (기존과 동일)
                items(filteredFeeds.size, key = { index -> filteredFeeds[index].id }) { index ->
                    FeedItemCard(
                        feed = filteredFeeds[index],
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp).animateItem(),
                        onVote = { id, opt -> onIntent(HomeIntent.OnVoteClicked(id, opt)) },
                        onDelete = { id -> onIntent(HomeIntent.OnDeleteClicked(id)) },
                        onReport = { id -> onIntent(HomeIntent.OnReportClicked(id)) },
                    )
                }
            }

            // 2. 로딩 중인 단계 (로딩이 끝나기 전까지는 Result를 판단하지 않음)
            uiState.isLoading -> {
                if (isTimeOut) {
                    // [요청사항] 특정 초(5초) 이상 로딩 중이면 에러 뷰 노출
                    item {
                        BuyOrNotErrorView(
                            modifier = Modifier.padding(top = 80.dp),
                            message = "연결 시간이 초과되었습니다",
                            onRefreshClick = { onIntent(HomeIntent.LoadFeeds) },
                        )
                    }
                } else {
                    // 아직 5초 미만이면 로딩 인디케이터만 노출
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BuyOrNotTheme.colors.gray900)
                        }
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
                    HomeFeedEmptyView(modifier = Modifier.padding(top = 80.dp))
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
    modifier: Modifier = Modifier,
    onVote: (String, Int) -> Unit,
    onDelete: (String) -> Unit,
    onReport: (String) -> Unit,
) {
    var userVotedOption by remember(feed.id, feed.userVotedOptionIndex) { mutableStateOf(feed.userVotedOptionIndex) }

    Column {
        FeedCard(
            modifier = modifier,
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
            onVote = { option ->
                userVotedOption = option
                onVote(feed.id, option)
            },
            onDeleteClick = { onDelete(feed.id) },
            onReportClick = { onReport(feed.id) },
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
        HomeScreen()
    }
}
