package com.sseotdabwa.buyornot.feature.auth.ui

import android.content.Context
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.UpdateStrategy
import com.sseotdabwa.buyornot.domain.model.UserType
import com.sseotdabwa.buyornot.domain.repository.AppPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.AppUpdateRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private const val SPLASH_TIMEOUT_MILLIS = 2300L
internal const val SOFT_UPDATE_INTERVAL_MILLIS = 24 * 60 * 60 * 1000L
private const val TAG = "SplashUpdate"

internal fun resolveUpdateDialogType(
    currentVersion: Int,
    updateInfo: com.sseotdabwa.buyornot.domain.model.AppUpdateInfo?,
    lastSoftUpdateShownTime: Long,
    now: Long,
): UpdateDialogType {
    if (updateInfo == null) return UpdateDialogType.None

    return when {
        currentVersion < updateInfo.minimumVersion -> UpdateDialogType.Force
        updateInfo.updateStrategy == UpdateStrategy.FORCE &&
            currentVersion < updateInfo.latestVersion -> UpdateDialogType.Force
        updateInfo.updateStrategy == UpdateStrategy.SOFT &&
            currentVersion < updateInfo.latestVersion -> {
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
) : BaseViewModel<SplashUiState, SplashIntent, SplashSideEffect>(SplashUiState()) {
    init {
        checkTokenAndNavigate()
    }

    override fun handleIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.DismissSoftUpdate -> dismissSoftUpdate()
        }
    }

    private fun checkTokenAndNavigate() {
        viewModelScope.launch {
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

            delay(SPLASH_TIMEOUT_MILLIS)

            // 업데이트 다이얼로그 타입 결정
            val updateInfo = updateInfoDeferred.await()
            val currentVersion =
                PackageInfoCompat
                    .getLongVersionCode(context.packageManager.getPackageInfo(context.packageName, 0))
                    .toInt()
            val dialogType = determineDialogType(currentVersion, updateInfo)

            Log.d(TAG, "currentVersion=$currentVersion, dialogType=$dialogType, updateInfo=$updateInfo")

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
        updateInfo: com.sseotdabwa.buyornot.domain.model.AppUpdateInfo?,
    ): UpdateDialogType {
        val now = System.currentTimeMillis()
        val lastShown = appPreferencesRepository.lastSoftUpdateShownTime.first()
        return resolveUpdateDialogType(currentVersion, updateInfo, lastShown, now)
    }

    private fun dismissSoftUpdate() {
        viewModelScope.launch {
            try {
                appPreferencesRepository.updateLastSoftUpdateShownTime(System.currentTimeMillis())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save soft update shown time", e)
            } finally {
                updateState { it.copy(updateDialogType = UpdateDialogType.None) }
            }
        }
    }
}
