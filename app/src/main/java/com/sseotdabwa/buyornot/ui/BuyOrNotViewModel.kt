package com.sseotdabwa.buyornot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.domain.repository.AppPreferencesRepository
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyOrNotViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analytics: Analytics,
) : ViewModel() {
    val isFirstRun =
        appPreferencesRepository.isFirstRun
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    init {
        userPreferencesRepository.userId
            .distinctUntilChanged()
            .onEach { userId ->
                analytics.identify(if (userId != 0L) userId.toString() else null)
            }.launchIn(viewModelScope)
    }

    fun updateIsFirstRun(isFirstRun: Boolean) {
        viewModelScope.launch {
            appPreferencesRepository.updateIsFirstRun(isFirstRun)
        }
    }
}
