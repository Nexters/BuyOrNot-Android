## 🛠 Related issue
closed #65

어떤 변경사항이 있었나요?
- [x] 🐞 BugFix Something isn't working
- [ ] 🎨 Design Markup & styling
- [ ] 📃 Docs Documentation writing and editing (README.md, etc.)
- [x] ✨ Feature Feature
- [x] 🔨 Refactor Code refactoring
- [ ] ⚙️ Setting Development environment setup
- [ ] ✅ Test Test related (Junit, etc.)

## ✅ CheckPoint
PR이 다음 요구 사항을 충족하는지 확인하세요.

- [x] PR 컨벤션에 맞게 작성했습니다. (필수)
- [x] merge할 브랜치의 위치를 확인해 주세요(main❌/develop⭕) (필수)
- [x] Approve된 PR은 assigner가 머지하고, 수정 요청이 온 경우 수정 후 다시 push를 합니다. (필수)
- [x] BugFix의 경우, 버그의 원인을 파악하였습니다. (선택)

## ✏️ Work Description

### 🐞 BugFix

- **로그인 성공 시 백스택 미제거**
  - 원인: `onLoginSuccess`에서 `popUpTo(SPLASH_ROUTE)`를 사용했으나, `navigateToLogin()` 호출 시점에 이미 SPLASH_ROUTE가 백스택에서 제거된 상태라 AUTH_ROUTE가 잔류
  - 수정: `popUpTo(AUTH_ROUTE) { inclusive = true }`로 변경 (`BuyOrNotNavHost.kt`)

- **투표 피드 중복 업로드**
  - 원인: 업로드 버튼 활성화 조건에 `isLoading` 체크 누락
  - 수정: `uiState.isLoading` 중 업로드 버튼 비활성화 및 ViewModel에서 이중 호출 방어 (`UploadScreen.kt`, `UploadViewModel.kt`)

- **회원탈퇴 다이얼로그 미노출**
  - 원인: `WithdrawalScreen`에서 `onShowWithdrawalDialog` / `onDismissWithdrawalDialog` 콜백이 전달되지 않음
  - 수정: 누락된 콜백 연결 (`WithdrawalScreen.kt`)

- **투표 낙관적 업데이트 시 득표율 노출**
  - 원인: 낙관적 업데이트로 투표 수가 임시 변경될 때 투표 결과(득표율 바)가 노출됨
  - 수정: API 응답 확정 전까지 UI에 결과를 반영하지 않도록 처리 (`HomeViewModel.kt`)

### ✨ Feature

- **스낵바 최신 메시지 즉시 교체**
  - 기존: `Mutex`로 이전 스낵바 종료 대기 후 다음 메시지 표시
  - 변경: `currentSnackbarData?.dismiss()` 호출로 현재 스낵바를 즉시 닫고 새 메시지 노출
  - 애니메이션도 케이스 분리: 교체(push down) / 최초 등장(slide up) / dismiss(slide down) (`SnackBar.kt`)

### 🔨 Refactor

- **HomeScreen 헤더 구현 단순화**
  - 기존: `SubcomposeLayout`으로 TopBar·Tab 높이를 별도 측정 → `NestedScrollConnection` + `offset`으로 수동 제어
  - 변경: TopBar를 LazyColumn의 일반 `item`으로, Tab을 `stickyHeader`로 배치
  - 제거: `SubcomposeLayout`, `HomeHeader` composable, `NestedScrollConnection`, `topBarHeightPx`/`tabHeightPx` state (`HomeScreen.kt`)

## 😅 Uncompleted Tasks
- N/A

## 📢 To Reviewers

- 스낵바 mutex 제거 후 빠르게 연속 메시지가 오는 케이스에서 `SnackbarHostState` 내부 mutex가 race condition 없이 처리하는지 확인 부탁드립니다.
- HomeScreen 리팩토링에서 `stickyHeader`를 사용하면 스크롤 시 TopBar는 올라가고 Tab만 고정되는 동작이 LazyColumn 기본 동작으로 자연스럽게 지원됩니다.

## 📃 RCA 룰
- R: 꼭 반영해 주세요. 적극적으로 고려해 주세요. (Request changes)
- C: 웬만하면 반영해 주세요. (Comment)
- A: 반영해도 좋고 넘어가도 좋습니다. 그냥 사소한 의견입니다. (Approve)
