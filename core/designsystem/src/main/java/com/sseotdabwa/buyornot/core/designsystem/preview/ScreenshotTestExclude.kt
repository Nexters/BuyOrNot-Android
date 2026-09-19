package com.sseotdabwa.buyornot.core.designsystem.preview

/**
 * 이 프리뷰를 스크린샷 비교 대상에서 뺀다 (#153).
 *
 * 렌더가 비결정적이어서가 아니라, **렌더해도 검증할 게 없을 때** 쓴다.
 * 예를 들어 화면 전체가 비동기 로딩 결과에 걸려 있으면 Robolectric 렌더 시점에는
 * 아무것도 그려지지 않아 빈 골든이 잡힌다. 그런 프리뷰를 남겨두면 "검증되고 있다"는
 * 착각만 남으므로 명시적으로 빼고 이유를 적는다.
 *
 * Android Studio 프리뷰에는 영향이 없다.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class ScreenshotTestExclude(
    /** 왜 뺐는지. 나중에 되살릴 수 있는지 판단하려면 이유가 남아 있어야 한다. */
    val reason: String,
)
