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
import androidx.compose.foundation.layout.ime
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import java.text.DecimalFormat

@Composable
fun UploadRoute(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToHomeReview: () -> Unit = {},
    viewModel: UploadViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = LocalSnackbarState.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is UploadSideEffect.ShowSnackbar -> snackbarState.show(sideEffect.message)
                is UploadSideEffect.NavigateBack -> onNavigateBack()
                is UploadSideEffect.NavigateToHomeReview -> onNavigateToHomeReview()
            }
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents(),
        ) { uris: List<Uri> ->
            if (uris.isNotEmpty()) viewModel.handleIntent(UploadIntent.AddImages(uris))
        }

    UploadScreen(
        modifier = modifier,
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onPickImage = {
            if (uiState.selectedImageUris.size < 3) galleryLauncher.launch("image/*")
        },
        onSubmit = {
            keyboardController?.hide()
            viewModel.handleIntent(UploadIntent.Submit(context))
        },
    )
}

@Composable
fun UploadScreen(
    uiState: UploadUiState,
    onIntent: (UploadIntent) -> Unit,
    onPickImage: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val decimalFormat = remember { DecimalFormat("#,###") }
    val scrollState = rememberScrollState()

    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    val isSubmitEnabled =
        remember(uiState) {
            uiState.category != null &&
                uiState.price.isNotEmpty() &&
                uiState.title.isNotEmpty() &&
                uiState.selectedImageUris.isNotEmpty() &&
                !uiState.isLoading
        }

    BackHandler {
        if (uiState.hasInput) {
            if (!uiState.showExitDialog) onIntent(UploadIntent.UpdateExitDialogVisibility(true))
        } else {
            onIntent(UploadIntent.NavigateBack)
        }
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
            if (uiState.hasInput) {
                onIntent(UploadIntent.UpdateExitDialogVisibility(true))
            } else {
                onIntent(UploadIntent.NavigateBack)
            }
        }

        Column(
            modifier =
                Modifier
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .weight(1f),
        ) {
            CategorySelectorRow(
                selectedCategory = uiState.category?.displayName,
                onCategoryClick = { onIntent(UploadIntent.UpdateCategorySheetVisibility(true)) },
            )

            HorizontalDivider(
                thickness = 2.dp,
                color = BuyOrNotTheme.colors.gray100,
            )

            LinkInputField(
                modifier = Modifier.padding(vertical = 18.dp),
                link = uiState.link,
                onLinkChange = { onIntent(UploadIntent.UpdateLink(it)) },
            )

            HorizontalDivider(
                thickness = 2.dp,
                color = BuyOrNotTheme.colors.gray100,
            )

            PriceInputField(
                modifier = Modifier.padding(vertical = 18.dp),
                priceFieldValue = uiState.priceFieldValue,
                priceRaw = uiState.price,
                decimalFormat = decimalFormat,
                onPriceChange = { digits, textFieldValue ->
                    onIntent(UploadIntent.UpdatePrice(digits, textFieldValue))
                },
            )

            HorizontalDivider(
                thickness = 2.dp,
                color = BuyOrNotTheme.colors.gray100,
            )

            Spacer(modifier = Modifier.height(20.dp))

            ContentInputField(
                title = uiState.title,
                onTitleChange = { onIntent(UploadIntent.UpdateTitle(it)) },
                content = uiState.content,
                onContentChange = { onIntent(UploadIntent.UpdateContent(it)) },
            )

            Spacer(modifier = Modifier.height(10.dp))

            ImagePickerRow(
                selectedImageUris = uiState.selectedImageUris,
                onPickImage = onPickImage,
                onRemoveImage = { uri -> onIntent(UploadIntent.RemoveImage(uri)) },
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = if (isImeVisible) Arrangement.SpaceBetween else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isImeVisible) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = BuyOrNotIcons.Camera.asImageVector(),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = BuyOrNotTheme.colors.gray800,
                    )
                    Text(
                        text = "${uiState.selectedImageUris.size}/3",
                        style = BuyOrNotTheme.typography.subTitleS5SemiBold,
                        color = BuyOrNotTheme.colors.gray800,
                    )
                }
            } else {
                if (isSubmitEnabled) {
                    ToolTip()
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            CapsuleButton(
                text = "투표 게시!",
                enabled = isSubmitEnabled,
                size = ButtonSize.Small,
                onClick = onSubmit,
            )
        }
    }

    if (uiState.showCategorySheet) {
        OptionSheet(
            title = "카테고리 선택",
            options = uiState.categories.map { it.displayName },
            selectedOption = uiState.category?.displayName,
            onOptionClick = { displayName ->
                val category = uiState.categories.find { it.displayName == displayName }
                if (category != null) {
                    onIntent(UploadIntent.UpdateCategory(category))
                }
            },
            onDismissRequest = {
                onIntent(UploadIntent.UpdateCategorySheetVisibility(false))
            },
        )
    }

    if (uiState.showExitDialog) {
        BuyOrNotAlertDialog(
            onDismissRequest = { onIntent(UploadIntent.UpdateExitDialogVisibility(false)) },
            title = "다음에 등록할까요?",
            subText = "지금까지 쓴 내용은 저장되지 않아요.",
            confirmText = "유지하기",
            dismissText = "나가기",
            onConfirm = {
                onIntent(UploadIntent.UpdateExitDialogVisibility(false))
            },
            onDismiss = {
                onIntent(UploadIntent.UpdateExitDialogVisibility(false))
                onIntent(UploadIntent.NavigateBack)
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
private fun LinkInputField(
    link: String,
    onLinkChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = BuyOrNotIcons.Link.asImageVector(),
            contentDescription = "Link",
            modifier = Modifier.size(18.dp),
            tint = BuyOrNotTheme.colors.gray600,
        )
        Spacer(modifier = Modifier.width(6.dp))

        BasicTextField(
            value = link,
            onValueChange = onLinkChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle =
                BuyOrNotTheme.typography.subTitleS3SemiBold.copy(
                    color = BuyOrNotTheme.colors.gray800,
                ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (link.isEmpty()) {
                    Text(
                        text = "상품 링크 (선택)",
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
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        BasicTextField(
            value = title,
            onValueChange = { if (it.length <= 40) onTitleChange(it) },
            modifier = Modifier.fillMaxWidth(),
            textStyle =
                BuyOrNotTheme.typography.titleT2Bold.copy(
                    color = BuyOrNotTheme.colors.gray900,
                ),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (title.isEmpty()) {
                    Text(
                        text = "제목",
                        style = BuyOrNotTheme.typography.titleT2Bold,
                        color = BuyOrNotTheme.colors.gray600,
                    )
                }
                innerTextField()
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

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
    selectedImageUris: List<Uri>,
    onPickImage: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CameraButton(
            selectedCount = selectedImageUris.size,
            enabled = selectedImageUris.size < 3,
            onClick = onPickImage,
        )

        selectedImageUris.forEach { uri ->
            SelectedImagePreview(
                imageUri = uri,
                onRemove = { onRemoveImage(uri) },
            )
        }
    }
}

@Composable
private fun CameraButton(
    selectedCount: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(68.dp),
        shape = RoundedCornerShape(12.dp),
        color = BuyOrNotTheme.colors.gray100,
        onClick = onClick,
        enabled = enabled,
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
                text = "$selectedCount/3",
                style = BuyOrNotTheme.typography.subTitleS5SemiBold,
                color = BuyOrNotTheme.colors.gray600,
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
    val outline = shape.createOutline(size, layoutDirection, this)
    val path =
        Path().apply {
            addOutline(outline)
        }

    drawIntoCanvas { canvas ->
        val paint =
            Paint().asFrameworkPaint().apply {
                this.color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    blur.toPx(),
                    offsetX.toPx(),
                    offsetY.toPx(),
                    color.toArgb(),
                )
            }
        canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    }
}

@Preview
@Composable
private fun UploadScreenPreview() {
    BuyOrNotTheme {
        UploadScreen(
            uiState = UploadUiState(),
            onIntent = {},
            onPickImage = {},
            onSubmit = {},
        )
    }
}
