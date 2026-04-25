# HomeScreen 스크롤 연동 헤더 설계

## 목표

홈 화면에서 아래로 스크롤 시 `HomeTopBarSection`과 `FilterChipRow`가 자연스럽게 사라지고, 위로 스크롤하면 다시 나타나도록 한다. `HomeTabSection`은 항상 화면 상단에 고정된다.

## 요구사항

| 컴포넌트 | 스크롤 동작 |
|---|---|
| `HomeTopBarSection` | LazyColumn item — 아래 스크롤 시 사라짐, 위로 스크롤(최상단) 시 나타남 |
| `HomeTabSection` | stickyHeader — 항상 고정 |
| `FilterChipRow` | LazyColumn item — 아래 스크롤 시 사라짐, 위로 스크롤(최상단) 시 나타남 |

- 애니메이션: LazyColumn 자연 스크롤 동작 (별도 슬라이드 애니메이션 없음)
- 피드 중간에서 위로 스크롤해도 즉시 나타나는 동작은 적용하지 않음 (최상단까지 돌아가야 노출)

## 구조 변경

### 변경 전 (`HomeFeedList`)

```
PullToRefreshBox
  └── Column
       ├── 고정 헤더 Column
       │    ├── HomeTopBarSection  (top padding 적용)
       │    └── HomeTabSection
       └── LazyColumn (weight(1f))
            ├── item: FilterChipRow
            ├── item: 배너 (조건부)
            └── items: 피드
```

### 변경 후

```
PullToRefreshBox
  └── LazyColumn (fillMaxSize)
       ├── item: HomeTopBarSection
       ├── stickyHeader: HomeTabSection
       ├── item: FilterChipRow
       ├── item: 배너 (조건부)
       └── items: 피드
```

외부 `Column` 래퍼와 고정 헤더 `Column`을 제거하고, `LazyColumn`이 `PullToRefreshBox`를 직접 채운다.

## 구현 상세

### contentPadding

기존 고정 헤더에 적용하던 `innerPadding.calculateTopPadding()`을 LazyColumn의 `contentPadding` 파라미터로 이전한다.

```kotlin
LazyColumn(
    state = listState,
    modifier = Modifier.fillMaxSize(),
    contentPadding = contentPadding, // Scaffold innerPadding 전달
)
```

### stickyHeader 배경

`HomeTabSection`이 피드 아이템 위에 겹칠 때 배경이 투명해 보이지 않도록 배경색을 지정한다.

```kotlin
stickyHeader {
    HomeTabSection(
        modifier = Modifier.background(BuyOrNotTheme.colors.gray0),
        ...
    )
}
```

### OptionSheet 위치

투표 상태 필터 `OptionSheet`는 현재 `PullToRefreshBox` 내부에 오버레이로 위치하며, 구조 변경 후에도 동일하게 유지한다.

## 변경 범위

- `HomeFeedList` 컴포저블 내부 구조 재편
- `HomeTabSection` 시그니처에 `modifier` 파라미터 추가 (없으면)
- 기타 컴포저블 시그니처 변경 없음
