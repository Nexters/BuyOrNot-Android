package com.sseotdabwa.buyornot.core.common.deeplink

/**
 * `https://{host}/feed/{feedId}` 앱 링크에서 feedId를 뽑는다.
 *
 * `android.net.Uri`를 받지 않고 이미 분해된 host·pathSegments를 받는 이유는
 * 이 규칙이 앱 링크 동작의 유일한 분기점이라 계측기 없이 단위 테스트로 고정해두기 위해서다.
 *
 * `?utm_source=` 같은 쿼리가 붙어도 깨지지 않도록 path 기준으로만 판단한다.
 *
 * @param host 수신 Uri의 host
 * @param pathSegments 수신 Uri의 path segments (빈 세그먼트가 제거된 형태)
 * @param expectedHost 이 빌드가 가로채기로 등록한 host (`BuildConfig.APP_LINK_HOST`)
 * @return 유효한 feedId, 규칙에 맞지 않으면 null
 */
fun feedIdFromAppLink(
    host: String?,
    pathSegments: List<String>,
    expectedHost: String,
): Long? {
    // intent-filter가 이미 host를 걸러주지만, adb·타 앱이 임의 Uri로 액티비티를 열 수 있어 한 번 더 본다.
    if (host != expectedHost) return null
    if (pathSegments.size < 2 || pathSegments[0] != FEED_PATH_SEGMENT) return null
    return pathSegments[1].toLongOrNull()?.takeIf { it > 0L }
}

private const val FEED_PATH_SEGMENT = "feed"
