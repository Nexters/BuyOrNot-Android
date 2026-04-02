package com.sseotdabwa.buyornot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.domain.repository.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyOrNotViewModel
    @Inject
    constructor(
        private val appPreferencesRepository: AppPreferencesRepository,
    ) : ViewModel() {
        val isFirstRun =
            appPreferencesRepository.isFirstRun
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = false,
                )

        fun updateIsFirstRun(isFirstRun: Boolean) {
            viewModelScope.launch {
                appPreferencesRepository.updateIsFirstRun(isFirstRun)
            }
        }
    }
