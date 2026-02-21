package com.sseotdabwa.buyornot.feature.notification.ui

import android.Manifest
import android.app.Activity
import android.os.Build
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotChip
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotEmptyView
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotImgs
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.permission.hasNotificationPermission
import com.sseotdabwa.buyornot.core.ui.permission.openAppSettings
import com.sseotdabwa.buyornot.core.ui.permission.rememberNotificationPermission
import com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationIntent
import com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationSideEffect

@Composable
fun NotificationScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (String) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 권한 상태 즉시 초기화 (깜빡임 방지)
    LaunchedEffect(Unit) {
        val hasPermission = context.hasNotificationPermission()
        viewModel.initializePermissionState(hasPermission)
    }

    // 알림 권한 요청
    val (_, requestPermission) =
        rememberNotificationPermission { granted ->
            if (granted) {
                viewModel.handleIntent(NotificationIntent.OnPermissionGranted)
            } else {
                viewModel.handleIntent(NotificationIntent.OnPermissionDenied)
            }
        }

    // SideEffect 처리
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is NotificationSideEffect.RequestNotificationPermission -> {
                    requestPermission()
                }
                is NotificationSideEffect.OpenAppSettings -> {
                    context.openAppSettings()
                }
            }
        }
    }

    // 앱이 다시 포그라운드로 올 때 권한 상태 재확인
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    val hasPermission = context.hasNotificationPermission()
                    viewModel.updatePermissionState(hasPermission)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
            // 1. 필터 칩 영역
            item {
                Spacer(modifier = Modifier.height(20.dp))
                NotificationFilterRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { filter ->
                        viewModel.handleIntent(NotificationIntent.OnFilterSelected(filter))
                    },
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. 알림 설정 가이드 배너 (권한 없을 때만 표시)
            if (!uiState.hasNotificationPermission) {
                item {
                    NotificationGuideBanner(
                        onActionClick = {
                            // Android 13 미만: 설정으로 이동
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                context.openAppSettings()
                                return@NotificationGuideBanner
                            }

                            val activity = context as? Activity
                            if (activity == null) {
                                context.openAppSettings()
                                return@NotificationGuideBanner
                            }

                            val shouldShowRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    activity,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                )

                            // ViewModel에서 권한 요청 이력(DataStore)를 기반으로 판단
                            viewModel.handleBannerClick(
                                shouldShowRationale = shouldShowRationale,
                            )
                        },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            if (uiState.notifications.isEmpty()) {
                item {
                    Column {
                        Spacer(
                            modifier =
                                Modifier.height(
                                    if (!uiState.hasNotificationPermission) {
                                        140.dp
                                    } else {
                                        120.dp
                                    },
                                ),
                        )
                        NotificationEmptyView()
                    }
                }
            } else {
                // 3. 알림 리스트 아이템
                items(uiState.notifications) { notification ->
                    NotificationItem(
                        state =
                            NotificationState(
                                id = notification.id,
                                imageUrl = notification.imageUrl,
                                label = notification.title,
                                message = notification.description,
                                time = notification.time,
                                isRead = notification.isRead,
                            ),
                        onClick = { onNotificationClick(notification.id) },
                    )
                }
            }

            // 4. 리스트 푸터
            if (uiState.notifications.isNotEmpty()) {
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
}

/**
 * 알림 필터 행 컴포넌트
 */
@Composable
private fun NotificationFilterRow(
    selectedFilter: com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationFilter,
    onFilterSelected: (com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationFilter) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(com.sseotdabwa.buyornot.feature.notification.viewmodel.NotificationFilter.entries) { filter ->
            BuyOrNotChip(
                text = filter.label,
                isSelected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
            )
        }
    }
}

@Composable
private fun NotificationEmptyView(modifier: Modifier = Modifier) {
    BuyOrNotEmptyView(
        modifier = modifier,
        title = "새로운 알림이 없어요",
        description = "투표에 참여하고 소식을 받아보세요!",
        image = BuyOrNotImgs.NoNotification.resId,
    )
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
