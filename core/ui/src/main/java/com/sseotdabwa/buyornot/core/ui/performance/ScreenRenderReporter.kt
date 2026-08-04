package com.sseotdabwa.buyornot.core.ui.performance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos

/**
 * 화면 콘텐츠가 실제로 그려졌음을 계측 계층에 알리는 통로.
 *
 * 구현은 app 모듈이 주입한다. core/ui 가 계측 구현(Firebase 등)에 의존하지 않도록
 * CompositionLocal 로 끊어 둔다.
 */
@Stable
fun interface ScreenRenderReporter {
    fun onScreenContentRendered()
}

/** 기본값은 no-op — 프리뷰와 테스트에서 별도 설정 없이 동작한다. */
val LocalScreenRenderReporter =
    staticCompositionLocalOf<ScreenRenderReporter> { ScreenRenderReporter { } }

/**
 * 콘텐츠가 준비된 뒤 **첫 프레임이 나간 시점**에 한 번 보고한다.
 *
 * 상태 반영 시점이 아니라 프레임 시점을 재는 것이 핵심이다. 상태만 보고 끝내면
 * 컴포지션·레이아웃 비용(목록이라면 특히 크다)이 지표에서 빠진다.
 *
 * @param ready 스켈레톤/스피너가 아닌 **실제 콘텐츠**를 그릴 수 있는 상태.
 *   로딩 인디케이터가 뜬 시점을 재지 않도록 호출부에서 로딩 상태를 제외해야 한다.
 * @param onRendered 화면 고유의 추가 처리. 화면별 Trace 종료 등에 쓴다.
 */
@Composable
fun ReportScreenRendered(
    ready: Boolean,
    onRendered: () -> Unit = {},
) {
    val reporter = LocalScreenRenderReporter.current
    var reported by remember { mutableStateOf(false) }

    LaunchedEffect(ready, reported) {
        if (!ready || reported) return@LaunchedEffect
        // 이 컴포지션 결과가 프레임으로 나간 뒤 재개된다. draw 완료보다 한 프레임 이내로 빠르다.
        withFrameNanos { }
        reported = true
        reporter.onScreenContentRendered()
        onRendered()
    }
}
