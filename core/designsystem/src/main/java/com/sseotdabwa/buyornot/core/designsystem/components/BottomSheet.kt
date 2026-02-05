package com.sseotdabwa.buyornot.core.designsystem.components

import android.R.attr.bottom
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyOrNotCustomBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier =
            Modifier
                .padding(horizontal = 14.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(26.dp),
        containerColor = BuyOrNotTheme.colors.gray0,
        tonalElevation = 8.dp,
        scrimColor = BuyOrNotTheme.colors.gray1000.copy(alpha = 0.5f),
        dragHandle = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 10.dp,
                        ),
            ) {
                Spacer(
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = Color(0xFFD9D9D9),
                                shape = RoundedCornerShape(18.dp),
                            ),
                )
            }
        },
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Interactive Custom Floating Sheet")
@Composable
private fun InteractiveBuyOrNotCustomSheetPreview() {
    BuyOrNotTheme {
        var showSheet by remember { mutableStateOf(false) }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PrimaryButton(
                text = "Show Floating Sheet",
                onClick = { showSheet = true },
            )
        }

        if (showSheet) {
            BuyOrNotCustomBottomSheet(
                onDismissRequest = { showSheet = false },
                content = {
                    Column(
                        modifier = Modifier.padding(18.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(50.dp)
                                    .background(
                                        color = BuyOrNotTheme.colors.gray300,
                                        shape = RoundedCornerShape(14.dp),
                                    ),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "제목",
                            style = BuyOrNotTheme.typography.subTitleS1SemiBold,
                            color = BuyOrNotTheme.colors.gray900,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "설명",
                            style = BuyOrNotTheme.typography.bodyB4Medium,
                            color = BuyOrNotTheme.colors.gray700,
                        )

                        Spacer(modifier = Modifier.height(26.dp))

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(BuyOrNotTheme.colors.gray300),
                        )

                        Spacer(modifier = Modifier.height(26.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            NeutralButton(
                                text = "아니요",
                                modifier = Modifier.weight(1f),
                            ) {
                                showSheet = false
                            }

                            PrimaryButton(
                                text = "예",
                                modifier = Modifier.weight(1f),
                            ) {
                                showSheet = false
                            }
                        }
                    }
                },
            )
        }
    }
}
