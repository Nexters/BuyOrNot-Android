package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotDivider
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotDividerSize
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * 알림 아이템의 상태를 정의 데이터 클래스
 *
 * @param id: 알림의 고유 식별자
 * @param imageUrl: 알림의 이미지 URL
 * @param label: 알림의 라벨 (예: "투표 종료")
 * @param message: 알림의 메시지 (예: "78% '애매하긴 해!'")
 * @param time: 알림이 생성된 시간 (예: "6시간 전")
 * @param isRead: 알림이 읽었는지 여부 (안 읽음: false, 읽음: true)
 */
data class NotificationState(
    val id: String,
    val imageUrl: String,
    val label: String,
    val message: String,
    val time: String,
    val isRead: Boolean
)

/**
 * 알림 화면의 개별 아이템 컴포저블
 *
 * @param state: 알림 아이템의 상태를 나타내는 NotificationState 객체
 * @param onClick: 아이템을 클릭했을 때의 콜백 액션
 * @param modifier: 컴포저블에 적용할 Modifier
 */
@Composable
fun NotificationItem(
    state: NotificationState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 읽음 상태에 따른 배경색 결정
    val backgroundColor = if (state.isRead) BuyOrNotTheme.colors.gray100 else BuyOrNotTheme.colors.gray0

    Column (
        modifier = modifier
        .fillMaxWidth()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측 이미지
            AsyncImage(
                model = state.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            // 우측 텍스트 영역
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.label,
                        style = BuyOrNotTheme.typography.bodyB5Medium,
                        color = BuyOrNotTheme.colors.gray600
                    )
                    Text(
                        text = state.time,
                        style = BuyOrNotTheme.typography.bodyB6Medium,
                        color = BuyOrNotTheme.colors.gray600
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = state.message,
                    style = BuyOrNotTheme.typography.subTitleS3SemiBold,
                    color = BuyOrNotTheme.colors.gray900
                )
            }
        }

        //아이템 분리 디바이더 (small)
        BuyOrNotDivider(
            size = BuyOrNotDividerSize.Small
        )

    }

}

@Preview(showBackground = true, name = "안 읽은 알림")
@Composable
private fun UnreadNotiPreview() {
    BuyOrNotTheme {
        NotificationItem(
            state = NotificationState(
                id = "1",
                imageUrl = "https://picsum.photos/200",
                label = "투표 종료",
                message = "90% '애매하긴 해!'",
                time = "3일 전",
                isRead = false
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "읽은 알림")
@Composable
private fun ReadNotiPreview() {
    BuyOrNotTheme {
        NotificationItem(
            state = NotificationState(
                id = "2",
                imageUrl = "https://picsum.photos/200",
                label = "투표 종료",
                message = "56% '사! 가즈아!'",
                time = "3일 전",
                isRead = true
            ),
            onClick = {}
        )
    }
}
