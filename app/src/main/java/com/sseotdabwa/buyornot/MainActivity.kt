package com.sseotdabwa.buyornot

import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import com.sseotdabwa.buyornot.core.common.deeplink.NavigationDestination
import com.sseotdabwa.buyornot.core.common.deeplink.PendingNavigation
import com.sseotdabwa.buyornot.core.common.deeplink.PendingNavigationStore
import com.sseotdabwa.buyornot.core.common.deeplink.feedIdFromAppLink
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.feature.auth.navigation.SplashRoute
import com.sseotdabwa.buyornot.notification.FcmKeys
import com.sseotdabwa.buyornot.notification.pushDestinationOf
import com.sseotdabwa.buyornot.performance.ScreenPerformanceTracker
import com.sseotdabwa.buyornot.performance.screenTraceNameOf
import com.sseotdabwa.buyornot.ui.BuyOrNotApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.serializer
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

    // Activity 필드로 두면 인증 대기 중 재생성될 때 목적지가 사라진다 —
    // 진입 Intent는 이미 소비된 상태다. 자세한 근거는 [PendingNavigationStore] 주석 참고.
    @Inject
    lateinit var pendingNavigationStore: PendingNavigationStore

    private var jankStats: JankStats? = null
    private var metricsStateHolder: PerformanceMetricsState.Holder? = null
    private lateinit var screenPerformanceTracker: ScreenPerformanceTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 딥링크 처리가 feedId extra를 소비하므로 로깅을 먼저 수행한다.
        handlePushOpened(intent)
        handleNavigationIntent(intent)
        screenPerformanceTracker =
            ScreenPerformanceTracker(
                performance = performance,
                // 스플래시가 그려진 것은 "앱을 쓸 수 있는 상태"가 아니므로 TTFD 판정에서 제외한다.
                //
                // 런타임 route와 맞추려면 serializer의 serialName을 써야 한다. `::class.qualifiedName`은
                // R8이 난독화한 이름이라 release에서 매칭에 실패하고, 스플래시가 의미 있는 첫 화면으로 집계된다.
                nonMeaningfulScreens = setOfNotNull(screenTraceNameOf(serializer<SplashRoute>().descriptor.serialName)),
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
            val pendingNavigation by pendingNavigationStore.pending.collectAsStateWithLifecycle()
            BuyOrNotTheme {
                BuyOrNotApp(
                    authEventBus = authEventBus,
                    screenPerformanceTracker = screenPerformanceTracker,
                    pendingNavigation = pendingNavigation,
                    onPendingNavigationConsumed = { pendingNavigationStore.consume() },
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
        handleNavigationIntent(intent)
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
     * 외부 유입 Intent에서 이동 대상을 정해 pending 상태에 넣는다.
     *
     * 앱 링크(Uri)를 FCM extras보다 먼저 본다. 한 Intent가 양쪽을 동시에 만족하는 일은 없어야 하고,
     * Uri 경로에서는 PushOpened를 절대 발행하지 않는다 (푸시 지표 오염 방지).
     *
     * FCM 경로의 목적지 판정 규칙은 [pushDestinationOf]에 있다 — 여기서는 Intent를 분해하고
     * 소비하는 일만 한다.
     */
    private fun handleNavigationIntent(intent: Intent?) {
        if (intent == null) return
        if (handleAppLink(intent)) return

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
            Log.d("FCM", "handleNavigationIntent - screen=$screen, feedId=$feedId, destination=$destination")
        }

        // 알 수 없는 screen이거나 이동할 대상이 없으면 앱만 열린다 — 크래시 금지.
        if (destination == null) return

        pendingNavigationStore.set(
            PendingNavigation(
                destination = destination,
                feedId = feedId,
                notificationId = notificationId,
            ),
        )
    }

    /**
     * `https://{host}/feed/{feedId}` 앱 링크를 처리한다.
     *
     * @return 앱 링크 Intent였으면 true (파싱 성공 여부와 무관). FCM extras 경로를 건너뛰는 신호다.
     */
    private fun handleAppLink(intent: Intent): Boolean {
        val uri = intent.data ?: return false

        // 소비 마커. 이게 없으면 회전·프로세스 재생성 때 onCreate가 같은 Intent를 다시 받아
        // 이벤트가 재발행되고 피드 상세로도 재이동한다. extras 경로의 removeExtra와 같은 역할이다.
        intent.data = null
        setIntent(intent)

        val feedId = feedIdFromAppLink(uri.host, uri.pathSegments, BuildConfig.APP_LINK_HOST)
        if (BuildConfig.DEBUG) {
            Log.d("AppLink", "handleAppLink - uri=$uri, resolved feedId=$feedId, referrer=$referrer")
        }

        // 파싱에 실패해도 이벤트는 반드시 발행한다.
        // 건너뛰면 «링크가 안 온 것»과 «와서 깨진 것»을 구분할 수 없다.
        analytics.track(
            AnalyticsEvent.AppLinkOpened(
                linkStatus =
                    if (feedId != null) {
                        AnalyticsEvent.AppLinkOpened.STATUS_RESOLVED
                    } else {
                        AnalyticsEvent.AppLinkOpened.STATUS_INVALID
                    },
                feedId = feedId,
                // 발신 앱이 넣어줄 때만 존재한다. 없으면 «referrer 없음»이 하나의 유입 버킷이 된다.
                referrer = referrer?.host,
                utmSource = uri.queryParameterOrNull("utm_source"),
                utmMedium = uri.queryParameterOrNull("utm_medium"),
                utmCampaign = uri.queryParameterOrNull("utm_campaign"),
            ),
        )

        // 파싱 실패 시 pending을 설정하지 않는다 → 앱만 열리고 홈으로. 크래시 금지.
        // (NotificationDetailViewModel이 feedId를 checkNotNull 하므로 잘못된 값을 넘기면 죽는다.)
        if (feedId != null) {
            pendingNavigationStore.set(
                PendingNavigation(destination = NavigationDestination.FEED_DETAIL, feedId = feedId),
            )
        }
        return true
    }
}

/** 서버가 Long/String 중 무엇으로 보내도 읽히도록 두 형태를 모두 시도한다. */
private fun Intent.longExtraOrNull(key: String): Long? =
    getStringExtra(key)?.toLongOrNull()
        ?: getLongExtra(key, -1L).takeIf { it != -1L }

/**
 * 값이 없거나 비어 있으면 null. Mixpanel에서 «속성 자체를 생략»하기 위한 정규화다.
 *
 * opaque URI(`mailto:` 등)에 [Uri.getQueryParameter]를 호출하면 예외가 나므로 감싼다.
 */
private fun Uri.queryParameterOrNull(key: String): String? = runCatching { getQueryParameter(key) }.getOrNull()?.takeIf { it.isNotBlank() }
