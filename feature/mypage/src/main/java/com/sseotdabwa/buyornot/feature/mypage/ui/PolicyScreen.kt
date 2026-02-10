package com.sseotdabwa.buyornot.feature.mypage.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.feature.mypage.component.SettingItem

@Composable
fun PolicyRoute(
    onBackClick: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
) {
    PolicyScreen(
        onBackClick = onBackClick,
        onServiceTermClick = onNavigateToTerms,
        onPrivacyPolicyClick = onNavigateToPrivacyPolicy,
    )
}

@Composable
fun PolicyScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onServiceTermClick: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackTopBarWithTitle(
            title = "약관 및 정책",
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingItem("개인정보처리방침") { onPrivacyPolicyClick() }
            SettingItem("서비스 약관") { onServiceTermClick() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PolicyScreenPreview() {
    BuyOrNotTheme {
        Scaffold(
            containerColor = BuyOrNotTheme.colors.gray0,
        ) { paddingValues ->
            PolicyScreen(
                modifier = Modifier.padding(paddingValues),
                onBackClick = {},
                onPrivacyPolicyClick = {},
                onServiceTermClick = {},
            )
        }
    }
}
