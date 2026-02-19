package com.sseotdabwa.buyornot.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotChip
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.permission.hasNotificationPermission
import com.sseotdabwa.buyornot.core.ui.permission.rememberNotificationPermission

/**
 * 알림 화면의 탭/필터 정의
 */
private enum class NotificationFilter(
    val label: String,
) {
    ALL("전체"),
    MY_VOTE("내가 올린 투표"),
    PARTICIPATED("참여한 투표"),
}

@Composable
fun NotificationScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // [State] MVI 패턴 적용 시 ViewModel에서 관리
    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }
    var hasNotificationPermission by remember { mutableStateOf(context.hasNotificationPermission()) }

    // 알림 권한 요청
    val (_, requestPermission) = rememberNotificationPermission { granted ->
        hasNotificationPermission = granted
    }

    // 앱이 다시 포그라운드로 올 때 권한 상태 재확인
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = context.hasNotificationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 더미 데이터 (디자인 가이드 반영)
    val notifications =
        remember {
            listOf(
                NotificationState("1", "https://picsum.photos/200", "투표 종료", "78% '애매하긴 해!'", "6시간 전", false),
                NotificationState("2", "https://picsum.photos/201", "투표 종료", "56% '사! 가즈아!'", "3일 전", true),
                NotificationState("3", "https://picsum.photos/202", "투표 종료", "90% '애매하긴 해!'", "6일 전", true),
                NotificationState("4", "https://picsum.photos/203", "투표 종료", "무승부! 2차전 가보자고!", "1주 전", true),
                NotificationState("5", "https://picsum.photos/204", "투표 종료", "결과를 확인해보세요", "2주 전", true),
                NotificationState("6", "https://picsum.photos/200", "투표 종료", "78% '애매하긴 해!'", "6시간 전", false),
                NotificationState("7", "https://picsum.photos/201", "투표 종료", "56% '사! 가즈아!'", "3일 전", true),
                NotificationState("8", "https://picsum.photos/202", "투표 종료", "90% '애매하긴 해!'", "6일 전", true),
                NotificationState("9", "https://picsum.photos/203", "투표 종료", "무승부! 2차전 가보자고!", "1주 전", true),
                NotificationState("10", "https://picsum.photos/204", "투표 종료", "결과를 확인해보세요", "2주 전", true),
            )
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BuyOrNotTheme.colors.gray0),
    ) {
        BackTopBarWithTitle(
            title = "알림",
            onBackClick = onBackClick,
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. 필터 칩 영역 (상단 여백 20px)
            item {
                NotificationFilterRow(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                )
                Spacer(modifier = Modifier.height(16.dp)) // 칩과 배너 사이 16px
            }

            // 2. 알림 설정 가이드 배너 (권한 없을 때만 표시)
            if (!hasNotificationPermission) {
                item {
                    NotificationGuideBanner(
                        onActionClick = {
                            requestPermission()
                        },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 3. 알림 리스트 아이템
            items(notifications) { notification ->
                NotificationItem(
                    state = notification,
                    onClick = { onNotificationClick(notification.id) },
                )
            }

            // 4. 리스트 푸터 (30일 전 알림 문구)
            item {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "30일 전 알림까지 보여줘요",
                        style = BuyOrNotTheme.typography.bodyB6Medium,
                        color = BuyOrNotTheme.colors.gray400,
                    )
                }
            }
        }
    }
}

/**
 * 알림 필터 행 컴포넌트
 */
@Composable
private fun NotificationFilterRow(
    selectedFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit,
) {
    LazyRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(NotificationFilter.entries) { filter ->
            BuyOrNotChip(
                text = filter.label,
                isSelected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationScreenPreview() {
    BuyOrNotTheme {
        NotificationScreen(
            onBackClick = {},
            onNotificationClick = {},
        )
    }
}
