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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBar
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.feature.mypage.component.SettingItem

@Composable
fun MyPageRoute(
    versionName: String,
    onBackClick: () -> Unit,
    onAccountSettingClick: () -> Unit,
    onPolicyClick: () -> Unit,
) {
    MyPageScreen(
        versionName = versionName,
        onBackClick = onBackClick,
        onAccountSettingClick = onAccountSettingClick,
        onPolicyClick = onPolicyClick,
    )
}

@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    versionName: String,
    onBackClick: () -> Unit = {},
    onAccountSettingClick: () -> Unit = {},
    onPolicyClick: () -> Unit = {},
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
                Box(
                    modifier =
                        Modifier
                            .background(
                                color = BuyOrNotTheme.colors.gray500,
                                shape = CircleShape,
                            ).size(42.dp)
                            .clip(CircleShape),
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "참새방앗간12456",
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
                SettingItem(title = "의견 남기기") { /* TODO : 구글 폼 열기 */ }
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
            )
        }
    }
}
