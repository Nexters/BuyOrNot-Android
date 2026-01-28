package com.sseotdabwa.buyornot.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
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

class BuyOrNotSnackBarVisuals(
    override val message: String,
    val icon: ImageVector? = null,
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
            visuals?.icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
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
                .togetherWith(fadeOut(tween(400)))
                .using(SizeTransform(clip = false))
        },
        label = "BuyOrNotSnackBarAnimation",
    ) { data ->
        if (data != null) {
            BuyOrNotSnackBar(snackbarData = data)
        }
    }
}

suspend fun showBuyOrNotSnackBar(
    snackbarHostState: SnackbarHostState,
    message: String,
    icon: ImageVector? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
): SnackbarResult =
    snackbarMutex.withLock {
        try {
            withTimeout(duration.toMillis()) {
                snackbarHostState.showSnackbar(
                    BuyOrNotSnackBarVisuals(
                        message = message,
                        icon = icon,
                        duration = SnackbarDuration.Indefinite, // 직접 타이머 제어
                    ),
                )
            }
        } catch (e: TimeoutCancellationException) {
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
        val icon = BuyOrNotIcons.Profile.asImageVector()

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
                            icon = icon,
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
                                icon = BuyOrNotIcons.Profile.asImageVector(),
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
                                icon = null,
                            )

                        override fun dismiss() {}

                        override fun performAction() {}
                    },
            )
        }
    }
}
