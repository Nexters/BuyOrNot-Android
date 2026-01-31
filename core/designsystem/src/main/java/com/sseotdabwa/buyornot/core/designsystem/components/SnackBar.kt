package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

private val SnackbarVerticalMargin = 10.dp
private val SnackbarHorizontalMargin = 20.dp
private val SnackbarMaxWidth = 800.dp

private val snackbarMutex = Mutex()

enum class SnackBarIconTint {
    Success,
}

class BuyOrNotSnackBarVisuals(
    override val message: String,
    val iconResource: IconResource? = null,
    val iconTint: SnackBarIconTint = SnackBarIconTint.Success,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

@Composable
fun BuyOrNotSnackBar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    val visuals = snackbarData.visuals as? BuyOrNotSnackBarVisuals
    val tint =
        when (visuals?.iconTint) {
            SnackBarIconTint.Success -> BuyOrNotTheme.colors.green200
            else -> LocalContentColor.current
        }
    val icon =
        visuals?.iconResource?.let {
            ImageVector.vectorResource(id = it.resId)
        }

    Surface(
        modifier =
            modifier
                .padding(horizontal = SnackbarHorizontalMargin, vertical = SnackbarVerticalMargin)
                .fillMaxWidth()
                .widthIn(max = SnackbarMaxWidth),
        color = BuyOrNotTheme.colors.gray900,
        contentColor = BuyOrNotTheme.colors.gray50,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 4.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = tint,
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = snackbarData.visuals.message,
                style = BuyOrNotTheme.typography.bodyB5Medium,
            )
        }
    }
}

@Composable
fun BuyOrNotSnackBarHost(hostState: SnackbarHostState) {
    AnimatedContent(
        targetState = hostState.currentSnackbarData,
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter,
        transitionSpec = {
            (slideInVertically { it } + fadeIn(tween(400)))
                .togetherWith(slideOutVertically(animationSpec = tween(400)) { it } + fadeOut(tween(400)))
                .using(SizeTransform(clip = false))
        },
        label = "BuyOrNotSnackBarAnimation",
    ) { data ->
        if (data != null) {
            BuyOrNotSnackBar(snackbarData = data)
        }
    }
}

/**
 * BuyOrNot 앱의 커스텀 스낵바를 표시합니다.
 *
 * 이 함수는 suspend 함수로, 스낵바가 사라질 때까지 대기합니다.
 * 동시에 여러 스낵바가 표시되지 않도록 Mutex를 사용하여 순차적으로 처리합니다.
 *
 * @param snackbarHostState 스낵바를 표시할 [SnackbarHostState]
 * @param message 스낵바에 표시할 메시지
 * @param iconResource 메시지 좌측에 표시할 아이콘 리소스 (null이면 아이콘 없음)
 * @param iconTint 아이콘의 색상 틴트 (기본값: [SnackBarIconTint.Success])
 * @param duration 스낵바 표시 시간 (기본값: [SnackbarDuration.Short] = 4초)
 * @return 스낵바가 어떻게 닫혔는지를 나타내는 [SnackbarResult]
 *
 * @sample
 * ```
 * val snackbarHostState = remember { SnackbarHostState() }
 * val scope = rememberCoroutineScope()
 *
 * scope.launch {
 *     showBuyOrNotSnackBar(
 *         snackbarHostState = snackbarHostState,
 *         message = "저장되었습니다.",
 *         iconResource = BuyOrNotIcons.Check,
 *     )
 * }
 * ```
 */
suspend fun showBuyOrNotSnackBar(
    snackbarHostState: SnackbarHostState,
    message: String,
    iconResource: IconResource? = null,
    iconTint: SnackBarIconTint = SnackBarIconTint.Success,
    duration: SnackbarDuration = SnackbarDuration.Short,
): SnackbarResult =
    snackbarMutex.withLock {
        try {
            withTimeout(duration.toMillis()) {
                snackbarHostState.showSnackbar(
                    BuyOrNotSnackBarVisuals(
                        message = message,
                        iconResource = iconResource,
                        iconTint = iconTint,
                        duration = SnackbarDuration.Indefinite, // 직접 타이머 제어
                    ),
                )
            }
        } catch (_: TimeoutCancellationException) {
            SnackbarResult.Dismissed
        } finally {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

private fun SnackbarDuration.toMillis(): Long =
    when (this) {
        SnackbarDuration.Short -> 4000L
        SnackbarDuration.Long -> 10000L
        SnackbarDuration.Indefinite -> Long.MAX_VALUE
    }

@Preview(name = "Snackbar Demo Screen")
@Composable
private fun SnackbarDemoScreenPreview() {
    BuyOrNotTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        Scaffold(
            snackbarHost = {
                BuyOrNotSnackBarHost(snackbarHostState)
            },
        ) { paddingValues ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Button(onClick = {
                    scope.launch {
                        showBuyOrNotSnackBar(
                            snackbarHostState = snackbarHostState,
                            message = "스낵바입니다. 안내 메세지를 작성해주세요.",
                            iconResource = BuyOrNotIcons.Check,
                        )
                    }
                }) {
                    Text("스낵바 띄우기")
                }
            }
        }
    }
}

@Preview(name = "BuyOrNotSnackBar")
@Composable
private fun BuyOrNotSnackBarPreview() {
    BuyOrNotTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BuyOrNotSnackBar(
                snackbarData =
                    object : SnackbarData {
                        override val visuals =
                            BuyOrNotSnackBarVisuals(
                                message = "아이콘이 있는 스낵바입니다.",
                                iconResource = BuyOrNotIcons.Check,
                            )

                        override fun dismiss() {}

                        override fun performAction() {}
                    },
            )

            BuyOrNotSnackBar(
                snackbarData =
                    object : SnackbarData {
                        override val visuals =
                            BuyOrNotSnackBarVisuals(
                                message = "아이콘이 없는 스낵바입니다.",
                                iconResource = null,
                            )

                        override fun dismiss() {}

                        override fun performAction() {}
                    },
            )
        }
    }
}
