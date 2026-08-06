package com.sseotdabwa.buyornot.performance

/** Trace 이름에 실을 화면 이름의 최대 길이. Firebase Trace 이름 상한(100자)에 접두사 여유를 둔다. */
private const val MAX_SCREEN_NAME_LENGTH = 60

/**
 * 네비게이션 route를 Trace 이름에 쓸 짧은 화면 이름으로 바꾼다.
 *
 * route를 그대로 쓸 수 없는 이유:
 * - 타입 안전 네비게이션의 route는 fully-qualified 클래스명이라 지나치게 길다.
 * - 일부 route는 인자를 갖는다(`....FeedDetailRoute/{feedId}`). 인자를 남기면 고유 Trace
 *   이름 수가 무한히 늘어나는데, Firebase는 고유 이름 수에 상한이 있다.
 *
 * 인자를 제거하고 클래스 단순명만 남겨 **화면 종류 수만큼으로 bounded** 하게 유지한다.
 * 연속 대문자(약어)는 글자별로 끊긴다 — `FAQRoute` → `f_a_q`. 정확도보다 안정성이 목적이다.
 */
internal fun screenTraceNameOf(route: String?): String? {
    if (route.isNullOrBlank()) return null

    val withoutArguments =
        route
            .substringBefore('/')
            .substringBefore('?')
    val simpleName = withoutArguments.substringAfterLast('.')
    val screenName =
        simpleName
            .removeSuffix("Route")
            .camelToSnakeCase()
            .take(MAX_SCREEN_NAME_LENGTH)
            .trim('_')

    return screenName.ifBlank { null }
}

private fun String.camelToSnakeCase(): String =
    buildString {
        this@camelToSnakeCase.forEach { char ->
            when {
                char.isUpperCase() -> {
                    if (isNotEmpty() && last() != '_') append('_')
                    append(char.lowercaseChar())
                }

                char.isLetterOrDigit() -> append(char)

                // Firebase Trace 이름에 쓸 수 없는 문자는 구분자로 접는다.
                else -> if (isNotEmpty() && last() != '_') append('_')
            }
        }
    }.trim('_')
