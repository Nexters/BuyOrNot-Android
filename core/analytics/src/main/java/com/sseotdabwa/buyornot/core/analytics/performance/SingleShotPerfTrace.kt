package com.sseotdabwa.buyornot.core.analytics.performance

/**
 * Trace 하나가 한 번만 측정되도록 보장하는 데코레이터.
 *
 * 홈 피드 로딩처럼 트리거가 여러 경로에서 겹쳐 들어오는 구간은 [start] 가 중복
 * 호출되거나 [stop] 이 [start] 없이 호출될 수 있다. Firebase Trace는 그 경우
 * 경고를 남기고 구간을 버리므로, 지표가 조용히 비는 대신 첫 측정만 남긴다.
 *
 * 종료 후의 속성/측정항목 추가도 무시한다 — Firebase에서 반영되지 않는 호출이다.
 */
class SingleShotPerfTrace(
    private val delegate: PerfTrace,
) : PerfTrace {
    private var started = false
    private var stopped = false

    override fun start() {
        if (started) return
        started = true
        delegate.start()
    }

    override fun stop() {
        if (!started || stopped) return
        stopped = true
        delegate.stop()
    }

    override fun putAttribute(
        name: String,
        value: String,
    ) {
        if (stopped) return
        delegate.putAttribute(name, value)
    }

    override fun putMetric(
        name: String,
        value: Long,
    ) {
        if (stopped) return
        delegate.putMetric(name, value)
    }
}
