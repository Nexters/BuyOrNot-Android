package com.sseotdabwa.buyornot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuyOrNotViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val isFirstRun =
        userPreferencesRepository.userPreferences
            .map { it.isFirstRun }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    fun updateIsFirstRun(isFirstRun: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateIsFirstRun(isFirstRun)
        }
    }
}
