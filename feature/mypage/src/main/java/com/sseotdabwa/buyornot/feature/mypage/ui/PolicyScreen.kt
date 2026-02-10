package com.sseotdabwa.buyornot.feature.mypage.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBar
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@Composable
fun PolicyRoute(onBackClick: () -> Unit) {
    PolicyScreen(onBackClick = onBackClick)
}

@Composable
fun PolicyScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackTopBar(onBackClick = onBackClick)

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Policy Screen")
            }
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
            )
        }
    }
}
