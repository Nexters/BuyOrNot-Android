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
 * 메인 스레드에서만 접근한다. 프레임 콜백과 네비게이션 변경이 모두 메인 스레드다.
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

    private var currentScreen: String? = null
    private var renderTrace: PerfTrace? = null
    private var framesTrace: PerfTrace? = null
    private val frameStats = ScreenFrameStats()

    fun onScreenEntered(screen: String) {
        if (screen == currentScreen) return

        stopFramesTrace()
        abandonRenderTrace()

        currentScreen = screen
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
        // 프레임이 한 장도 없으면(즉시 스쳐 지나간 화면) 0으로 평균을 흐리지 않도록 버린다.
        if (frameStats.isEmpty) return

        trace.putMetric(METRIC_TOTAL_FRAMES, frameStats.totalFrames)
        trace.putMetric(METRIC_JANK_FRAMES, frameStats.jankFrames)
        trace.putMetric(METRIC_FROZEN_FRAMES, frameStats.frozenFrames)
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
    }
}
