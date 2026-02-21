package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBar
import com.sseotdabwa.buyornot.core.designsystem.components.FeedCard
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * 알림 상세 화면
 *
 * 사용자가 알림을 탭했을 때 해당 투표의 상세 내용을 표시하는 화면입니다.
 * 투표 종료 여부, 사용자의 투표 참여 이력, 투표 결과 등을 함께 보여줍니다.
 *
 * @param notificationId 조회할 알림의 고유 ID (서버 연동 시 사용)
 * @param onBackClick 뒤로 가기 버튼 클릭 시 호출되는 콜백
 */
@Composable
fun NotificationDetailScreen(
    notificationId: String,
    onBackClick: () -> Unit,
) {
    // TODO: ViewModel을 통해 notificationId에 해당하는 투표 상세 데이터를 불러와야 함

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BuyOrNotTheme.colors.gray0)
                .verticalScroll(rememberScrollState()),
    ) {
        BackTopBar(onBackClick = onBackClick)
        // 상세 페이지의 메인 콘텐츠 (FeedCard와 동일한 구성)
        FeedCard(
            modifier = Modifier.padding(20.dp),
            profileImageUrl = "https://picsum.photos/seed/user/200/200",
            nickname = "참새방앗간12456",
            category = "업무 · 공부 생산성",
            createdAt = "2일 전",
            content = "가나다라마바사아자차카타나다라마바사아자차카타나다라마바사아자차카타나다라마바사아자차카타나다라마바사아자차카타나다라마바사아자차카타나다라마바사아자차카타",
            productImageUrl = "https://picsum.photos/seed/product/800/1000",
            price = "31,900",
            imageAspectRatio = ImageAspectRatio.PORTRAIT,
            isVoteEnded = true, // 종료 상태
            userVotedOptionIndex = 0, // 내가 '사! 가즈아!'에 참여한 경우
            buyVoteCount = 80,
            maybeVoteCount = 20,
            totalVoteCount = 100, // 시안의 % 계산을 위한 임의 값
            onExpandClick = { /* 이미지 확대 */ },
            onVote = { /* 종료된 투표이므로 동작하지 않음 */ },
        )
    }
}

/**
 * 알림 상세 화면 Preview
 *
 * 투표가 종료되고 사용자가 이미 투표에 참여한 상태의 알림 상세 화면을 보여줍니다.
 */
@Preview(showBackground = true)
@Composable
private fun NotificationDetailScreenPreview() {
    BuyOrNotTheme {
        NotificationDetailScreen(
            notificationId = "123",
            onBackClick = {},
        )
    }
}
