package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBar
import com.sseotdabwa.buyornot.core.designsystem.components.FeedCard
import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@Composable
fun NotificationDetailScreen(
    notificationId: String,
    onBackClick: () -> Unit,
) {
    // TODO: ViewModel을 통해 notificationId에 해당하는 투표 상세 데이터를 불러와야 함

    Scaffold(
        topBar = {
            BackTopBar(onBackClick = onBackClick)
        },
        containerColor = BuyOrNotTheme.colors.gray0,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(4.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            // 상세 페이지의 메인 콘텐츠 (FeedCard와 동일한 구성)
            FeedCard(
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

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
