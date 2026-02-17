### 중앙 스낵바 관리 전략 (상태 호이스팅 방식)

**1. 목표 (Goal)**
- `BuyOrNotApp.kt`의 최상단 `Scaffold`에서 스낵바를 중앙 관리한다.
- `core:common`과 `core:designsystem` 간의 부적절한 의존성 문제를 회피한다.
- Compose의 상태 호이스팅(State Hoisting)과 `CompositionLocal` 패턴을 활용한다.

**2. 핵심 아이디어 (Core Idea)**
- 스낵바 표시에 필요한 상태(`SnackbarHostState`)와 로직(`showBuyOrNotSnackBar`)을 포함하는 `BuyOrNotSnackbarState` 클래스를 UI 계층(`app` 모듈)에 생성한다.
- 이 상태 객체를 `BuyOrNotApp`에서 `remember`로 생성하고, `CompositionLocal`을 통해 앱의 전체 UI 트리에 제공한다.
- 각 화면의 `ViewModel`은 이전처럼 `ShowSnackbar` `SideEffect`를 발생시킨다.
- 각 화면의 `...Route` 컴포저블은 `CompositionLocal`로부터 `BuyOrNotSnackbarState`를 얻고, `SideEffect`를 구독하여 `snackbarState.show(...)`를 호출한다.

**3. 구현 단계 (Implementation Steps)**

- **`BuyOrNotSnackbarState` 클래스 및 `CompositionLocal` 정의:**
    - **경로**: `core/ui/src/main/java/com/sseotdabwa/buyornot/core/ui/BuyOrNotSnackbarState.kt`
    - **`BuyOrNotSnackbarState` 내용**:
        - `SnackbarHostState`와 `CoroutineScope`를 내부 프로퍼티로 갖는다.
        - `showBuyOrNotSnackBar`를 호출하는 `fun show(message: String, icon: IconResource? = null, iconTint: SnackBarIconTint = SnackBarIconTint.Success)` 메서드를 노출한다.
    - **`remember...` 함수 내용**: `rememberBuyOrNotSnackbarState` 라는 `@Composable` 팩토리 함수를 만들어 `remember` 로직을 캡슐화한다.
    - **`CompositionLocal` 내용**: `val LocalSnackbarState = compositionLocalOf<BuyOrNotSnackbarState> { error("SnackbarState not provided") }` 와 같이 `CompositionLocal`을 함께 정의한다.

- **`BuyOrNotApp.kt` 수정:**
    - `rememberBuyOrNotSnackbarState()`를 호출하여 `snackbarState` 인스턴스를 생성한다.
    - `CompositionLocalProvider(LocalSnackbarState provides snackbarState)`를 사용하여 `BuyOrNotNavHost`를 감싼다.
    - 최상단 `Scaffold`의 `snackbarHost` 파라미터에 `BuyOrNotSnackBarHost(snackbarState.snackbarHostState)`를 설정한다.

- **`SideEffect` 수정:**
    - 스낵바에 아이콘을 표시할 수 있도록 `ShowSnackbar` `SideEffect` 데이터 클래스에 `icon: IconResource? = null` 및 `iconTint: SnackBarIconTint = SnackBarIconTint.Success` 파라미터를 추가한다. (대상: `LoginSideEffect.kt`, `MyPageSideEffect.kt` 등)

- **개별 화면 UI (`...Route`) 수정:**
    - **대상 파일**: `LoginScreen.kt`, `MyPageScreen.kt`, `AccountSettingScreen.kt`, `WithdrawalScreen.kt`
    - 각 파일의 `...Route` 컴포저블에서 로컬 `Scaffold`와 `SnackbarHostState`를 **삭제**한다.
    - `LocalSnackbarState.current`를 통해 중앙 `snackbarState`에 접근한다.
    - `LaunchedEffect`에서 `ViewModel`의 `ShowSnackbar` `SideEffect`를 `collect`하고, `snackbarState.show(sideEffect.message, sideEffect.icon, sideEffect.iconTint)`를 호출한다.

- **ViewModel 수정:**
    - `SnackbarManager`를 사용하는 대신, 기존처럼 `ShowSnackbar` `SideEffect`를 발생시키도록 유지하거나 복원한다. (이 전략에서는 ViewModel을 수정할 필요가 없음)


**4. 장단점 (Pros and Cons)**

- **장점**:
    - **모듈 의존성 해결**: `core:common`이 `core:designsystem`에 의존할 필요가 없어진다. 모든 UI 관련 로직이 UI 계층에 머무른다.
    - **Compose 친화적**: `CompositionLocal`을 사용하는 것은 Compose UI 트리 내에서 상태를 공유하는 자연스러운 방법이다.

- **단점**:
    - **테스트 복잡성**: `...Route` 컴포저블을 UI 테스트하려면 `CompositionLocalProvider`를 통해 `BuyOrNotSnackbarState`의 Mock 객체를 제공해야 하는 등 테스트 설정이 더 복잡해진다.
    - **보일러플레이트**: 각 `...Route` 컴포저블마다 `LocalSnackbarState.current`를 호출하고 `LaunchedEffect`를 설정하는 코드가 반복된다. (`SnackbarManager` 방식에서는 이 로직이 `BuyOrNotApp`에 단 한 번만 존재했다.)
    - **낮은 유연성**: 스낵바가 아닌 다른 방식(예: 푸시 알림)으로 메시지를 보내고 싶을 경우, UI 상태에 의존적인 현재 구조는 확장이 더 어렵다.
