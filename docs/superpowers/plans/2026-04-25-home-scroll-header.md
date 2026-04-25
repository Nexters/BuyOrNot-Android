# HomeScreen 스크롤 연동 헤더 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 홈 화면에서 아래로 스크롤 시 TopBar와 FilterChipRow가 자연스럽게 사라지고, HomeTabSection은 항상 상단에 고정되도록 LazyColumn 구조를 재편한다.

**Architecture:** `HomeFeedList` 내부의 외부 `Column` 래퍼와 고정 헤더 `Column`을 제거하고, `LazyColumn` 하나로 통합한다. `HomeTopBarSection`과 `FilterChipRow`는 일반 `item`으로, `HomeTabSection`은 `stickyHeader`로 배치한다.

**Tech Stack:** Jetpack Compose, `LazyColumn`, `stickyHeader`

---

## 변경 파일

- Modify: `feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeScreen.kt`
  - `HomeFeedList` 컴포저블 내부 구조 재편 (유일한 변경 파일)

---

### Task 1: `HomeFeedList` 구조 재편

**Files:**
- Modify: `feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeScreen.kt:374-537`

- [ ] **Step 1: `HomeFeedList` 내부 전체를 아래 코드로 교체**

`PullToRefreshBox` 블록 전체(현재 lines 374-536)를 다음으로 교체한다.

```kotlin
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
        // TopBar: 스크롤 시 자연스럽게 사라짐
        item {
            HomeTopBarSection(
                userType = uiState.userType,
                onLoginClick = onLoginClick,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick,
            )
        }

        // Tab: 항상 상단 고정
        stickyHeader {
            HomeTabSection(
                userType = uiState.userType,
                selectedTab = uiState.selectedTab,
                onTabSelected = { onIntent(HomeIntent.OnTabSelected(it)) },
                modifier = Modifier.background(BuyOrNotTheme.colors.gray0),
            )
        }

        // FilterChipRow: 스크롤 시 자연스럽게 사라짐 (내 투표 빈 상태일 때는 미노출)
        if (!isMyFeedEmpty) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                FilterChipRow(
                    selectedCategories = uiState.selectedCategories,
                    onAllCategorySelected = { onIntent(HomeIntent.OnAllCategorySelected) },
                    onCategoryToggled = { onIntent(HomeIntent.OnCategoryToggled(it)) },
                    selectedFilter = uiState.selectedFilter,
                    onShowSortSheet = { onIntent(HomeIntent.ShowSortSheet) },
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 배너 (투표 피드 탭이고 isBannerVisible이 true일 때만 표시)
        if (filteredFeeds.isNotEmpty() && uiState.isBannerVisible && uiState.selectedTab == HomeTab.FEED) {
            item {
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
                items(filteredFeeds.size, key = { index -> filteredFeeds[index].id }) { index ->
                    FeedItemCard(
                        feed = filteredFeeds[index],
                        voterProfileImageUrl = uiState.voterProfileImageUrl,
                        isGuest = uiState.userType == UserType.GUEST,
                        modifier = Modifier.animateItem(),
                        showProductLinkTooltip = showLinkTooltip && index == tooltipTargetIndex,
                        onVote = { id, opt -> onIntent(HomeIntent.OnVoteClicked(id, opt)) },
                        onDelete = { id -> onIntent(HomeIntent.ShowDeleteDialog(id)) },
                        onReport = { id -> onIntent(HomeIntent.OnReportClicked(id)) },
                        onBlock = { id -> onIntent(HomeIntent.ShowBlockDialog(id)) },
                        onLinkClick = onLinkClick,
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
                                color = BuyOrNotTheme.colors.gray950,
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }
            }

            // 2. 로딩 중인 단계
            uiState.isLoading -> {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BuyOrNotTheme.colors.gray950)
                    }
                }
            }

            // 3. 에러
            uiState.hasError -> {
                item {
                    BuyOrNotErrorView(
                        modifier = Modifier.padding(top = 80.dp),
                        message = "피드를 불러오지 못했어요",
                        onRefreshClick = { onIntent(HomeIntent.LoadFeeds) },
                    )
                }
            }

            else -> {
                item {
                    if (uiState.selectedTab == HomeTab.MY_FEED) {
                        HomeFeedEmptyView(
                            modifier = Modifier.padding(top = 140.dp),
                            title = "아직 올린 투표가 없어요",
                            description = "고민되는 상품의 투표를 올려보세요!",
                            onUploadClick = onUploadClick,
                        )
                    } else {
                        HomeFeedEmptyView(
                            modifier = Modifier.padding(top = 120.dp),
                            title = "첫번째 투표를 올려보세요!",
                            onUploadClick = onUploadClick,
                        )
                    }
                }
            }
        }
    }

    // 투표 상태 필터 시트 (PullToRefreshBox 최상단 → full-screen dim 적용)
    if (uiState.showSortSheet) {
        OptionSheet(
            title = "투표 상태",
            options = FilterChip.entries.map { it.label },
            selectedOption = uiState.selectedFilter.label,
            onOptionClick = { option ->
                val filter = FilterChip.entries.first { it.label == option }
                onIntent(HomeIntent.OnFilterSelected(filter))
            },
            onDismissRequest = { onIntent(HomeIntent.DismissSortSheet) },
        )
    }
}
```

- [ ] **Step 2: 불필요해진 import 정리 확인**

외부 `Column`이 제거되면서 `Column`이 파일 내 다른 곳에서도 사용되는지 확인한다.
`HomeTabSection`, `HomeFab` 등에서 여전히 `Column`을 사용하므로 import는 유지한다.

- [ ] **Step 3: 빌드 확인**

```bash
./gradlew :feature:home:assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 동작 검증**

에뮬레이터 또는 기기에서 앱 실행 후 아래 항목을 확인한다.

| 시나리오 | 기대 동작 |
|---|---|
| 피드 목록에서 아래로 스크롤 | TopBar + FilterChipRow 자연스럽게 위로 사라짐 |
| 아래로 스크롤 중 Tab | 항상 화면 상단에 고정 유지 |
| 최상단으로 다시 스크롤 | TopBar + FilterChipRow 다시 나타남 |
| 비회원 상태 | GuestTopBar 동일하게 동작 |
| 내 투표 탭 빈 상태 | FilterChipRow 미노출, Tab 고정 유지 |
| Pull-to-Refresh | 정상 동작 |
| 투표 상태 필터 시트 | 화면 전체 dim과 함께 정상 표시 |

- [ ] **Step 5: 커밋**

```bash
git add feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeScreen.kt
git commit -m "feat/#90: 스크롤 연동 헤더 — TopBar/FilterRow 스크롤, Tab sticky 고정"
```
