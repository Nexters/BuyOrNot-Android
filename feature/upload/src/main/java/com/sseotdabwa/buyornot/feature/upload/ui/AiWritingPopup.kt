package com.sseotdabwa.buyornot.feature.upload.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.R
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

// 업로드는 짧아 첫 칸까지만 채우고, 초안 응답을 기다리는 동안 끝 직전까지 천천히 채운다.
// 응답 시간을 알 수 없어 끝까지는 채우지 않고, [DRAFTING_LABEL_PROGRESS]를 넘기면 문구를 "작성중"으로 바꾼다.
private const val UPLOADING_PROGRESS = 0.11f
private const val DRAFTING_MAX_PROGRESS = 0.94f
private const val DRAFTING_LABEL_PROGRESS = 0.55f
private const val DRAFTING_DURATION_MILLIS = 15_000

@Composable
internal fun AiWritingPopup(
    aiWriting: AiWriting,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(aiWriting.phase) {
        when (aiWriting.phase) {
            AiWritingPhase.UPLOADING -> progress.animateTo(UPLOADING_PROGRESS)
            AiWritingPhase.DRAFTING ->
                progress.animateTo(
                    targetValue = DRAFTING_MAX_PROGRESS,
                    animationSpec = tween(durationMillis = DRAFTING_DURATION_MILLIS, easing = LinearOutSlowInEasing),
                )
        }
    }
    val label =
        if (progress.value < DRAFTING_LABEL_PROGRESS) "사진 ${aiWriting.imageCount}장 분석중.." else "투표 글 작성중.."

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(BuyOrNotTheme.colors.black.copy(alpha = 0.6f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .size(256.dp)
                    .background(BuyOrNotTheme.colors.gray0, RoundedCornerShape(20.dp))
                    .padding(start = 28.dp, top = 28.dp, end = 28.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.img_tobong_loading),
                contentDescription = null,
                modifier = Modifier.weight(1f),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = label,
                    modifier = Modifier.fillMaxWidth(),
                    style = BuyOrNotTheme.typography.titleT4Bold,
                    color = BuyOrNotTheme.colors.gray950,
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(BuyOrNotTheme.colors.gray300, CircleShape),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth(progress.value)
                                .fillMaxHeight()
                                .background(BuyOrNotTheme.colors.gray950, CircleShape),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AiWritingPopupPreview() {
    BuyOrNotTheme {
        AiWritingPopup(aiWriting = AiWriting(imageCount = 3, phase = AiWritingPhase.UPLOADING))
    }
}
