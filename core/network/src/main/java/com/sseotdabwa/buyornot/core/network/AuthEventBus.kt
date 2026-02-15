package com.sseotdabwa.buyornot.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class AuthEvent {
    FORCE_LOGOUT,
}

@Singleton
class AuthEventBus
    @Inject
    constructor() {
        private val _events = MutableSharedFlow<AuthEvent>()
        val events = _events.asSharedFlow()

        suspend fun emit(event: AuthEvent) {
            _events.emit(event)
        }
    }
