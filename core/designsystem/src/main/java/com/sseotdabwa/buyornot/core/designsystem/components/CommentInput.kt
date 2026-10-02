package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.preview.PreviewImages
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

private const val COMMENT_INPUT_MAX_LINES = 5

/**
 * 피드 상세 하단에 고정되는 댓글 입력창.
 *
 * 입력이 길어지면 줄바꿈되며 높이가 늘어나고, [COMMENT_INPUT_MAX_LINES]줄부터는 내부 스크롤된다.
 *
 * @param votedOptionLabel 내가 투표한 선택지. null이 아니면 입력창 위에 "'…'에 투표했어요"를 보여준다.
 * @param enabled false면 입력 자체를 막고 [disabledPlaceholder]를 보여준다 (투표 전 등).
 * @param submitEnabled 등록 버튼 활성화 여부. 비어 있거나 금칙어로 거절된 뒤 수정 전이면 false.
 * @param maxLength 이 길이를 넘는 입력은 잘라낸다.
 */
@Composable
fun CommentInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
    votedOptionLabel: String? = null,
    enabled: Boolean = true,
    submitEnabled: Boolean = value.isNotBlank(),
    placeholder: String = "댓글을 남겨주세요!",
    disabledPlaceholder: String = placeholder,
    maxLength: Int = Int.MAX_VALUE,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(BuyOrNotTheme.colors.gray0),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalDivider(thickness = 1.dp, color = BuyOrNotTheme.colors.gray300)

        if (votedOptionLabel != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text =
                    buildAnnotatedString {
                        append("‘")
                        withStyle(SpanStyle(color = BuyOrNotTheme.colors.orange100)) { append(votedOptionLabel) }
                        append("’에 투표했어요")
                    },
                style = BuyOrNotTheme.typography.bodyB6Medium,
                color = BuyOrNotTheme.colors.gray800,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            CommentAvatar(
                profileImageUrl = profileImageUrl,
                size = 38,
                modifier = Modifier.padding(bottom = 2.dp),
            )

            BasicTextField(
                value = value,
                onValueChange = { onValueChange(it.take(maxLength)) },
                modifier =
                    Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                enabled = enabled,
                maxLines = COMMENT_INPUT_MAX_LINES,
                textStyle = BuyOrNotTheme.typography.bodyB4Medium.copy(color = BuyOrNotTheme.colors.gray900),
                cursorBrush = SolidColor(BuyOrNotTheme.colors.gray950),
                decorationBox = { innerTextField ->
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(BuyOrNotTheme.colors.gray200, RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = if (enabled) placeholder else disabledPlaceholder,
                                style = BuyOrNotTheme.typography.bodyB4Medium,
                                color = if (enabled) BuyOrNotTheme.colors.gray600 else BuyOrNotTheme.colors.gray500,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            val canSubmit = enabled && submitEnabled
            Box(
                modifier =
                    Modifier
                        .size(width = 46.dp, height = 42.dp)
                        .clip(CircleShape)
                        .background(if (canSubmit) BuyOrNotTheme.colors.gray950 else BuyOrNotTheme.colors.gray100)
                        .clickable(enabled = canSubmit, onClick = onSubmit),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = BuyOrNotIcons.ArrowUp.asImageVector(),
                    contentDescription = "댓글 등록",
                    modifier = Modifier.size(18.dp),
                    tint = if (canSubmit) BuyOrNotTheme.colors.gray0 else BuyOrNotTheme.colors.gray600,
                )
            }
        }
    }
}

@Preview(name = "CommentInput - 상태", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CommentInputPreview() {
    BuyOrNotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CommentInput(
                value = "",
                onValueChange = {},
                onSubmit = {},
                profileImageUrl = PreviewImages.avatar(),
                votedOptionLabel = "사! 가즈아!",
            )
            CommentInput(
                value = "댓글을 남겨주세요!댓글을 남겨주세",
                onValueChange = {},
                onSubmit = {},
                profileImageUrl = PreviewImages.avatar(),
                votedOptionLabel = "사! 가즈아!",
            )
            CommentInput(
                value = "",
                onValueChange = {},
                onSubmit = {},
                profileImageUrl = PreviewImages.avatar(),
                enabled = false,
                disabledPlaceholder = "투표 후 의견을 작성할 수 있어요!",
            )
        }
    }
}

@Preview(name = "CommentInput - 입력 중", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CommentInputTypingPreview() {
    BuyOrNotTheme {
        var text by remember { mutableStateOf("댓글을 남겨주세요!댓글을 남겨주세요!댓글을 남겨주세요!댓글을 남겨주세요!") }
        CommentInput(
            value = text,
            onValueChange = { text = it },
            onSubmit = {},
            profileImageUrl = PreviewImages.avatar(),
            votedOptionLabel = "애매하긴 해",
        )
    }
}
