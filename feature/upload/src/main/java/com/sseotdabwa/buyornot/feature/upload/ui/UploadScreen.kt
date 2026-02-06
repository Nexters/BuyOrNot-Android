package com.sseotdabwa.buyornot.feature.upload.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBar
import com.sseotdabwa.buyornot.core.designsystem.components.ButtonSize
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotAlertDialog
import com.sseotdabwa.buyornot.core.designsystem.components.CapsuleButton
import com.sseotdabwa.buyornot.core.designsystem.components.OptionSheet
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.shape.BubbleShape
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import java.text.DecimalFormat

@Composable
fun UploadScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
) {
    var priceRaw by remember { mutableStateOf("") }
    var priceFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var content by remember { mutableStateOf("") }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val categories =
        remember {
            listOf(
                "명품 ∙ 프리미엄",
                "패션 ∙ 잡화",
                "화장품 ∙ 뷰티",
                "트렌드 ∙ 가성비템",
                "음식",
                "전자기기",
                "여행 쇼핑템",
                "헬스 ∙ 운동용품",
                "도서",
                "기타",
            )
        }
    val decimalFormat = remember { DecimalFormat("#,###") }
    val scrollState = rememberScrollState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            selectedImageUri = uri
        }

    val isSubmitEnabled = selectedCategory != null && priceRaw.isNotEmpty() && selectedImageUri != null

    BackHandler {
        if (!showExitDialog) showExitDialog = true
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(BuyOrNotTheme.colors.gray0)
                .imePadding()
                .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        BackTopBar {
            showExitDialog = true
        }

        Column(
            modifier =
                Modifier
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .weight(1f),
        ) {
            CategorySelectorRow(
                selectedCategory = selectedCategory,
                onCategoryClick = { showCategorySheet = true },
            )

            HorizontalDivider(
                thickness = 2.dp,
                color = BuyOrNotTheme.colors.gray100,
            )

            PriceInputField(
                modifier = Modifier.padding(vertical = 18.dp),
                priceFieldValue = priceFieldValue,
                priceRaw = priceRaw,
                decimalFormat = decimalFormat,
                onPriceChange = { digits, textFieldValue ->
                    priceRaw = digits
                    priceFieldValue = textFieldValue
                },
            )

            HorizontalDivider(
                thickness = 2.dp,
                color = BuyOrNotTheme.colors.gray100,
            )

            Spacer(modifier = Modifier.height(20.dp))

            ContentInputField(
                content = content,
                onContentChange = { content = it },
            )

            Spacer(modifier = Modifier.height(10.dp))

            ImagePickerRow(
                selectedImageUri = selectedImageUri,
                onPickImage = { galleryLauncher.launch("image/*") },
                onRemoveImage = { selectedImageUri = null },
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSubmitEnabled) {
                ToolTip()
                Spacer(modifier = Modifier.width(6.dp))
            }

            CapsuleButton(
                text = "투표 게시!",
                enabled = isSubmitEnabled,
                size = ButtonSize.Small,
            ) {
            }
        }
    }

    if (showCategorySheet) {
        OptionSheet(
            title = "카테고리 선택",
            options = categories,
            selectedOption = selectedCategory,
            onOptionClick = {
                selectedCategory = it
                showCategorySheet = false
            },
            onDismissRequest = {
                showCategorySheet = false
            },
        )
    }

    if (showExitDialog) {
        BuyOrNotAlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = "다음에 등록할까요?",
            subText = "지금까지 쓴 내용은 저장되지 않아요.",
            confirmText = "유지하기",
            dismissText = "나가기",
            onConfirm = {
                showExitDialog = false
            },
            onDismiss = {
                showExitDialog = false
                onNavigateBack()
            },
        )
    }
}

@Composable
private fun CategorySelectorRow(
    selectedCategory: String?,
    onCategoryClick: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "투표 등록",
            style = BuyOrNotTheme.typography.subTitleS3SemiBold,
            color = BuyOrNotTheme.colors.gray800,
        )
        Icon(
            imageVector = BuyOrNotIcons.ArrowRight.asImageVector(),
            contentDescription = "Arrow Right",
            modifier = Modifier.size(14.dp),
            tint = BuyOrNotTheme.colors.gray600,
        )
        Text(
            text = selectedCategory ?: "카테고리 추가",
            modifier = Modifier.clickable { onCategoryClick() },
            style = BuyOrNotTheme.typography.subTitleS3SemiBold,
            color = if (selectedCategory != null) BuyOrNotTheme.colors.gray800 else BuyOrNotTheme.colors.gray600,
        )
    }
}

@Composable
private fun PriceInputField(
    modifier: Modifier = Modifier,
    priceFieldValue: TextFieldValue,
    priceRaw: String,
    decimalFormat: DecimalFormat,
    onPriceChange: (digits: String, textFieldValue: TextFieldValue) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = BuyOrNotIcons.Won.asImageVector(),
            contentDescription = "Won",
            modifier = Modifier.size(18.dp),
            tint = BuyOrNotTheme.colors.gray600,
        )
        Spacer(modifier = Modifier.width(6.dp))

        BasicTextField(
            value = priceFieldValue,
            onValueChange = { newValue ->
                val newDigits = newValue.text.filter { it.isDigit() }

                if (newDigits.length <= 10) {
                    val formattedText =
                        if (newDigits.isEmpty()) {
                            ""
                        } else {
                            decimalFormat.format(newDigits.toLongOrNull() ?: 0)
                        }

                    val cursorPos = newValue.selection.end
                    val digitsBeforeCursor = newValue.text.take(cursorPos).count { it.isDigit() }

                    var digitCount = 0
                    var newCursorPos = 0
                    for (i in formattedText.indices) {
                        if (formattedText[i].isDigit()) {
                            digitCount++
                        }
                        if (digitCount == digitsBeforeCursor) {
                            newCursorPos = i + 1
                            break
                        }
                    }
                    if (digitsBeforeCursor == 0) {
                        newCursorPos = 0
                    }

                    onPriceChange(
                        newDigits,
                        TextFieldValue(
                            text = formattedText,
                            selection = TextRange(newCursorPos),
                        ),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle =
                BuyOrNotTheme.typography.subTitleS3SemiBold.copy(
                    color = BuyOrNotTheme.colors.gray800,
                ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { innerTextField ->
                if (priceRaw.isEmpty()) {
                    Text(
                        text = "상품 가격을 입력해주세요",
                        style = BuyOrNotTheme.typography.subTitleS3SemiBold,
                        color = BuyOrNotTheme.colors.gray600,
                    )
                }
                innerTextField()
            },
        )
    }
}

@Composable
private fun ContentInputField(
    content: String,
    onContentChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        BasicTextField(
            value = content,
            onValueChange = { if (it.length <= 100) onContentChange(it) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp),
            textStyle =
                BuyOrNotTheme.typography.paragraphP2Medium.copy(
                    color = BuyOrNotTheme.colors.gray900,
                ),
            decorationBox = { innerTextField ->
                if (content.isEmpty()) {
                    Text(
                        text = "고민 이유를 자세히 적을수록 더 정확한 투표 결과를 얻을 수 있어요!",
                        style = BuyOrNotTheme.typography.paragraphP2Medium,
                        color = BuyOrNotTheme.colors.gray600,
                    )
                }
                innerTextField()
            },
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "${content.length}/100",
            modifier = Modifier.align(Alignment.End),
            style = BuyOrNotTheme.typography.captionC3Medium,
            color = BuyOrNotTheme.colors.gray600,
        )
    }
}

@Composable
private fun ImagePickerRow(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CameraButton(
            selectedCount = if (selectedImageUri != null) 1 else 0,
            onClick = onPickImage,
        )

        selectedImageUri?.let {
            SelectedImagePreview(
                imageUri = it,
                onRemove = onRemoveImage,
            )
        }
    }
}

@Composable
private fun CameraButton(
    selectedCount: Int,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .size(68.dp)
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = BuyOrNotTheme.colors.gray100,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(
                    space = 2.dp,
                    alignment = Alignment.CenterVertically,
                ),
        ) {
            Icon(
                imageVector = BuyOrNotIcons.Camera.asImageVector(),
                contentDescription = "Camera",
                modifier = Modifier.size(20.dp),
                tint = BuyOrNotTheme.colors.gray600,
            )
            Text(
                text =
                    buildAnnotatedString {
                        withStyle(
                            style =
                                SpanStyle(
                                    color =
                                        if (selectedCount > 0) {
                                            BuyOrNotTheme.colors.gray800
                                        } else {
                                            BuyOrNotTheme.colors.gray600
                                        },
                                ),
                        ) {
                            append("$selectedCount")
                        }
                        withStyle(
                            style = SpanStyle(color = BuyOrNotTheme.colors.gray600),
                        ) {
                            append("/1")
                        }
                    },
                style = BuyOrNotTheme.typography.subTitleS5SemiBold,
            )
        }
    }
}

@Composable
private fun SelectedImagePreview(
    imageUri: Uri,
    onRemove: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.TopEnd,
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Selected Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier =
                Modifier
                    .padding(
                        top = 4.dp,
                        end = 4.dp,
                    ).size(20.dp)
                    .background(
                        color =
                            BuyOrNotTheme.colors.black.copy(
                                alpha = 0.4f,
                            ),
                        shape = CircleShape,
                    ).clip(CircleShape)
                    .clickable { onRemove() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BuyOrNotIcons.Close.asImageVector(),
                contentDescription = "Close",
                modifier = Modifier.size(10.dp),
                tint = BuyOrNotTheme.colors.gray0,
            )
        }
    }
}

@Composable
private fun ToolTip(modifier: Modifier = Modifier) {
    val tooltipShape =
        remember {
            BubbleShape(
                cornerRadius = 10.dp,
                arrowWidth = 5.dp,
                arrowHeight = 10.dp,
            )
        }

    Row(
        modifier =
            modifier
                .customShadow(shape = tooltipShape)
                .background(
                    color = BuyOrNotTheme.colors.gray0,
                    shape = tooltipShape,
                ).padding(vertical = 10.dp)
                .padding(start = 12.dp, end = 17.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = BuyOrNotIcons.Clock.asImageVector(),
            contentDescription = "Clock",
            modifier = Modifier.size(16.dp),
            tint = BuyOrNotTheme.colors.gray700,
        )

        Text(
            text = "투표는 48시간동안 진행돼요.",
            style = BuyOrNotTheme.typography.paragraphP4Medium,
            color = BuyOrNotTheme.colors.gray700,
        )
    }
}

fun Modifier.customShadow(
    shape: Shape,
    color: Color = Color(0xFF3670DB).copy(alpha = 0.2f),
    blur: Dp = 50.dp,
    offsetX: Dp = 40.dp,
    offsetY: Dp = 4.dp,
) = this.drawBehind {
    // 1. 전달받은 Shape로부터 현재 사이즈에 맞는 Outline을 생성합니다.
    val outline = shape.createOutline(size, layoutDirection, this)
    val path =
        Path().apply {
            // Outline을 Path 형태로 변환합니다.
            addOutline(outline)
        }

    drawIntoCanvas { canvas ->
        val paint =
            Paint().asFrameworkPaint().apply {
                this.color = android.graphics.Color.TRANSPARENT
                // 설정된 Offset과 Blur(Spread)를 적용합니다.
                setShadowLayer(
                    blur.toPx(),
                    offsetX.toPx(),
                    offsetY.toPx(),
                    color.toArgb(),
                )
            }
        // Compose Path를 Native Path로 변환하여 그림자를 그립니다.
        canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    }
}

@Preview
@Composable
private fun UploadScreenPreview() {
    BuyOrNotTheme {
        Scaffold {
            UploadScreen(modifier = Modifier.padding(it))
        }
    }
}
