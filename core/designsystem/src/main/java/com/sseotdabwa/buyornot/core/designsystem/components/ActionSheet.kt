package com.sseotdabwa.buyornot.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme

/**
 * BottomSheet 액션 아이템 하나를 나타내는 데이터 클래스.
 *
 * @param icon 아이템 왼쪽에 표시할 아이콘
 * @param text 아이템에 표시할 텍스트
 * @param onClick 아이템 클릭 시 실행할 동작
 */
data class ActionItem(
    val icon: IconResource,
    val text: String,
    val onClick: () -> Unit,
)

/**
 * 아이콘과 텍스트로 구성된 액션 목록을 BottomSheet로 표시하는 컴포넌트.
 *
 * 각 아이템을 탭하면 해당 액션을 실행하고 시트를 닫는다.
 * 선택 상태 없이 액션 실행에만 특화되어 있으며, title과 오버플로우 그라데이션은 없다.
 *
 * @param actions 표시할 액션 아이템 목록
 * @param onDismissRequest 시트를 닫을 때 호출되는 콜백
 * @param sheetShape 시트의 모양 (기본값: 상단 모서리 26.dp 라운드)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionSheet(
    actions: List<ActionItem>,
    onDismissRequest: () -> Unit,
    sheetShape: Shape = RoundedCornerShape(26.dp),
) {
    BuyOrNotBottomSheet(
        onDismissRequest = onDismissRequest,
        isHalfExpandedOnly = true,
        sheetShape = sheetShape,
    ) { hideSheet ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            items(
                count = actions.size,
                key = { it },
            ) { index ->
                val action = actions[index]
                ActionItemRow(
                    item = action,
                    onClick = {
                        action.onClick()
                        hideSheet()
                    },
                )
            }
        }
    }
}

@Composable
private fun ActionItemRow(
    item: ActionItem,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            imageVector = item.icon.asImageVector(),
            contentDescription = null,
            tint = BuyOrNotTheme.colors.gray950,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = item.text,
            style = BuyOrNotTheme.typography.subTitleS3SemiBold,
            color = BuyOrNotTheme.colors.gray950,
        )
    }
}

@Preview(showBackground = true, name = "ActionSheet Preview")
@Composable
private fun ActionSheetPreview() {
    BuyOrNotTheme {
        ActionSheet(
            actions =
                listOf(
                    ActionItem(
                        icon = BuyOrNotIcons.Camera,
                        text = "카메라로 직접 찍기",
                        onClick = {},
                    ),
                    ActionItem(
                        icon = BuyOrNotIcons.Gallery,
                        text = "앨범에서 사진 선택",
                        onClick = {},
                    ),
                ),
            onDismissRequest = {},
        )
    }
}
