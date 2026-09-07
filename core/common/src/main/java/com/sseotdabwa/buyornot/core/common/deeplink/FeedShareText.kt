package com.sseotdabwa.buyornot.core.common.deeplink

/**
 * 피드 공유 링크를 만든다. [feedIdFromAppLink]가 읽는 것과 같은 규칙이다 —
 * 링크를 만드는 쪽과 읽는 쪽이 어긋나지 않도록 한 파일 옆에 둔다.
 */
fun feedShareUrlOf(
    host: String,
    feedId: Long,
): String = "https://$host/$FEED_PATH_SEGMENT/$feedId"

/**
 * 공유 시트에 실을 텍스트를 만든다.
 *
 * 제목을 앞에 붙이는 이유는 OG 미리보기가 뜨지 않는 환경(문자·일부 앱)에서도 무엇을
 * 물어보는지 전달되게 하려는 것이다. 카톡·슬랙에서는 OG 카드가 함께 뜬다.
 *
 * 제목이 비었거나 공백뿐이면 URL만 반환한다. 따옴표만 남은 텍스트가 나가면 안 된다.
 */
fun feedShareTextOf(
    host: String,
    feedId: Long,
    title: String,
): String {
    val url = feedShareUrlOf(host, feedId)
    val trimmed = title.trim()
    return if (trimmed.isEmpty()) url else "'$trimmed' 살까 말까?\n$url"
}
