package com.sseotdabwa.buyornot.feature.mypage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotConfirmDialog
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.domain.model.UserProfile
import com.sseotdabwa.buyornot.feature.mypage.components.SettingItem
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.AccountSettingIntent
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.AccountSettingSideEffect
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.AccountSettingUiState
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.AccountSettingViewModel

@Composable
fun AccountSettingRoute(
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToWithdrawal: () -> Unit,
    onNavigateToProfileEdit: () -> Unit,
    viewModel: AccountSettingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = LocalSnackbarState.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is AccountSettingSideEffect.NavigateToLogin -> onNavigateToLogin()
                is AccountSettingSideEffect.ShowSnackbar -> {
                    snackbarState.show(
                        message = sideEffect.message,
                        icon = sideEffect.icon,
                        iconTint = sideEffect.iconTint,
                    )
                }
            }
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        AccountSettingScreen(
            onBackClick = onBackClick,
            onLogoutClick = {
                viewModel.handleIntent(AccountSettingIntent.Logout(context))
            },
            onShowLogoutDialog = {
                viewModel.handleIntent(AccountSettingIntent.ShowLogoutDialog)
            },
            onDismissLogoutDialog = {
                viewModel.handleIntent(AccountSettingIntent.DismissLogoutDialog)
            },
            onNavigateToWithdrawal = onNavigateToWithdrawal,
            onProfileClick = onNavigateToProfileEdit,
            uiState = uiState,
        )
    }
}

@Composable
fun AccountSettingScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onShowLogoutDialog: () -> Unit,
    onDismissLogoutDialog: () -> Unit,
    onNavigateToWithdrawal: () -> Unit,
    onProfileClick: () -> Unit,
    uiState: AccountSettingUiState,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackTopBarWithTitle(
            title = "계정 설정",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileItem(
                nickname = uiState.userProfile?.nickname ?: "...",
                profileImage = uiState.userProfile?.profileImage,
                onClick = onProfileClick,
            )
            EmailItem(uiState.userProfile?.email ?: "...")
            SettingItem("로그아웃") { onShowLogoutDialog() }
            SettingItem(
                title = "회원 탈퇴",
                textColor = BuyOrNotTheme.colors.red100,
            ) {
                onNavigateToWithdrawal()
            }
        }
    }

    if (uiState.isLogoutDialogVisible) {
        BuyOrNotConfirmDialog(
            onDismissRequest = onDismissLogoutDialog,
            title = "로그아웃 하시겠어요?",
            confirmText = "유지하기",
            dismissText = "로그아웃",
            onConfirm = onDismissLogoutDialog,
            onDismiss = {
                onLogoutClick()
                onDismissLogoutDialog()
            },
        )
    }
}

/** 프로필 이미지와 닉네임. 누르면 프로필 설정으로 이동한다. */
@Composable
private fun ProfileItem(
    nickname: String,
    profileImage: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            modifier =
                Modifier
                    .size(42.dp)
                    .background(color = BuyOrNotTheme.colors.gray100, shape = CircleShape)
                    .border(width = 1.5.dp, color = BuyOrNotTheme.colors.gray300, shape = CircleShape)
                    .clip(CircleShape),
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(profileImage)
                    .crossfade(true)
                    .build(),
            contentDescription = "UserProfileImage",
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = nickname,
            modifier = Modifier.weight(1f),
            style = BuyOrNotTheme.typography.subTitleS1SemiBold,
            color = BuyOrNotTheme.colors.gray950,
        )

        Icon(
            imageVector = BuyOrNotIcons.ArrowRight.asImageVector(),
            contentDescription = null,
            tint = BuyOrNotTheme.colors.gray600,
        )
    }
}

@Composable
private fun EmailItem(email: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "이메일",
            style = BuyOrNotTheme.typography.paragraphP1Medium,
            color = BuyOrNotTheme.colors.gray950,
        )

        Text(
            text = email,
            style = BuyOrNotTheme.typography.paragraphP2Medium,
            color = BuyOrNotTheme.colors.gray600,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountSettingScreenPreview() {
    BuyOrNotTheme {
        Scaffold(
            containerColor = BuyOrNotTheme.colors.gray0,
        ) { paddingValues ->
            AccountSettingScreen(
                modifier = Modifier.padding(paddingValues),
                onBackClick = {},
                onLogoutClick = {},
                onShowLogoutDialog = {},
                onDismissLogoutDialog = {},
                onNavigateToWithdrawal = {},
                onProfileClick = {},
                uiState =
                    AccountSettingUiState(
                        userProfile =
                            UserProfile(
                                id = 0,
                                nickname = "서따봐",
                                profileImage = "",
                                socialAccount = "KAKAO",
                                email = "buyornot@gmail.com",
                            ),
                    ),
            )
        }
    }
}
