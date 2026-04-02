package com.sseotdabwa.buyornot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.BuildConfig
import com.sseotdabwa.buyornot.domain.model.UpdateStrategy
import com.sseotdabwa.buyornot.domain.repository.AppPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.AppUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UpdateDialogType {
    data object None : UpdateDialogType
    data object Soft : UpdateDialogType
    data object Force : UpdateDialogType
}

@HiltViewModel
class BuyOrNotViewModel
    @Inject
    constructor(
        private val appPreferencesRepository: AppPreferencesRepository,
        private val appUpdateRepository: AppUpdateRepository,
    ) : ViewModel() {
        val isFirstRun =
            appPreferencesRepository.isFirstRun
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = false,
                )

        private val _updateDialogType = MutableStateFlow<UpdateDialogType>(UpdateDialogType.None)
        val updateDialogType: StateFlow<UpdateDialogType> = _updateDialogType.asStateFlow()

        init {
            checkAppUpdate()
        }

        private fun checkAppUpdate() {
            viewModelScope.launch {
                runCatching {
                    val updateInfo = appUpdateRepository.getAppUpdateInfo()
                    val currentVersion = BuildConfig.VERSION_CODE

                    val dialogType =
                        when {
                            currentVersion < updateInfo.minimumVersion -> UpdateDialogType.Force
                            updateInfo.updateStrategy == UpdateStrategy.FORCE -> UpdateDialogType.Force
                            updateInfo.updateStrategy == UpdateStrategy.SOFT &&
                                currentVersion < updateInfo.latestVersion -> {
                                val lastShown = appPreferencesRepository.lastSoftUpdateShownTime.first()
                                val now = System.currentTimeMillis()
                                if (now - lastShown >= SOFT_UPDATE_INTERVAL_MILLIS) {
                                    UpdateDialogType.Soft
                                } else {
                                    UpdateDialogType.None
                                }
                            }
                            else -> UpdateDialogType.None
                        }

                    _updateDialogType.value = dialogType
                }
            }
        }

        fun updateIsFirstRun(isFirstRun: Boolean) {
            viewModelScope.launch {
                appPreferencesRepository.updateIsFirstRun(isFirstRun)
            }
        }

        fun dismissSoftUpdate() {
            viewModelScope.launch {
                appPreferencesRepository.updateLastSoftUpdateShownTime(System.currentTimeMillis())
                _updateDialogType.value = UpdateDialogType.None
            }
        }

        companion object {
            private const val SOFT_UPDATE_INTERVAL_MILLIS = 24 * 60 * 60 * 1000L
        }
    }
