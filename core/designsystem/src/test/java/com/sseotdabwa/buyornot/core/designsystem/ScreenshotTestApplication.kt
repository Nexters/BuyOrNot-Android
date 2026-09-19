package com.sseotdabwa.buyornot.core.designsystem

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.intercept.Interceptor
import coil.request.CachePolicy
import coil.request.ImageResult
import kotlinx.coroutines.Dispatchers

/**
 * 스크린샷 테스트용 Application (#153).
 *
 * 프리뷰는 [com.sseotdabwa.buyornot.core.designsystem.preview.PreviewImages]의 로컬 리소스 URI만
 * 쓰기로 했지만, 그건 사람이 지키는 규율이다. 새 프리뷰가 실수로 http URL을 들고 오면
 * 아무도 UI를 건드리지 않은 PR에서 diff가 뜨는데, 원인이 코드가 아니라 네트워크 상태라
 * 추적하기가 어렵다.
 *
 * 그래서 http/https 요청은 인터셉터에서 끊는다. `android.resource://`는 통과시켜 비율별 샘플
 * 이미지가 살아있게 한다.
 *
 * 인터셉터가 던지는 예외는 Coil이 삼켜 ErrorResult로 바꾸므로 테스트가 실패하지는 않는다.
 * 실제 효과는 "네트워크에 나가지 않는다"이고, 새 프리뷰가 http URL을 들고 오면 이미지가 빈 채로
 * 렌더되어 diff로 드러난다 — 네트워크 상태와 무관하게 항상 같은 결과이므로 오탐은 아니다.
 * http URL 자체를 막는 역할은 CI의 grep 가드가 맡는다.
 *
 * `src/test/resources/robolectric.properties`가 이 클래스를 Robolectric Application으로 지정한다.
 */
class ScreenshotTestApplication :
    Application(),
    ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
            // 캐시는 실행 간 상태를 남겨 렌더 결과를 실행 순서에 의존하게 만든다.
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.DISABLED)
            // Coil은 기본적으로 백그라운드에서 비동기로 로드한다. Robolectric은 그 완료를 기다려주지
            // 않으므로 이미지가 빈 채로 캡처된다. 모든 단계를 메인 디스패처에서 즉시 실행시켜
            // 렌더 시점에 이미지가 이미 올라와 있게 만든다.
            .interceptorDispatcher(Dispatchers.Main.immediate)
            .fetcherDispatcher(Dispatchers.Main.immediate)
            .decoderDispatcher(Dispatchers.Main.immediate)
            .transformationDispatcher(Dispatchers.Main.immediate)
            .components { add(NetworkImageGuard) }
            .build()

    private object NetworkImageGuard : Interceptor {
        override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
            val data = chain.request.data
            if (data is String && (data.startsWith("http://") || data.startsWith("https://"))) {
                error(
                    "스크린샷 테스트에서 네트워크 이미지 요청이 발생했습니다: $data - " +
                        "프리뷰는 PreviewImages의 로컬 리소스만 사용해야 합니다.",
                )
            }
            return chain.proceed(chain.request)
        }
    }
}
