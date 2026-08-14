package com.sseotdabwa.buyornot.core.analytics.performance

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace

/**
 * Firebase Performance Monitoring 기반 구현.
 *
 * debug(dev) / release(prod) 모두 이 구현을 쓴다. 두 빌드의 applicationId가 같은
 * Firebase 프로젝트에 별도 앱으로 등록돼 있어 콘솔에서 환경이 구분된다.
 * dev 빌드는 `firebase_performance_logcat_enabled` 로 로컬 확인도 가능하다.
 */
internal class FirebasePerformanceTracer(
    private val firebasePerformance: FirebasePerformance,
) : Performance {
    override fun newTrace(name: String): PerfTrace = SingleShotPerfTrace(FirebasePerfTrace(firebasePerformance.newTrace(name)))
}

private class FirebasePerfTrace(
    private val trace: Trace,
) : PerfTrace {
    override fun start() = trace.start()

    override fun stop() = trace.stop()

    override fun putAttribute(
        name: String,
        value: String,
    ) = trace.putAttribute(name, value)

    override fun putMetric(
        name: String,
        value: Long,
    ) = trace.putMetric(name, value)
}
