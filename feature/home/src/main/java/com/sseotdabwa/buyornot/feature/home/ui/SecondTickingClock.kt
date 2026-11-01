package com.sseotdabwa.buyornot.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

private const val MILLIS_PER_SECOND = 1_000L

/**
 * 화면의 마감 타이머들이 함께 읽는 현재 시각(epoch ms).
 *
 * 화면이 RESUMED인 동안에만 초 경계마다 갱신하고, 백그라운드에서 돌아오면 바로 현재 시각으로 맞춘다.
 * 값을 읽는 컴포저블만 다시 그려지므로 State 자체가 아니라 `{ clock.value }` 람다로 넘겨 타이머 안에서만 읽는다.
 */
@Composable
internal fun rememberSecondTickingClock(): State<Long> {
    val clock = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                val now = System.currentTimeMillis()
                clock.longValue = now
                // 고정 1초 delay는 초 경계에서 밀려 같은 초를 두 번 그리거나 건너뛰므로 다음 경계까지만 기다린다.
                delay(MILLIS_PER_SECOND - now % MILLIS_PER_SECOND)
            }
        }
    }
    return clock
}
