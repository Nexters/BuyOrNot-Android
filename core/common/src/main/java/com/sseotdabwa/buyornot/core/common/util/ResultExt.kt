package com.sseotdabwa.buyornot.core.common.util

import kotlinx.coroutines.CancellationException

/**
 * [CancellationException]을 삼키지 않는 `runCatching` 래퍼 함수입니다.
 *
 * 이 함수는 코루틴 취소가 정상적인 메커니즘으로 사용되며 오류로 간주되어서는 안 되는
 * `suspend` 함수에서 유용합니다.
 *
 * @param block 실행할 suspend 함수.
 * @return [block]의 성공 또는 실패를 포함하는 [Result].
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
