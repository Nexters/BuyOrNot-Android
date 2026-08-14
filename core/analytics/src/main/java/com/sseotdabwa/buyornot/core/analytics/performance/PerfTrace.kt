package com.sseotdabwa.buyornot.core.analytics.performance

/**
 * 시작과 종료가 서로 다른 지점에서 일어나는 구간을 계측하는 Trace.
 *
 * 속성/측정항목은 Firebase 제한을 따른다 — 커스텀 속성 5개, 측정항목 32개,
 * 속성 이름은 32자 이내의 `A-Z a-z _` 조합.
 */
interface PerfTrace {
    fun start()

    fun stop()

    fun putAttribute(
        name: String,
        value: String,
    )

    fun putMetric(
        name: String,
        value: Long,
    )
}

/** 계측을 하지 않는 Trace. 테스트나 수집 비활성 환경에서 사용한다. */
object NoOpPerfTrace : PerfTrace {
    override fun start() = Unit

    override fun stop() = Unit

    override fun putAttribute(
        name: String,
        value: String,
    ) = Unit

    override fun putMetric(
        name: String,
        value: Long,
    ) = Unit
}
