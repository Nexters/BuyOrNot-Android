package com.sseotdabwa.buyornot.feature.auth.ui

import android.content.Context
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.analytics.performance.Performance
import com.sseotdabwa.buyornot.core.analytics.performance.TraceNames
import com.sseotdabwa.buyornot.core.common.deeplink.PendingNavigationStore
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.AppUpdateInfo
import com.sseotdabwa.buyornot.domain.model.UpdateStrategy
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.repository.AppPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.AppUpdateRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private const val SPLASH_TIMEOUT_MILLIS = 2300L
internal const val SOFT_UPDATE_INTERVAL_MILLIS = 24 * 60 * 60 * 1000L
private const val TAG = "SplashUpdate"

internal fun resolveUpdateDialogType(
    currentVersion: Int,
    updateInfo: AppUpdateInfo?,
    lastSoftUpdateShownTime: Long,
    now: Long,
): UpdateDialogType {
    if (updateInfo == null) return UpdateDialogType.None

    return when {
        currentVersion < updateInfo.minimumVersion -> UpdateDialogType.Force
        // currentVersion >= latestVersion이면 이미 최신 버전이므로 FORCE 팝업 표시 안 함
        updateInfo.updateStrategy == UpdateStrategy.FORCE &&
            currentVersion < updateInfo.latestVersion -> UpdateDialogType.Force
        updateInfo.updateStrategy == UpdateStrategy.SOFT &&
            currentVersion < updateInfo.latestVersion -> {
            // lastSoftUpdateShownTime이 미래 값이면 시계 역행으로 판단, 표시된 적 없는 것으로 처리
            val effectiveLastShown = if (lastSoftUpdateShownTime > now) 0L else lastSoftUpdateShownTime
            if (now - effectiveLastShown >= SOFT_UPDATE_INTERVAL_MILLIS) {
                UpdateDialogType.Soft
            } else {
                UpdateDialogType.None
            }
        }
        else -> UpdateDialogType.None
    }
}

/**
 * 스플래시 화면을 위한 ViewModel
 *
 * 토큰 존재 여부와 앱 업데이트 필요 여부를 병렬로 확인하고,
 * 업데이트 팝업이 표시 중이면 네비게이션을 차단합니다.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val appUpdateRepository: AppUpdateRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val userRepository: UserRepository,
    private val performance: Performance,
    private val pendingNavigationStore: PendingNavigationStore,
) : BaseViewModel<SplashUiState, SplashIntent, SplashSideEffect>(SplashUiState()) {
    // 스플래시 진입부터 홈/로그인 분기가 결정되기까지. SPLASH_TIMEOUT_MILLIS 고정 딜레이가
    // 하한이므로, 이 값이 딜레이를 넘어서면 원격 설정 조회가 병목이라는 뜻이다.
    private val splashTrace = performance.newTrace(TraceNames.SPLASH_TO_NAVIGATION)

    init {
        checkTokenAndNavigate()
    }

    override fun handleIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.DismissSoftUpdate -> dismissSoftUpdate()
        }
    }

    private fun checkTokenAndNavigate() {
        viewModelScope.launch { runCatchingCancellable { userRepository.notifyAppOpened() } }

        viewModelScope.launch {
            splashTrace.start()

            // 토큰 체크 + 업데이트 체크 병렬 실행
            val updateInfoDeferred =
                async {
                    runCatching { appUpdateRepository.getAppUpdateInfo() }.getOrNull()
                }

            val hasValidToken =
                try {
                    userPreferencesRepository.userType.first() != UserType.GUEST
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    false
                }

            // 딥링크로 들어왔으면 브랜딩용 고정 대기를 건너뛴다. 링크를 누른 사용자는 특정 콘텐츠를
            // 보러 온 것이고, 웹은 같은 URL에서 즉시 보여주므로 2.3초를 세우면 앱만 불친절해진다.
            // 업데이트 팝업 판단은 아래에서 그대로 수행하므로 강제 업데이트는 여전히 막힌다.
            // 대기 중에 딥링크가 도착할 수도 있다(이미 실행 중인 앱에 링크를 탭하면 onNewIntent로 온다).
            // 진입 시점에 한 번만 확인하면 그 사용자는 고정 대기를 끝까지 기다리게 되므로,
            // «pending 도착»과 타임아웃을 경쟁시킨다. 이미 도착해 있으면 first()가 즉시 반환한다.
            withTimeoutOrNull(SPLASH_TIMEOUT_MILLIS) {
                pendingNavigationStore.pending.filterNotNull().first()
            }

            // 업데이트 다이얼로그 타입 결정
            val updateInfo = updateInfoDeferred.await()
            val currentVersion =
                PackageInfoCompat
                    .getLongVersionCode(context.packageManager.getPackageInfo(context.packageName, 0))
                    .toInt()
            val dialogType = determineDialogType(currentVersion, updateInfo)

            Log.d(TAG, "currentVersion=$currentVersion, dialogType=$dialogType, updateInfo=$updateInfo")

            // 업데이트 팝업 대기는 사용자 반응 시간이라 지표에서 제외한다 — 여기서 끊어야
            // splash_to_navigation이 순수하게 앱이 소비한 시간만 담는다.
            splashTrace.putAttribute("update_dialog", dialogType.toString())
            splashTrace.putAttribute("has_valid_token", hasValidToken.toString())
            splashTrace.stop()

            if (dialogType != UpdateDialogType.None) {
                updateState { it.copy(updateDialogType = dialogType) }
                // 팝업이 닫힐 때까지 네비게이션 차단
                uiState.first { it.updateDialogType == UpdateDialogType.None }
            }

            if (hasValidToken) {
                sendSideEffect(SplashSideEffect.NavigateToHome)
            } else {
                sendSideEffect(SplashSideEffect.NavigateToLogin)
            }

            updateState { it.copy(isLoading = false) }
        }
    }

    private suspend fun determineDialogType(
        currentVersion: Int,
        updateInfo: AppUpdateInfo?,
    ): UpdateDialogType {
        val now = System.currentTimeMillis()
        val lastShown = appPreferencesRepository.lastSoftUpdateShownTime.first()
        return resolveUpdateDialogType(currentVersion, updateInfo, lastShown, now)
    }

    private fun dismissSoftUpdate() {
        viewModelScope.launch {
            try {
                appPreferencesRepository.updateLastSoftUpdateShownTime(System.currentTimeMillis())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save soft update shown time", e)
            } finally {
                updateState { it.copy(updateDialogType = UpdateDialogType.None) }
            }
        }
    }
}
