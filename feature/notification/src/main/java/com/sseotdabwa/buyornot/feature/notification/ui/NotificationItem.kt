package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@Composable
fun NotificationItem(
    id: Long,
    imageUrl: String,
    label: String,
    message: String,
    time: String,
    isRead: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isRead) BuyOrNotTheme.colors.gray50 else BuyOrNotTheme.colors.gray0

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable { onClick() }
                .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = BuyOrNotTheme.typography.bodyB5Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )
                Text(
                    text = time,
                    style = BuyOrNotTheme.typography.bodyB6Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                style = BuyOrNotTheme.typography.subTitleS3SemiBold,
                color = BuyOrNotTheme.colors.gray950,
            )
        }
    }
}

@Preview(showBackground = true, name = "안 읽은 알림")
@Composable
private fun UnreadNotiPreview() {
    BuyOrNotTheme {
        NotificationItem(
            id = 1L,
            imageUrl = "https://picsum.photos/200",
            label = "투표 종료",
            message = "90% '애매하긴 해!'",
            time = "3일 전",
            isRead = false,
            onClick = {},
        )
    }
}

@Preview(showBackground = true, name = "읽은 알림")
@Composable
private fun ReadNotiPreview() {
    BuyOrNotTheme {
        NotificationItem(
            id = 2L,
            imageUrl = "https://picsum.photos/200",
            label = "투표 종료",
            message = "56% '사! 가즈아!'",
            time = "3일 전",
            isRead = true,
            onClick = {},
        )
    }
}
