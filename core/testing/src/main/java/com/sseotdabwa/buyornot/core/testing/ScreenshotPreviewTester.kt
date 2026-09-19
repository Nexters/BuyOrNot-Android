package com.sseotdabwa.buyornot.core.testing

import com.airbnb.lottie.LottieTask
import com.github.takahirom.roborazzi.AndroidComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import java.util.concurrent.Executor

private typealias AndroidPreviewTestParameter =
    ComposePreviewTester.TestParameter.JUnit4TestParameter.AndroidPreviewJUnit4TestParameter

/**
 * 생성된 프리뷰 테스트에 우리 설정을 끼워 넣는 유일한 훅 (#153).
 *
 * `ScreenshotTestApplication` 을 쓰려 했지만 Robolectric 이 이 구성에서 Application 의
 * `init` 도 `onCreate` 도 호출하지 않는다 (인스턴스는 만들어져 `applicationContext` 로는
 * 보이지만 생성자 블록이 돌지 않는다). Coil 설정이 동작하는 것은 `ImageLoaderFactory` 를
 * Coil 싱글턴이 지연 호출하기 때문이지 Application 생명주기 덕이 아니다.
 *
 * 그래서 테스트 시작 지점이 필요한 설정은 여기에 둔다.
 */
@OptIn(ExperimentalRoborazziApi::class)
class ScreenshotPreviewTester : ComposePreviewTester<AndroidPreviewTestParameter> {
    private val delegate = AndroidComposePreviewTester()

    override fun options(): ComposePreviewTester.Options = delegate.options()

    override fun testParameters(): List<AndroidPreviewTestParameter> = delegate.testParameters()

    override fun test(testParameter: AndroidPreviewTestParameter) {
        installSynchronousLottieExecutor()
        delegate.test(testParameter)
    }

    private companion object {
        /**
         * 로티는 composition 파싱을 [LottieTask.EXECUTOR] 에서 돌린다. Compose 의 idle 감지
         * 바깥이라 테스트가 완료를 기다리지 못하고, `composition` 이 `null` 인 채로 캡처되어
         * 화면 전체가 빈 골든이 된다.
         *
         * 호출 스레드에서 즉시 실행시키면 파싱이 리컴포지션 전에 끝난다.
         * Coil 디스패처를 `Dispatchers.Main.immediate` 로 묶은 것과 같은 이유다.
         */
        fun installSynchronousLottieExecutor() {
            LottieTask.EXECUTOR = Executor { it.run() }
        }
    }
}
