package com.sseotdabwa.buyornot.feature.mypage.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sseotdabwa.buyornot.core.designsystem.components.ActionItem
import com.sseotdabwa.buyornot.core.designsystem.components.ActionSheet
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotAlertDialog
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotTextField
import com.sseotdabwa.buyornot.core.designsystem.components.PrimaryButton
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.nickname.NicknamePolicy
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.ProfileEditIntent
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.ProfileEditSideEffect
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.ProfileEditUiState
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.ProfileEditViewModel

/**
 * @param onNavigateToCrop 고르거나 찍은 사진을 1:1 자르기 화면으로 넘긴다. 결과는 [ProfileEditIntent.SelectImage]로 돌아온다.
 * @param onProfileUpdated 저장에 성공했을 때. 이전 화면에 갱신을 알리고 돌아간다.
 */
@Composable
fun ProfileEditRoute(
    onBackClick: () -> Unit,
    onNavigateToCrop: (Uri) -> Unit,
    onProfileUpdated: () -> Unit,
    viewModel: ProfileEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = LocalSnackbarState.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is ProfileEditSideEffect.ShowSnackbar ->
                    snackbarState.show(
                        message = sideEffect.message,
                        icon = sideEffect.icon,
                        iconTint = sideEffect.iconTint,
                    )
                ProfileEditSideEffect.NavigateBack -> onBackClick()
                ProfileEditSideEffect.NavigateBackWithUpdate -> onProfileUpdated()
            }
        }
    }

    // 저장 중에 떠나면 결과를 이전 화면에 알리지 못하므로 뒤로가기를 막는다.
    BackHandler(enabled = uiState.isSaving) {}

    var showPhotoSheet by rememberSaveable { mutableStateOf(false) }
    // 카메라 앱이 사진을 저장할 MediaStore URI. 촬영이 끝날 때까지 들고 있는다.
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val insertPhotoUri: () -> Uri? = {
        val contentValues =
            ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "buyornot_${System.currentTimeMillis()}.jpg",
                )
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BuyOrNot")
                }
            }
        context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues,
        )
    }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
        ) { success ->
            if (success) {
                photoUri?.let(onNavigateToCrop)
            } else {
                photoUri?.let { context.contentResolver.delete(it, null, null) }
            }
            photoUri = null
        }

    val launchCamera = {
        val uri = insertPhotoUri()
        if (uri != null) {
            photoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) {
                launchCamera()
            } else {
                snackbarState.show("카메라 권한을 허용해 주세요.")
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri: Uri? ->
            uri?.let(onNavigateToCrop)
        }

    ProfileEditScreen(
        uiState = uiState,
        onBackClick = { if (!uiState.isSaving) onBackClick() },
        onNicknameChange = { viewModel.handleIntent(ProfileEditIntent.UpdateNickname(it)) },
        onImageClick = { if (uiState.isProfileLoaded && !uiState.isSaving) showPhotoSheet = true },
        onSubmit = { viewModel.handleIntent(ProfileEditIntent.Submit) },
        onConfirmNicknameChange = { viewModel.handleIntent(ProfileEditIntent.ConfirmNicknameChange) },
        onDismissNicknameDialog = { viewModel.handleIntent(ProfileEditIntent.DismissNicknameDialog) },
    )

    if (showPhotoSheet) {
        ActionSheet(
            actions =
                listOf(
                    ActionItem(
                        icon = BuyOrNotIcons.Camera,
                        text = "카메라로 직접 찍기",
                        onClick = {
                            val hasCameraPermission =
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA,
                                ) == PackageManager.PERMISSION_GRANTED
                            if (hasCameraPermission) {
                                launchCamera()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                    ),
                    ActionItem(
                        icon = BuyOrNotIcons.Gallery,
                        text = "앨범에서 사진 선택",
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    ),
                ),
            onDismissRequest = { showPhotoSheet = false },
        )
    }
}

@Composable
fun ProfileEditScreen(
    uiState: ProfileEditUiState,
    onBackClick: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onImageClick: () -> Unit,
    onSubmit: () -> Unit,
    onConfirmNicknameChange: () -> Unit,
    onDismissNicknameDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .imePadding()
                .pointerInput(Unit) {
                    // 입력창 밖을 누르면 키패드를 내린다.
                    detectTapGestures { focusManager.clearFocus() }
                },
    ) {
        BackTopBarWithTitle(
            title = "프로필 설정",
            onBackClick = onBackClick,
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            ProfileImageEditor(
                // 새로 고른 이미지가 있으면 저장 전이라도 미리 보여준다.
                imageModel = uiState.selectedImageUri ?: uiState.profileImageUrl,
                onClick = onImageClick,
            )

            Spacer(modifier = Modifier.height(30.dp))

            BuyOrNotTextField(
                value = uiState.nickname,
                onValueChange = onNicknameChange,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                placeholder = "닉네임을 입력해주세요",
                label = "닉네임",
                enabled = uiState.isProfileLoaded,
                errorMessage = uiState.errorMessage,
                maxLength = NicknamePolicy.MAX_LENGTH,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
        }

        // 키패드 위에 붙는다.
        PrimaryButton(
            text = "완료",
            enabled = uiState.isSubmitEnabled,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            onClick = {
                focusManager.clearFocus()
                onSubmit()
            },
        )
    }

    if (uiState.isNicknameDialogVisible) {
        BuyOrNotAlertDialog(
            onDismissRequest = onDismissNicknameDialog,
            title = "닉네임을 변경할까요?",
            subText = "닉네임은 20일마다 1번 수정할 수 있어요.",
            confirmText = "변경",
            dismissText = "취소",
            onConfirm = onConfirmNicknameChange,
            onDismiss = onDismissNicknameDialog,
        )
    }
}

/** 120dp 원형 프로필 이미지와 우하단 편집 배지. 어디를 눌러도 사진 변경 메뉴를 연다. */
@Composable
private fun ProfileImageEditor(
    imageModel: String,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.size(120.dp)) {
        AsyncImage(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(color = BuyOrNotTheme.colors.gray100, shape = CircleShape)
                    .border(width = 1.5.dp, color = BuyOrNotTheme.colors.gray300, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onClick),
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(imageModel)
                    .crossfade(true)
                    .build(),
            contentDescription = "UserProfileImage",
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(29.dp)
                    .background(color = BuyOrNotTheme.colors.gray50, shape = CircleShape)
                    .border(width = 1.5.dp, color = BuyOrNotTheme.colors.gray300, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BuyOrNotIcons.Pencil.asImageVector(),
                contentDescription = "프로필 이미지 변경",
                modifier = Modifier.size(18.dp),
                tint = BuyOrNotTheme.colors.gray600,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileEditScreenPreview() {
    BuyOrNotTheme {
        ProfileEditScreen(
            uiState =
                ProfileEditUiState(
                    originalNickname = "서따봐",
                    isProfileLoaded = true,
                    nickname = "서따봐",
                ),
            onBackClick = {},
            onNicknameChange = {},
            onImageClick = {},
            onSubmit = {},
            onConfirmNicknameChange = {},
            onDismissNicknameDialog = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileEditScreenErrorPreview() {
    BuyOrNotTheme {
        ProfileEditScreen(
            uiState =
                ProfileEditUiState(
                    originalNickname = "서따봐",
                    isProfileLoaded = true,
                    nickname = "살까말까",
                    errorMessage = "이미 사용 중인 닉네임이에요.",
                ),
            onBackClick = {},
            onNicknameChange = {},
            onImageClick = {},
            onSubmit = {},
            onConfirmNicknameChange = {},
            onDismissNicknameDialog = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileEditScreenNicknameDialogPreview() {
    BuyOrNotTheme {
        ProfileEditScreen(
            uiState =
                ProfileEditUiState(
                    originalNickname = "서따봐",
                    isProfileLoaded = true,
                    nickname = "살까말까",
                    isNicknameDialogVisible = true,
                ),
            onBackClick = {},
            onNicknameChange = {},
            onImageClick = {},
            onSubmit = {},
            onConfirmNicknameChange = {},
            onDismissNicknameDialog = {},
        )
    }
}
