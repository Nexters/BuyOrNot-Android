package com.sseotdabwa.buyornot.feature.mypage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBar
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.domain.model.UserProfile
import com.sseotdabwa.buyornot.feature.mypage.components.SettingItem
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.MyPageSideEffect
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.MyPageUiState
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.MyPageViewModel

@Composable
fun MyPageRoute(
    versionName: String,
    onBackClick: () -> Unit,
    onAccountSettingClick: () -> Unit,
    onPolicyClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    viewModel: MyPageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = LocalSnackbarState.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is MyPageSideEffect.ShowSnackbar -> {
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
        MyPageScreen(
            versionName = versionName,
            onBackClick = onBackClick,
            onAccountSettingClick = onAccountSettingClick,
            onPolicyClick = onPolicyClick,
            onFeedbackClick = onFeedbackClick,
            uiState = uiState,
        )
    }
}

@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    versionName: String,
    uiState: MyPageUiState,
    onBackClick: () -> Unit = {},
    onAccountSettingClick: () -> Unit = {},
    onPolicyClick: () -> Unit = {},
    onFeedbackClick: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackTopBar(onBackClick = onBackClick)

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 10.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    modifier =
                        Modifier
                            .background(
                                color = BuyOrNotTheme.colors.gray500,
                                shape = CircleShape,
                            ).size(42.dp)
                            .clip(CircleShape),
                    model =
                        ImageRequest
                            .Builder(LocalContext.current)
                            .data(uiState.userProfile?.profileImage)
                            .crossfade(true)
                            .build(),
                    contentDescription = "UserProfileImage",
                    contentScale = ContentScale.Crop,
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = uiState.userProfile?.nickname ?: "...",
                    style = BuyOrNotTheme.typography.subTitleS1SemiBold,
                    color = BuyOrNotTheme.colors.gray900,
                )
            }

            HorizontalDivider(
                thickness = 2.dp,
                color = BuyOrNotTheme.colors.gray100,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SettingItem(title = "계정 설정") { onAccountSettingClick() }
                SettingItem(title = "약관 및 정책") { onPolicyClick() }
                SettingItem(title = "의견 남기기") { onFeedbackClick() }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "앱버전",
                    style = BuyOrNotTheme.typography.paragraphP4Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )
                Text(
                    text = "v $versionName",
                    style = BuyOrNotTheme.typography.paragraphP4Medium,
                    color = BuyOrNotTheme.colors.gray600,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    BuyOrNotTheme {
        Scaffold(
            containerColor = BuyOrNotTheme.colors.gray0,
        ) { paddingValues ->
            MyPageScreen(
                modifier = Modifier.padding(paddingValues),
                versionName = "1.0.0",
                onBackClick = {},
                onAccountSettingClick = {},
                onPolicyClick = {},
                onFeedbackClick = {},
                uiState =
                    MyPageUiState(
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
