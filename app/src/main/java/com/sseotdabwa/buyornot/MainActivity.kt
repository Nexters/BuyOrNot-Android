package com.sseotdabwa.buyornot

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.metrics.performance.JankStats
import androidx.metrics.performance.PerformanceMetricsState
import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.core.analytics.AnalyticsEvent
import com.sseotdabwa.buyornot.core.analytics.performance.Performance
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.feature.auth.navigation.SplashRoute
import com.sseotdabwa.buyornot.notification.FcmKeys
import com.sseotdabwa.buyornot.notification.PushDestination
import com.sseotdabwa.buyornot.notification.pushDestinationOf
import com.sseotdabwa.buyornot.performance.ScreenPerformanceTracker
import com.sseotdabwa.buyornot.performance.screenTraceNameOf
import com.sseotdabwa.buyornot.ui.BuyOrNotApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/** JankStats 프레임에 붙이는 상태 키. 이 태그로 프레임을 화면별로 가른다. */
private const val FRAME_STATE_SCREEN = "screen"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authEventBus: AuthEventBus

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var performance: Performance

    private val pendingFeedDeepLink = MutableStateFlow<PendingFeedDeepLink?>(null)

    // 피드 상세가 아닌 목적지(홈·투표 등록). feedId가 없어 [PendingFeedDeepLink]로 표현할 수 없다.
    private val pendingPushDestination = MutableStateFlow<PushDestination?>(null)

    private var jankStats: JankStats? = null
    private var metricsStateHolder: PerformanceMetricsState.Holder? = null
    private lateinit var screenPerformanceTracker: ScreenPerformanceTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 딥링크 처리가 feedId extra를 소비하므로 로깅을 먼저 수행한다.
        handlePushOpened(intent)
        handlePushNavigation(intent)
        screenPerformanceTracker =
            ScreenPerformanceTracker(
                performance = performance,
                // 스플래시가 그려진 것은 "앱을 쓸 수 있는 상태"가 아니므로 TTFD 판정에서 제외한다.
                nonMeaningfulScreens = setOfNotNull(screenTraceNameOf(SplashRoute::class.qualifiedName)),
                onScreenChanged = { screen -> metricsStateHolder?.state?.putState(FRAME_STATE_SCREEN, screen) },
            )
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    scrim = Color.WHITE,
                    darkScrim = Color.WHITE,
                ),
            navigationBarStyle =
                SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
        )
        setContent {
            val pendingDeepLink by pendingFeedDeepLink.collectAsStateWithLifecycle()
            val pendingDestination by pendingPushDestination.collectAsStateWithLifecycle()
            BuyOrNotTheme {
                BuyOrNotApp(
                    authEventBus = authEventBus,
                    screenPerformanceTracker = screenPerformanceTracker,
                    pendingFeedDeepLink = pendingDeepLink,
                    onPendingFeedDeepLinkConsumed = { pendingFeedDeepLink.value = null },
                    pendingPushDestination = pendingDestination,
                    onPendingPushDestinationConsumed = { pendingPushDestination.value = null },
                    onBackPressed = { finish() },
                    onFinish = { finishAffinity() },
                )
            }
        }
        startTrackingFrames()
    }

    /**
     * 프레임 품질 수집을 시작한다.
     *
     * 단일 Activity라 JankStats 인스턴스도 하나다. 화면 구분은 프레임에 붙는
     * [FRAME_STATE_SCREEN] 태그로 하며, 태그는 네비게이션 변경 시 갱신된다.
     *
     * `setContent` 이후에 호출해야 [PerformanceMetricsState] 가 붙을 뷰 계층이 존재한다.
     */
    private fun startTrackingFrames() {
        metricsStateHolder = PerformanceMetricsState.getHolderForHierarchy(window.decorView)
        jankStats =
            JankStats.createAndTrack(window) { frameData ->
                // API 24+ 에서는 메인 스레드가 아닌 프레임 메트릭스 스레드에서 매 프레임 실행된다.
                // frameData 는 다음 프레임에 재사용되므로 필요한 값만 즉시 읽고 넘긴다.
                // 카운터 증가만 하고 할당·I/O를 하지 않는다.
                screenPerformanceTracker.onFrame(frameData.isJank, frameData.frameDurationUiNanos)
            }
    }

    override fun onResume() {
        super.onResume()
        jankStats?.isTrackingEnabled = true
        screenPerformanceTracker.onResumed()
    }

    override fun onPause() {
        super.onPause()
        jankStats?.isTrackingEnabled = false
        // 백그라운드 시간이 화면 체류 시간으로 잡히지 않도록 구간을 끊는다.
        screenPerformanceTracker.onPaused()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePushOpened(intent)
        handlePushNavigation(intent)
    }

    /**
     * 알림 탭 유입을 로깅한다. 딥링크(feedId 필요)와 분리해야 feedId가 없는 마케팅 알림 탭도 잡힌다.
     *
     * 탭 판별은 서버 data 페이로드 키인 [FcmKeys.TYPE]의 존재로 한다. 앱이 심은 마커를 쓰면
     * 백그라운드·종료 상태 탭(FCM이 launch Intent를 만드는 경로)이 전부 누락된다.
     */
    private fun handlePushOpened(intent: Intent?) {
        if (intent == null || !intent.hasExtra(FcmKeys.TYPE)) return
        val pushType = intent.getStringExtra(FcmKeys.TYPE) ?: FcmKeys.UNKNOWN_TYPE
        val feedId = intent.longExtraOrNull(FcmKeys.FEED_ID)
        val notificationId = intent.longExtraOrNull(FcmKeys.NOTIFICATION_ID)
        // 회전·프로세스 재생성으로 onCreate가 같은 Intent를 다시 받아도 중복 발행되지 않도록 마커를 소비한다.
        intent.removeExtra(FcmKeys.TYPE)
        setIntent(intent)
        if (BuildConfig.DEBUG) {
            Log.d("FCM", "handlePushOpened - pushType=$pushType, feedId=$feedId, notificationId=$notificationId")
        }
        analytics.track(
            AnalyticsEvent.PushOpened(
                pushType = pushType,
                feedId = feedId,
                notificationId = notificationId,
            ),
        )
    }

    /**
     * 알림 payload의 `screen`·`feedId`로 이동 대상을 정해 pending 상태에 넣는다.
     *
     * 목적지 판정 규칙은 [pushDestinationOf]에 있다 — 여기서는 Intent를 분해하고 소비하는 일만 한다.
     */
    private fun handlePushNavigation(intent: Intent?) {
        if (intent == null) return
        val screen = intent.getStringExtra(FcmKeys.SCREEN)
        val feedId = intent.longExtraOrNull(FcmKeys.FEED_ID)
        val notificationId = intent.longExtraOrNull(FcmKeys.NOTIFICATION_ID)
        if (screen == null && feedId == null) return

        // 소비 마커. 이게 없으면 회전·프로세스 재생성 때 onCreate가 같은 Intent를 다시 받아
        // 같은 화면으로 재이동한다. handlePushOpened의 removeExtra(TYPE)와 같은 역할이다.
        intent.removeExtra(FcmKeys.SCREEN)
        intent.removeExtra(FcmKeys.FEED_ID)
        intent.removeExtra(FcmKeys.NOTIFICATION_ID)
        setIntent(intent)

        val destination = pushDestinationOf(screen, feedId)
        if (BuildConfig.DEBUG) {
            Log.d("FCM", "handlePushNavigation - screen=$screen, feedId=$feedId, destination=$destination")
        }

        when (destination) {
            // 딥링크는 feedId가 있어야 성립한다. pushDestinationOf가 이를 보장한다.
            PushDestination.FEED_DETAIL ->
                feedId?.let {
                    pendingFeedDeepLink.value = PendingFeedDeepLink(feedId = it, notificationId = notificationId)
                }

            PushDestination.HOME, PushDestination.FEED_CREATE -> pendingPushDestination.value = destination

            // 알 수 없는 screen이거나 이동할 대상이 없다. 앱만 열린다 — 크래시 금지.
            null -> Unit
        }
    }
}

/** 서버가 Long/String 중 무엇으로 보내도 읽히도록 두 형태를 모두 시도한다. */
private fun Intent.longExtraOrNull(key: String): Long? =
    getStringExtra(key)?.toLongOrNull()
        ?: getLongExtra(key, -1L).takeIf { it != -1L }

data class PendingFeedDeepLink(
    val feedId: Long,
    val notificationId: Long?,
)
