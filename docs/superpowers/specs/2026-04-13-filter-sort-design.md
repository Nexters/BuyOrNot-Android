# FilterChipRow 카테고리 필터 + 투표 상태 정렬 설계

**날짜**: 2026-04-13  
**대상 파일**: `feature/home/src/main/java/com/sseotdabwa/buyornot/feature/home/ui/`

---

## 개요

홈 화면의 `FilterChipRow`를 카테고리 다중 선택 칩 + 투표 상태 OptionSheet 구조로 개편한다. 카테고리 필터링은 클라이언트 로컬에서 처리하며, API 연동은 이후 별도 작업으로 진행한다.

---

## State & Intent 변경

### HomeUiState

```kotlin
// 추가
val allFeeds: List<FeedItem> = emptyList()          // 원본 피드 (API 응답 캐시)
val selectedCategories: Set<FeedCategory> = emptySet() // 빈 Set = 전체

// 기존 유지
val selectedFilter: FilterChip = FilterChip.ALL     // 투표 상태 필터
val feeds: List<FeedItem> = emptyList()             // 필터 적용 결과 (UI 렌더링용)
```

`feeds`는 항상 `allFeeds`에 `selectedCategories` + `selectedFilter`를 AND 조합으로 적용한 파생 값이다.

### HomeIntent 추가

```kotlin
data class OnCategoryToggled(val category: FeedCategory) : HomeIntent
// OnFilterSelected는 기존 그대로 OptionSheet 투표 상태 선택에 사용
```

### ViewModel 필터링 로직

- `allFeeds` 갱신 시점: `LoadFeeds`, `LoadNextPage`, `Refresh`, `OnTabSelected`
- `selectedCategories` / `selectedFilter` 변경 시: API 재호출 없이 `allFeeds` 재필터링 → `feeds` 업데이트
- 필터 조합 (AND):
  1. 카테고리: `selectedCategories.isEmpty()` 이면 전체, 아니면 `feed.category in selectedCategories.map { it.displayName }`
  2. 투표 상태: `FilterChip.ALL` 이면 전체, `IN_PROGRESS` → `!feed.isVoteEnded`, `ENDED` → `feed.isVoteEnded`

---

## UI 구조

### FilterChipRow 레이아웃

```
[ 정렬아이콘(ic_sort) ] [ 패션∙잡화 ] [ 명품∙프리미엄 ] [ 화장품∙뷰티 ] ...
```

- **정렬 아이콘 버튼** (맨 좌측)
  - `ic_sort.xml` 사용
  - 클릭 시 `OptionSheet` 표시
  - `selectedFilter != FilterChip.ALL` 일 때 아이콘 tint를 활성 색상으로 변경해 필터 적용 중임을 표시
- **카테고리 칩**: `FeedCategory.entries` 전체 9개, `FeedCategory.displayName` 텍스트 표시
  - `BuyOrNotChip(isSelected = category in selectedCategories)`
  - 클릭 시 `OnCategoryToggled` 전송 (토글: 선택 → 해제, 미선택 → 선택)
  - 아무것도 선택 안 됨 = 전체

### OptionSheet

- `FilterChipRow` 내부 `showSortSheet: Boolean` 로컬 상태로 표시 제어
- 타이틀: `"투표 상태"`
- 선택지: `전체` / `진행중 투표` / `마감된 투표` (`FilterChip.entries`의 `label`)
- `selectedOption`: 현재 `selectedFilter.label`
- 선택 후 `onFilterSelected` 호출 → 자동 닫힘 (OptionSheet 기본 동작)

### FilterChipRow 시그니처

```kotlin
private fun FilterChipRow(
    selectedCategories: Set<FeedCategory>,
    onCategoryToggled: (FeedCategory) -> Unit,
    selectedFilter: FilterChip,
    onFilterSelected: (FilterChip) -> Unit,
)
```

---

## 동작 규칙

### 필터 조합

| 카테고리 선택 | 투표 상태 | 결과 |
|---|---|---|
| 미선택 (빈 Set) | ALL | 전체 피드 |
| 선택 있음 | ALL | 선택된 카테고리의 모든 피드 |
| 미선택 | IN_PROGRESS | 진행중인 전체 피드 |
| 선택 있음 | IN_PROGRESS | 선택된 카테고리 중 진행중 피드 |
| 선택 있음 | ENDED | 선택된 카테고리 중 마감된 피드 |
| 조합 결과 빈 리스트 | — | 기존 `HomeFeedEmptyView` 표시 |

### 탭 전환 시

- `selectedCategories` → 빈 Set으로 초기화
- `selectedFilter` → `FilterChip.ALL`로 초기화
- `allFeeds` / `feeds` 초기화 후 API 재호출

### Refresh 시

- `selectedCategories`, `selectedFilter` 현재 값 유지
- `allFeeds` 갱신 후 즉시 재필터링 → `feeds` 업데이트

### LoadNextPage 시

- 새 페이지를 `allFeeds`에 append
- 재필터링으로 `feeds` 업데이트

---

## 변경 범위 요약

| 파일 | 변경 내용 |
|---|---|
| `HomeContract.kt` | `HomeUiState`에 `allFeeds`, `selectedCategories` 추가; `HomeIntent`에 `OnCategoryToggled` 추가 |
| `HomeViewModel.kt` | `handleCategoryToggled()`, `applyFilters()` 추가; `allFeeds` 관리; 탭 전환/Refresh/NextPage 로직 수정 |
| `HomeScreen.kt` | `FilterChipRow` 시그니처 변경, 정렬 아이콘 + OptionSheet 추가, `HomeFeedList`에서 `selectedCategories` 전달 |
