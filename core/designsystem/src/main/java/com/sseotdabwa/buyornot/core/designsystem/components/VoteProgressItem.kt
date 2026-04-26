package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

private enum class VoteProgressSlot {
    ProgressBar,
    BaseText,
    InvertedText,
    MaxPercentageText,
}

private const val PROGRESS_ANIMATION_DURATION = 500

@Composable
fun VoteProgressItem(
    text: String,
    percentage: Float, // 0.0 ~ 1.0
    percentageText: String,
    modifier: Modifier = Modifier,
    progressBarColor: Color = BuyOrNotTheme.colors.gray400,
    textColor: Color = BuyOrNotTheme.colors.gray800,
    percentageTextColor: Color = BuyOrNotTheme.colors.gray950,
    invertedTextColor: Color = BuyOrNotTheme.colors.gray0,
    shouldInvertTextColor: Boolean = false,
    leadingContent: @Composable (() -> Unit)? = null,
    animationEnabled: Boolean = true,
) {
    val clampedPercentage = percentage.coerceIn(0f, 1f)

    // 0%부터 시작하여 원본 percentage까지 애니메이션
    val animatedPercentage = remember { Animatable(0f) }

    LaunchedEffect(clampedPercentage) {
        animatedPercentage.animateTo(
            targetValue = clampedPercentage,
            animationSpec =
                tween(
                    durationMillis = if (animationEnabled) PROGRESS_ANIMATION_DURATION else 0,
                ),
        )
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BuyOrNotTheme.colors.gray0)
                .border(
                    width = 1.dp,
                    color = BuyOrNotTheme.colors.gray300,
                    shape = RoundedCornerShape(12.dp),
                ),
    ) {
        SubcomposeLayout { constraints ->
            val totalWidth = constraints.maxWidth
            val totalHeight = constraints.maxHeight
            val progressWidth = (totalWidth * animatedPercentage.value).toInt()

            // 100% 텍스트 너비 측정 (고정 위치 계산용)
            val maxPercentageTextWidth =
                subcompose(VoteProgressSlot.MaxPercentageText) {
                    Text(
                        text = "100%",
                        style = BuyOrNotTheme.typography.subTitleS4SemiBold,
                    )
                }.first().measure(constraints).width

            // 1. 진행률 바 측정 및 배치
            val progressBarPlaceable =
                subcompose(VoteProgressSlot.ProgressBar) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .background(progressBarColor),
                    )
                }.first().measure(
                    constraints.copy(
                        minWidth = progressWidth,
                        maxWidth = progressWidth,
                    ),
                )

            // 2. 기본 텍스트 레이어 측정
            val baseTextPlaceable =
                subcompose(VoteProgressSlot.BaseText) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = text,
                                style = BuyOrNotTheme.typography.subTitleS4SemiBold,
                                color = textColor,
                            )
                        }
                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (leadingContent != null) {
                                leadingContent()
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            // percentageText를 고정 너비로 감싸서 leadingContent 위치 고정
                            Box(
                                modifier =
                                    Modifier.width(
                                        with(LocalDensity.current) {
                                            maxPercentageTextWidth.toDp()
                                        },
                                    ),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Text(
                                    text = percentageText,
                                    style = BuyOrNotTheme.typography.subTitleS4SemiBold,
                                    color = percentageTextColor,
                                )
                            }
                        }
                    }
                }.first().measure(constraints)

            // 3. 반전 텍스트 레이어 측정 (진행률 영역만큼 클리핑)
            val invertedTextPlaceable =
                if (shouldInvertTextColor && animatedPercentage.value > 0f) {
                    subcompose(VoteProgressSlot.InvertedText) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                                    .drawWithContent {
                                        // 진행률 영역만큼만 클리핑해서 그리기
                                        val clipRect =
                                            Path().apply {
                                                addRect(
                                                    Rect(
                                                        left = 0f,
                                                        top = 0f,
                                                        right = (progressWidth.toFloat() - 16.dp.toPx()).coerceAtLeast(0f), // padding 보정
                                                        bottom = size.height,
                                                    ),
                                                )
                                            }
                                        clipPath(clipRect) {
                                            this@drawWithContent.drawContent()
                                        }
                                    },
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = text,
                                    style = BuyOrNotTheme.typography.subTitleS4SemiBold,
                                    color = invertedTextColor,
                                )
                            }
                            Row(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (leadingContent != null) {
                                    leadingContent()
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                // percentageText를 고정 너비로 감싸서 leadingContent 위치 고정
                                Box(
                                    modifier =
                                        Modifier.width(
                                            with(LocalDensity.current) {
                                                maxPercentageTextWidth.toDp()
                                            },
                                        ),
                                    contentAlignment = Alignment.CenterEnd,
                                ) {
                                    Text(
                                        text = percentageText,
                                        style = BuyOrNotTheme.typography.subTitleS4SemiBold,
                                        color = invertedTextColor,
                                    )
                                }
                            }
                        }
                    }.first().measure(constraints)
                } else {
                    null
                }

            layout(totalWidth, totalHeight) {
                // 진행률 바 배치
                progressBarPlaceable.placeRelative(0, 0)
                // 기본 텍스트 배치
                baseTextPlaceable.placeRelative(0, 0)
                // 반전 텍스트 배치 (위에 덮기)
                invertedTextPlaceable?.placeRelative(0, 0)
            }
        }
    }
}

@Preview(name = "VoteProgressItem - 선택됨 (90%)", showBackground = true)
@Composable
private fun VoteProgressItemSelectedPreview() {
    BuyOrNotTheme {
        VoteProgressItem(
            text = "사! 가즈아!",
            percentage = 0.9f,
            percentageText = "90%",
            modifier = Modifier.padding(16.dp),
            progressBarColor = BuyOrNotTheme.colors.gray950,
            shouldInvertTextColor = true,
        )
    }
}

@Preview(name = "VoteProgressItem - 선택 안됨 (75%)", showBackground = true)
@Composable
private fun VoteProgressItemNotSelectedPreview() {
    BuyOrNotTheme {
        VoteProgressItem(
            text = "애매하긴 해...",
            percentage = 0.75f,
            percentageText = "75%",
            percentageTextColor = BuyOrNotTheme.colors.gray700,
            modifier = Modifier.padding(16.dp),
            textColor = BuyOrNotTheme.colors.gray700,
        )
    }
}

@Preview(name = "VoteProgressItem - 낮은 퍼센트 (10%)", showBackground = true)
@Composable
private fun VoteProgressItemLowPercentagePreview() {
    BuyOrNotTheme {
        VoteProgressItem(
            text = "사! 가즈아!",
            percentage = 0.1f,
            percentageText = "10%",
            modifier = Modifier.padding(16.dp),
            progressBarColor = BuyOrNotTheme.colors.gray950,
            shouldInvertTextColor = true,
        )
    }
}

@Preview(name = "VoteProgressItem - 높은 퍼센트 (100%)", showBackground = true)
@Composable
private fun VoteProgressItemNoInvertPreview() {
    BuyOrNotTheme {
        VoteProgressItem(
            text = "사! 가즈아!",
            percentage = 1f,
            percentageText = "100%",
            modifier = Modifier.padding(16.dp),
            progressBarColor = BuyOrNotTheme.colors.gray950,
            // ProfileImage 예시 (실제로는 ProfileImage Composable 사용)
            leadingContent = {
                Box(
                    modifier =
                        Modifier
                            .height(20.dp)
                            .width(20.dp)
                            .background(
                                color = BuyOrNotTheme.colors.gray500,
                                shape = RoundedCornerShape(10.dp),
                            ),
                )
            },
            shouldInvertTextColor = true,
        )
    }
}

@Preview(name = "VoteProgressItem - 전체 화면", showBackground = true)
@Composable
private fun VoteScreenPreview() {
    BuyOrNotTheme {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // 1번 항목: 선택됨, 90% 진행, leadingContent 포함
            VoteProgressItem(
                text = "사! 가즈아!",
                percentage = 0.9f,
                percentageText = "90%",
                progressBarColor = BuyOrNotTheme.colors.gray950,
                shouldInvertTextColor = true,
                leadingContent = {
                    // ProfileImage 예시 (실제로는 ProfileImage Composable 사용)
                    Box(
                        modifier =
                            Modifier
                                .height(20.dp)
                                .width(20.dp)
                                .background(
                                    color = BuyOrNotTheme.colors.gray500,
                                    shape = RoundedCornerShape(10.dp),
                                ),
                    )
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2번 항목: 선택 안됨, 10% 진행
            VoteProgressItem(
                text = "애매하긴 해...",
                percentage = 0.1f,
                percentageText = "10%",
                percentageTextColor = BuyOrNotTheme.colors.gray700,
                textColor = BuyOrNotTheme.colors.gray700,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.padding(start = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    "89명이 투표했어요.",
                    style = BuyOrNotTheme.typography.bodyB7Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )

                Text(
                    "·",
                    style = BuyOrNotTheme.typography.bodyB7Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )

                Text(
                    "진행중",
                    style = BuyOrNotTheme.typography.bodyB7Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )
            }
        }
    }
}

@Preview(name = "VoteProgressItem - 동률 0% (투표 없음)", showBackground = true)
@Composable
private fun VoteProgressItemTieZeroPreview() {
    BuyOrNotTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            VoteProgressItem(
                text = "사! 가즈아!",
                percentage = 0f,
                percentageText = "0%",
                progressBarColor = BuyOrNotTheme.colors.gray950,
                shouldInvertTextColor = true,
                textColor = BuyOrNotTheme.colors.gray700,
                percentageTextColor = BuyOrNotTheme.colors.gray700,
            )
            Spacer(modifier = Modifier.height(8.dp))
            VoteProgressItem(
                text = "애매하긴 해..",
                percentage = 0f,
                percentageText = "0%",
                textColor = BuyOrNotTheme.colors.gray700,
                percentageTextColor = BuyOrNotTheme.colors.gray700,
            )
        }
    }
}

@Preview(name = "VoteProgressItem - 동률 50% (투표 있음)", showBackground = true)
@Composable
private fun VoteProgressItemTieFiftyPreview() {
    BuyOrNotTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            VoteProgressItem(
                text = "사! 가즈아!",
                percentage = 0.5f,
                percentageText = "50%",
                progressBarColor = BuyOrNotTheme.colors.gray950,
                shouldInvertTextColor = true,
            )
            Spacer(modifier = Modifier.height(8.dp))
            VoteProgressItem(
                text = "애매하긴 해..",
                percentage = 0.5f,
                percentageText = "50%",
                progressBarColor = BuyOrNotTheme.colors.gray950,
                textColor = BuyOrNotTheme.colors.gray700,
                percentageTextColor = BuyOrNotTheme.colors.gray950,
                shouldInvertTextColor = true,
            )
        }
    }
}
