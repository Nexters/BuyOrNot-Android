package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.components.NotificationDotBadge
import com.sseotdabwa.buyornot.core.designsystem.image.ImageModel
import com.sseotdabwa.buyornot.core.designsystem.image.toCoilModel
import com.sseotdabwa.buyornot.core.designsystem.preview.PreviewImages
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@Composable
fun NotificationItem(
    image: ImageModel,
    headline: String,
    message: String,
    time: String?,
    isRead: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(BuyOrNotTheme.colors.gray0)
                .clickable(onClick = onClick)
                .padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = image.toCoilModel(),
            contentDescription = null,
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = headline,
                    style = BuyOrNotTheme.typography.bodyB5Medium,
                    color = BuyOrNotTheme.colors.gray700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (time != null) {
                        Text(
                            text = time,
                            style = BuyOrNotTheme.typography.bodyB6Medium,
                            color = BuyOrNotTheme.colors.gray600,
                        )
                    }
                    if (!isRead) NotificationDotBadge()
                }
            }

            Text(
                text = message,
                style = BuyOrNotTheme.typography.subTitleS4SemiBold,
                color = BuyOrNotTheme.colors.gray950,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, name = "안 읽은 알림")
@Composable
private fun UnreadNotiPreview() {
    BuyOrNotTheme {
        NotificationItem(
            image = PreviewImages.square,
            headline = "록시땅 바디로션록시땅 바디로션록시땅 바디로션록시땅 바디로션",
            message = "‘사! 가즈아!’ 승!",
            time = "3일 전",
            isRead = false,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, name = "읽은 답글 알림")
@Composable
private fun ReadCommentNotiPreview() {
    BuyOrNotTheme {
        NotificationItem(
            image = PreviewImages.square,
            headline = "록시땅 바디로션",
            message = "‘아니 이거 향 너무 좋더라구요 저도 한번 구매를 해볼까 싶은데 어떠세요’",
            time = null,
            isRead = true,
            onClick = {},
        )
    }
}
