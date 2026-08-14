package com.sseotdabwa.buyornot.performance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sseotdabwa.buyornot.core.analytics.performance.PerfTrace
import com.sseotdabwa.buyornot.core.analytics.performance.Performance
import com.sseotdabwa.buyornot.core.analytics.performance.ScreenFrameStats
import com.sseotdabwa.buyornot.core.analytics.performance.TraceNames

/**
 * 단일 Activity Compose 구조에서 화면 단위 성능 지표를 만든다.
 *
 * Firebase 자동 화면 Trace는 Activity/Fragment 단위라 이 앱에서는 `_st_MainActivity`
 * 하나로 모든 화면의 프레임이 뭉개진다. 그래서 두 지표를 직접 만든다.
 * - `screen_render_<화면>` — 화면 진입부터 콘텐츠 첫 프레임까지
 * - `screen_frames_<화면>` — 체류 구간 길이 + 프레임 품질(전체/jank/frozen)
 *
 * [onFrame] 만 JankStats 의 프레임 메트릭스 스레드에서 호출되고(API 24+), 나머지는 모두
 * 메인 스레드에서 호출된다. 두 스레드가 공유하는 상태는 [currentScreen] 과 [frameStats] 뿐이라
 * 각각 `@Volatile` 과 내부 락으로 막는다. Trace 핸들은 메인 스레드에서만 만지므로 보호하지 않는다.
 *
 * @param nonMeaningfulScreens 앱 시작 TTFD 판정에서 제외할 화면. 스플래시가 그려진 것은
 *   "앱을 쓸 수 있는 상태"가 아니므로 제외한다.
 */
class ScreenPerformanceTracker(
    private val performance: Performance,
    private val nonMeaningfulScreens: Set<String> = emptySet(),
    private val onScreenChanged: (String) -> Unit = {},
) {
    /** 스플래시가 아닌 첫 화면의 콘텐츠가 그려졌는지. 앱 시작 TTFD 보고 조건으로 쓴다. */
    var isFirstMeaningfulRenderDone: Boolean by mutableStateOf(false)
        private set

    /** [onFrame] 이 프레임 메트릭스 스레드에서 읽으므로 쓰기가 곧바로 보이도록 한다. */
    @Volatile
    private var currentScreen: String? = null

    /** 지금 측정 중인 체류 구간의 식별자. 같은 화면 종류의 연속 진입을 가르는 기준이다. */
    private var currentSessionId: String? = null
    private var renderTrace: PerfTrace? = null
    private var framesTrace: PerfTrace? = null
    private val frameStats = ScreenFrameStats()

    /**
     * 화면 진입. 이전 구간을 닫고 새 구간을 연다.
     *
     * @param screen Trace 이름에 실을 화면 이름. 고유 Trace 이름 수를 화면 종류만큼으로 묶기 위해
     *   route 인자를 접은 값이라, 서로 다른 진입이 같은 이름을 가질 수 있다.
     * @param sessionId 이 진입을 구분하는 식별자. `NavBackStackEntry.id` 를 넘긴다.
     *   [screen] 으로 구분하면 피드 A 상세 → 피드 B 상세처럼 이름이 같은 연속 진입이
     *   같은 구간으로 뭉개져, B의 렌더 Trace가 아예 열리지 않고 B의 프레임이 A의 집계로 들어간다.
     */
    fun onScreenEntered(
        screen: String,
        sessionId: String,
    ) {
        if (sessionId == currentSessionId) return

        stopFramesTrace()
        abandonRenderTrace()

        currentScreen = screen
        currentSessionId = sessionId
        onScreenChanged(screen)
        renderTrace = performance.newTrace(TraceNames.screenRender(screen)).apply { start() }
        startFramesTrace(screen)
    }

    /** [com.sseotdabwa.buyornot.core.ui.performance.ReportScreenRendered] 가 첫 프레임에서 호출한다. */
    fun onScreenContentRendered() {
        val screen = currentScreen ?: return

        renderTrace?.stop()
        renderTrace = null
        if (screen !in nonMeaningfulScreens) isFirstMeaningfulRenderDone = true
    }

    fun onFrame(
        isJank: Boolean,
        frameDurationNanos: Long,
    ) {
        if (currentScreen == null) return
        frameStats.record(isJank, frameDurationNanos)
    }

    /**
     * 백그라운드 진입 시 호출. 체류 시간이 백그라운드 시간까지 삼키지 않도록 구간을 끊는다.
     * [currentScreen] 은 유지해 [onResumed] 에서 이어받는다.
     */
    fun onPaused() {
        stopFramesTrace()
        abandonRenderTrace()
    }

    fun onResumed() {
        val screen = currentScreen ?: return
        startFramesTrace(screen)
    }

    private fun startFramesTrace(screen: String) {
        frameStats.reset()
        framesTrace = performance.newTrace(TraceNames.screenFrames(screen)).apply { start() }
    }

    private fun stopFramesTrace() {
        val trace = framesTrace ?: return
        framesTrace = null
        val stats = frameStats.snapshot()
        // 프레임이 한 장도 없으면(즉시 스쳐 지나간 화면) 0으로 평균을 흐리지 않도록 버린다.
        if (stats.isEmpty) return

        // 개수는 프레임 가중 집계용이다. 콘솔에서 sum(jank)/sum(total) 로 진짜 비율을 뽑을 수 있다.
        trace.putMetric(METRIC_TOTAL_FRAMES, stats.totalFrames)
        trace.putMetric(METRIC_JANK_FRAMES, stats.jankFrames)
        trace.putMetric(METRIC_FROZEN_FRAMES, stats.frozenFrames)

        // 비율은 세션별 분포와 회귀 알림용이다. 개수와 달리 체류 시간에 오염되지 않아
        // 화면 간 비교가 된다. 분모가 작으면 값이 튀므로 그때는 개수만 남긴다.
        if (stats.isRateReliable) {
            trace.putMetric(METRIC_JANK_RATE, stats.jankRatePermille)
            trace.putMetric(METRIC_FROZEN_RATE, stats.frozenRatePermille)
        }

        trace.stop()
    }

    /**
     * 콘텐츠 첫 프레임 전에 이탈했거나 백그라운드로 갔으면 stop 하지 않고 버린다.
     * Firebase는 stop 되지 않은 Trace를 보고하지 않으므로, 미완성 구간이 지표를 오염시키지 않는다.
     */
    private fun abandonRenderTrace() {
        renderTrace = null
    }

    private companion object {
        const val METRIC_TOTAL_FRAMES = "total_frames"
        const val METRIC_JANK_FRAMES = "jank_frames"
        const val METRIC_FROZEN_FRAMES = "frozen_frames"

        // 정수 측정항목이라 비율은 ‰ 로 싣는다. 이름에 단위를 박아 콘솔에서 오독하지 않게 한다.
        const val METRIC_JANK_RATE = "jank_rate_permille"
        const val METRIC_FROZEN_RATE = "frozen_rate_permille"
    }
}
