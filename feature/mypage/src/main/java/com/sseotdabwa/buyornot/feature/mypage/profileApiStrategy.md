# MyPage Feature API 연동 계획

## 목표

`feature/mypage` 모듈에서 사용자 API를 연동하고, 로그아웃 및 회원탈퇴를 구현합니다.
1.  `MyPageScreen`에서 사용자 프로필 정보를 조회합니다.
2.  `AccountSettingScreen`에서 로그아웃을 처리하고, 회원탈퇴 화면으로 이동합니다.
3.  `WithdrawalScreen`에서 회원 탈퇴(`DELETE /api/v1/users/me`)를 처리합니다.
4.  로그아웃/회원탈퇴 성공 시, 로컬 토큰을 삭제하고 연결된 소셜 계정(카카오/구글)을 로그아웃/연결해제 합니다.

## 참고 아키텍처

- `core/ui`의 `BaseViewModel`을 상속하는 MVI 아키텍처를 따릅니다.
- ViewModel에서 `Context`를 받아 소셜 SDK를 직접 호출하고, Repository는 서버 API 및 로컬 데이터 처리를 담당합니다.
- MVI 계약(State, Intent, SideEffect)은 `Contract.kt` 파일로 분리하여 관리합니다.

## 연동 전략

### 1. Network Layer (`core/network`)
- `UserApiService` 정의는 기존 계획과 동일하게 유지합니다. (`getMyProfile`, `deleteMyAccount`)

### 2. Domain Layer (`domain`)
- **`AuthRepository` Interface 정의:**
    - 로컬 토큰 삭제 함수를 정의합니다.
    - ```kotlin
      // In domain/src/main/kotlin/com/sseotdabwa/buyornot/domain/repository/AuthRepository.kt
      interface AuthRepository {
          // ... login methods
          suspend fun clearTokens()
      }
      ```
- **`UserRepository` Interface 정의:** 기존 계획과 동일합니다. (`getMyProfile`, `deleteMyAccount`)

### 3. Data Layer (`core/data`)
- **`AuthRepositoryImpl` 구현:**
    - `clearTokens`를 구현합니다.
    - ```kotlin
      // In core/data/src/main/kotlin/com/sseotdabwa/buyornot/core/data/repository/AuthRepositoryImpl.kt
      class AuthRepositoryImpl @Inject constructor(
          // ...
          private val userPreferencesDataSource: UserPreferencesDataSource
      ) : AuthRepository {
          // ... login methods
          override suspend fun clearTokens() {
              userPreferencesDataSource.clearTokens()
          }
      }
      ```
- **`UserRepositoryImpl` 구현:** 기존 계획과 동일합니다.

### 4. Feature Layer (`feature/mypage`)

#### 4.1. MyPageScreen - 사용자 정보 조회
- `MyPageViewModel` 및 관련 UI 구현은 기존 계획과 동일하게 유지합니다.

#### 4.2. AccountSettingScreen - 로그아웃

- **MVI 요소 정의:** (`AccountSettingContract.kt`)
    - ```kotlin
      data class AccountSettingUiState(
          val isLoading: Boolean = false,
          val userProfile: UserProfile? = null,
          val isLogoutDialogVisible: Boolean = false,
      )

      sealed interface AccountSettingIntent {
          data object FetchProfile : AccountSettingIntent
          data class Logout(val context: Context) : AccountSettingIntent
          data object ShowLogoutDialog : AccountSettingIntent
          data object DismissLogoutDialog : AccountSettingIntent
      }

      sealed interface AccountSettingSideEffect {
          data class ShowSnackbar(val message: String) : AccountSettingSideEffect
          data object NavigateToLogin : AccountSettingSideEffect
      }
      ```
- **ViewModel 구현:** (`AccountSettingViewModel.kt`)
    - `AccountSettingViewModel`은 로그아웃 로직과 로그아웃 확인 다이얼로그 상태만 관리합니다.
- **UI (Composable) 구현:** (`AccountSettingScreen.kt`)
    - '회원 탈퇴' 버튼 클릭 시, `WithdrawalScreen`으로 이동하는 `onNavigateToWithdrawal()` 콜백을 호출합니다.
    - 로그아웃 확인 다이얼로그의 `onDismiss`에서 로그아웃 인텐트를 호출합니다.

#### 4.3. WithdrawalScreen - 회원 탈퇴

- **MVI 요소 정의:** (`WithdrawalContract.kt`)
    - ```kotlin
      data class WithdrawalUiState(
          val isLoading: Boolean = false,
          val userProfile: UserProfile? = null,
      )

      sealed interface WithdrawalIntent {
          data object FetchProfile : WithdrawalIntent
          data class Withdraw(val context: Context) : WithdrawalIntent
      }

      sealed interface WithdrawalSideEffect {
          data class ShowSnackbar(val message: String) : WithdrawalSideEffect
          data object NavigateToLogin : WithdrawalSideEffect
      }
      ```
- **ViewModel 구현:** (`WithdrawalViewModel.kt`)
    - `WithdrawalViewModel`이 회원 탈퇴와 관련된 모든 로직을 담당합니다.
- **UI (Composable) 구현:** (`WithdrawalScreen.kt`)
    - `BuyOrNotConfirmDialog`의 `onDismiss` 콜백에서 `Withdraw` 인텐트를 호출합니다.

#### 4.4. Navigation

- **네비게이션 라우트 정의:** (`MyPageNavigation.kt`)
    - 모든 라우트 경로는 `MyPageScreens`라는 Sealed Class로 정의하여 타입 안전성을 확보합니다.
    - ```kotlin
      // In feature/mypage/navigation/MyPageNavigation.kt
      sealed class MyPageScreens(
          val route: String,
      ) {
          object Graph : MyPageScreens("mypage_graph")
          object Main : MyPageScreens("mypage_main")
          object AccountSetting : MyPageScreens("account_setting")
          object Policy : MyPageScreens("policy")
          object Withdrawal : MyPageScreens("withdrawal")
      }

      fun NavController.navigateToMyPage() {
          this.navigate(MyPageScreens.Graph.route)
      }

      fun NavController.navigateToAccountSetting() {
          this.navigate(MyPageScreens.AccountSetting.route)
      }

      fun NavController.navigateToPolicy() {
          this.navigate(MyPageScreens.Policy.route)
      }

      fun NavController.navigateToWithdrawal() {
          this.navigate(MyPageScreens.Withdrawal.route)
      }
      ```
- **네비게이션 그래프 정의:**
    - `MyPageNavigation.kt`에 `myPageGraph` NavGraphBuilder 확장 함수를 정의합니다.
    - ```kotlin
      // In feature/mypage/navigation/MyPageNavigation.kt
      fun NavGraphBuilder.myPageGraph(
          navController: NavHostController,
          versionName: String,
          onNavigateToLogin: () -> Unit,
          onNavigateToTerms: () -> Unit, // 정책 화면에서 약관으로 이동
          onNavigateToPrivacyPolicy: () -> Unit, // 정책 화면에서 개인정보처리방침으로 이동
      ) {
          navigation(
              startDestination = MyPageScreens.Main.route,
              route = MyPageScreens.Graph.route,
          ) {
              composable(MyPageScreens.Main.route) {
                  MyPageRoute(
                      versionName = versionName,
                      onBackClick = navController::popBackStack,
                      onAccountSettingClick = navController::navigateToAccountSetting,
                      onPolicyClick = navController::navigateToPolicy,
                  )
              }

              composable(MyPageScreens.AccountSetting.route) {
                  AccountSettingRoute(
                      onBackClick = navController::popBackStack,
                      onNavigateToLogin = onNavigateToLogin,
                      onNavigateToWithdrawal = navController::navigateToWithdrawal,
                  )
              }

              composable(MyPageScreens.Policy.route) {
                  PolicyRoute(
                      onBackClick = navController::popBackStack,
                      onNavigateToTerms = onNavigateToTerms,
                      onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                  )
              }

              composable(MyPageScreens.Withdrawal.route) {
                  WithdrawalRoute(
                      onBackClick = navController::popBackStack,
                      onNavigateToLogin = onNavigateToLogin,
                  )
              }
          }
      }
      ```
