# FilterChipRow 카테고리 필터 + 투표 상태 정렬 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 홈 화면 FilterChipRow를 카테고리 다중 선택 칩 + 투표 상태 OptionSheet 구조로 개편한다. 카테고리 필터링은 클라이언트 로컬에서 처리한다.

**Architecture:** ViewModel이 `allFeeds`(API 원본)와 `feeds`(카테고리 필터 적용 결과)를 분리 관리한다. 투표 상태(OPEN/CLOSED)는 기존대로 API 쿼리 파라미터로 처리하고, 카테고리 필터는 API 응답에 로컬로 적용한다. 두 필터는 AND 조합이다.

**Tech Stack:** Kotlin, Jetpack Compose, MVI (BaseViewModel), Hilt, `FeedCategory` domain model, `BuyOrNotChip` / `OptionSheet` / `BuyOrNotIcons` design system

---

## 파일 변경 범위

| 파일 | 변경 종류 | 내용 |
|------|-----------|------|
| `core/designsystem/src/main/java/com/sseotdabwa/buyornot/core/designsystem/icon/BuyOrNotIcons.kt` | Modify | `Sort` 아이콘 추가 |
| `feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeContract.kt` | Modify | `allFeeds`, `selectedCategories` State 추가; `OnCategoryToggled` Intent 추가 |
| `feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeViewModel.kt` | Modify | `applyCategories()`, `handleCategoryToggled()` 추가; `allFeeds` 관리 로직 추가 |
| `feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeScreen.kt` | Modify | `FilterChipRow` 정렬 아이콘 + 카테고리 칩 + OptionSheet 구현 |

---

## Task 1: BuyOrNotIcons에 Sort 아이콘 추가

**Files:**
- Modify: `core/designsystem/src/main/java/com/sseotdabwa/buyornot/core/designsystem/icon/BuyOrNotIcons.kt`

- [ ] **Step 1: `BuyOrNotIcons`에 `Sort` 항목 추가**

  `val Won = IconResource(R.drawable.ic_won)` 아래에 추가:

  ```kotlin
  val Sort = IconResource(R.drawable.ic_sort)
  ```

  변경 후 `BuyOrNotIcons` object 마지막 부분:
  ```kotlin
  val Won = IconResource(R.drawable.ic_won)
  val Sort = IconResource(R.drawable.ic_sort)
  ```

- [ ] **Step 2: 빌드 확인**

  ```bash
  ./gradlew :core:designsystem:assembleDebug
  ```
  Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: 커밋**

  ```bash
  git add core/designsystem/src/main/java/com/sseotdabwa/buyornot/core/designsystem/icon/BuyOrNotIcons.kt
  git commit -m "feat/#86: BuyOrNotIcons에 Sort 아이콘 추가"
  ```

---

## Task 2: HomeContract.kt — State & Intent 확장

**Files:**
- Modify: `feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeContract.kt`

- [ ] **Step 1: import 추가**

  파일 상단 import 블록에 추가:
  ```kotlin
  import com.sseotdabwa.buyornot.domain.model.FeedCategory
  ```

- [ ] **Step 2: `HomeUiState`에 `allFeeds`, `selectedCategories` 추가**

  `HomeUiState`의 `feeds` 필드 바로 위에 추가:
  ```kotlin
  val allFeeds: List<FeedItem> = emptyList(),
  val selectedCategories: Set<FeedCategory> = emptySet(),
  ```

  변경 후 `HomeUiState`:
  ```kotlin
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
  )
  ```

- [ ] **Step 3: `HomeIntent`에 `OnCategoryToggled` 추가**

  `HomeIntent` sealed interface의 `OnBlockConfirmed` 아래에 추가:
  ```kotlin
  data class OnCategoryToggled(
      val category: FeedCategory,
  ) : HomeIntent
  ```

- [ ] **Step 4: 빌드 확인**

  ```bash
  ./gradlew :feature:home:assembleDebug
  ```
  Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

  ```bash
  git add feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeContract.kt
  git commit -m "feat/#86: HomeContract에 카테고리 필터 State/Intent 추가"
  ```

---

## Task 3: HomeViewModel.kt — 카테고리 필터 로직

**Files:**
- Modify: `feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeViewModel.kt`

- [ ] **Step 1: import 추가**

  파일 상단 import 블록에 추가:
  ```kotlin
  import com.sseotdabwa.buyornot.domain.model.FeedCategory
  ```

- [ ] **Step 2: `handleIntent`에 `OnCategoryToggled` 분기 추가**

  `is HomeIntent.OnBlockConfirmed -> handleBlockConfirmed()` 아래에 추가:
  ```kotlin
  is HomeIntent.OnCategoryToggled -> handleCategoryToggled(intent.category)
  ```

- [ ] **Step 3: `applyCategories()` private 함수 추가**

  `FilterChip.toFeedStatus()` 함수 바로 위에 추가:
  ```kotlin
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
  ```

- [ ] **Step 4: `handleCategoryToggled()` private 함수 추가**

  `applyCategories()` 바로 아래에 추가:
  ```kotlin
  private fun handleCategoryToggled(category: FeedCategory) {
      val newCategories =
          if (category in currentState.selectedCategories) {
              currentState.selectedCategories - category
          } else {
              currentState.selectedCategories + category
          }
      updateState {
          it.copy(
              selectedCategories = newCategories,
              feeds = applyCategories(it.allFeeds, newCategories),
          )
      }
  }
  ```

- [ ] **Step 5: `handleTabSelection()` — `selectedCategories`, `allFeeds` 초기화 추가**

  기존 `handleTabSelection` 함수를 아래로 교체:
  ```kotlin
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
  ```

- [ ] **Step 6: `loadFeeds()` — `allFeeds` 관리 추가**

  `loadFeeds` 함수의 `if (clearFeeds)` 블록을 수정:
  ```kotlin
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
  ```

  그리고 `.onSuccess { feedList ->` 블록 전체를 교체:
  ```kotlin
  .onSuccess { feedList ->
      val newFeeds =
          feedList.feeds.map { feed ->
              val isOwner = currentUserId != null && feed.author.userId == currentUserId
              feed.toFeedItem(isOwner)
          }
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
  }
  ```

- [ ] **Step 7: `handleNextPage()` — `allFeeds` append 처리**

  `handleNextPage` 내 `.onSuccess { feedList ->` 블록의 `val newItems = ...` 이후를 교체:
  ```kotlin
  .onSuccess { feedList ->
      if (currentState.selectedTab != requestedTab) {
          updateState { it.copy(isNextPageLoading = false) }
          return@launch
      }

      val newItems =
          feedList.feeds.map { feed ->
              val isOwner = currentUserId != null && feed.author.userId == currentUserId
              feed.toFeedItem(isOwner)
          }

      val newAllFeeds = currentState.allFeeds + newItems

      updateState {
          it.copy(
              allFeeds = newAllFeeds,
              feeds = applyCategories(newAllFeeds, it.selectedCategories),
              isNextPageLoading = false,
              hasNextPage = feedList.hasNext,
              nextCursor = feedList.nextCursor,
          )
      }
  }
  ```

- [ ] **Step 8: `handleRefresh()` — `allFeeds` 갱신 처리**

  `handleRefresh` 내 `.onSuccess { feedList ->` 블록을 교체:
  ```kotlin
  .onSuccess { feedList ->
      val refreshedFeeds =
          feedList.feeds.map { feed ->
              val isOwner = currentUserId != null && feed.author.userId == currentUserId
              feed.toFeedItem(isOwner)
          }
      updateState {
          it.copy(
              allFeeds = refreshedFeeds,
              feeds = applyCategories(refreshedFeeds, it.selectedCategories),
              isRefreshing = false,
              hasNextPage = feedList.hasNext,
              nextCursor = feedList.nextCursor,
          )
      }
  }
  ```

- [ ] **Step 9: 빌드 확인**

  ```bash
  ./gradlew :feature:home:assembleDebug
  ```
  Expected: `BUILD SUCCESSFUL`

- [ ] **Step 10: 커밋**

  ```bash
  git add feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeViewModel.kt
  git commit -m "feat/#86: HomeViewModel 카테고리 로컬 필터링 로직 추가"
  ```

---

## Task 4: HomeScreen.kt — FilterChipRow UI 재구성

**Files:**
- Modify: `feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeScreen.kt`

- [ ] **Step 1: import 추가 (파일 상단)**

  기존 import 블록에 아래 항목들을 추가:
  ```kotlin
  import androidx.compose.material3.Icon
  import androidx.compose.material3.IconButton
  import androidx.compose.ui.Alignment
  import com.sseotdabwa.buyornot.core.designsystem.components.OptionSheet
  import com.sseotdabwa.buyornot.domain.model.FeedCategory
  ```

  > 참고: `Alignment`는 이미 import되어 있을 수 있으니 중복 여부 확인 후 추가.

- [ ] **Step 2: `HomeFeedList`에서 `FilterChipRow` 호출 시그니처 변경**

  `HomeFeedList` 내 `FilterChipRow` 호출 부분을 교체:
  ```kotlin
  // 기존
  FilterChipRow(
      selectedFilter = uiState.selectedFilter,
      onFilterSelected = { onIntent(HomeIntent.OnFilterSelected(it)) },
  )

  // 변경 후
  FilterChipRow(
      selectedCategories = uiState.selectedCategories,
      onCategoryToggled = { onIntent(HomeIntent.OnCategoryToggled(it)) },
      selectedFilter = uiState.selectedFilter,
      onFilterSelected = { onIntent(HomeIntent.OnFilterSelected(it)) },
  )
  ```

- [ ] **Step 3: `FilterChipRow` 함수 전체 교체**

  기존 `FilterChipRow` composable 전체를 아래로 교체:

  ```kotlin
  /**
   * 필터 칩 행 컴포넌트
   * - 맨 좌측: 정렬 아이콘 (클릭 시 투표 상태 OptionSheet 표시)
   * - 이후: FeedCategory 카테고리 칩 (다중 선택, 없으면 전체)
   */
  @Composable
  private fun FilterChipRow(
      selectedCategories: Set<FeedCategory>,
      onCategoryToggled: (FeedCategory) -> Unit,
      selectedFilter: FilterChip,
      onFilterSelected: (FilterChip) -> Unit,
  ) {
      var showSortSheet by remember { mutableStateOf(false) }

      if (showSortSheet) {
          OptionSheet(
              title = "투표 상태",
              options = FilterChip.entries.map { it.label },
              selectedOption = selectedFilter.label,
              onOptionClick = { option ->
                  val filter = FilterChip.entries.first { it.label == option }
                  onFilterSelected(filter)
              },
              onDismissRequest = { showSortSheet = false },
          )
      }

      LazyRow(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(horizontal = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
      ) {
          item {
              IconButton(
                  onClick = { showSortSheet = true },
                  modifier = Modifier.size(32.dp),
              ) {
                  Icon(
                      imageVector = BuyOrNotIcons.Sort.asImageVector(),
                      contentDescription = "투표 상태 필터",
                      tint = if (selectedFilter != FilterChip.ALL) {
                          BuyOrNotTheme.colors.gray900
                      } else {
                          BuyOrNotTheme.colors.gray500
                      },
                  )
              }
          }

          items(FeedCategory.entries.size) { index ->
              val category = FeedCategory.entries[index]
              BuyOrNotChip(
                  text = category.displayName,
                  isSelected = category in selectedCategories,
                  onClick = { onCategoryToggled(category) },
              )
          }
      }
  }
  ```

- [ ] **Step 4: `LazyRow`에 `verticalAlignment` import 확인**

  `LazyRow`에 `verticalAlignment` 파라미터를 사용하려면 아래 import가 필요:
  ```kotlin
  import androidx.compose.foundation.lazy.LazyRow
  ```
  이미 있으므로 추가 불필요. `Arrangement.spacedBy` 및 `Alignment.CenterVertically`도 기존 import 확인.

- [ ] **Step 5: 빌드 확인**

  ```bash
  ./gradlew :feature:home:assembleDebug
  ```
  Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: 커밋**

  ```bash
  git add feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/HomeScreen.kt
  git commit -m "feat/#86: FilterChipRow 카테고리 칩 + 투표 상태 OptionSheet 구현"
  ```

---

## 검증 체크리스트 (수동)

- [ ] 카테고리 칩 하나 선택 → 해당 카테고리 피드만 노출
- [ ] 카테고리 칩 복수 선택 → 선택된 카테고리 모두 노출 (OR)
- [ ] 선택된 카테고리 칩 재클릭 → 선택 해제 (전체 복귀)
- [ ] 정렬 아이콘 클릭 → OptionSheet "투표 상태" 표시
- [ ] OptionSheet에서 "진행중 투표" 선택 → 진행중 피드만 (카테고리 필터 유지)
- [ ] OptionSheet에서 "전체" 선택 → 상태 필터 해제 (카테고리 필터 유지)
- [ ] 투표 상태 ≠ ALL일 때 정렬 아이콘 tint 진하게 표시
- [ ] 탭 전환 → 카테고리 선택 + 투표 상태 모두 초기화
- [ ] 당겨서 새로고침 → 카테고리/투표상태 필터 유지
- [ ] 무한 스크롤 → 카테고리 필터 유지된 채 새 페이지 append
