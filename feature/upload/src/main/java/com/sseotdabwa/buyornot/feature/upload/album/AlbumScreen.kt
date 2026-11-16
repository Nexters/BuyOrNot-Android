package com.sseotdabwa.buyornot.feature.upload.album

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotToggle
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.feature.upload.util.createCameraPhotoUri

private const val GRID_COLUMNS = 3
private val GridSpacing = 2.dp

/**
 * @param onConfirm 고른 순서대로 사진과 AI로 글쓰기 사용 여부를 받는다.
 */
@Composable
internal fun AlbumRoute(
    viewModel: AlbumViewModel,
    onBack: () -> Unit,
    onConfirm: (uris: List<Uri>, useAi: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarState = LocalSnackbarState.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is AlbumSideEffect.ShowSnackbar -> snackbarState.show(sideEffect.message)
            }
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.handleIntent(AlbumIntent.OnAccessChanged(context.galleryAccess()))
        }
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }

    // 설정 앱에서 권한을 바꾸고 돌아오거나, 일부 허용에서 사진을 더 고르고 돌아오면 다시 읽는다.
    LifecycleResumeEffect(Unit) {
        val access = context.galleryAccess()
        if (access == GalleryAccess.DENIED && !hasRequestedPermission) {
            hasRequestedPermission = true
            permissionLauncher.launch(galleryPermissions())
        } else {
            viewModel.handleIntent(AlbumIntent.OnAccessChanged(access))
        }
        onPauseOrDispose {}
    }

    // 카메라 앱에 다녀오는 사이 접었다 펴거나 프로세스가 죽어도 찍은 사진을 이어받도록 저장한다.
    var capturedUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val uri = capturedUri
            capturedUri = null
            if (uri == null) return@rememberLauncherForActivityResult
            if (success) {
                viewModel.handleIntent(AlbumIntent.OnPhotoCaptured(uri))
            } else {
                context.contentResolver.delete(uri, null, null)
            }
        }
    val launchCamera = {
        val uri = createCameraPhotoUri(context)
        if (uri == null) {
            snackbarState.show("사진을 저장할 수 없어요.")
        } else {
            capturedUri = uri
            cameraLauncher.launch(uri)
        }
    }
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera() else snackbarState.show("카메라 권한을 허용해 주세요.")
        }

    AlbumScreen(
        modifier = modifier,
        uiState = uiState,
        onBack = onBack,
        onConfirm = { onConfirm(uiState.selected, uiState.useAi) },
        onIntent = viewModel::handleIntent,
        onCameraClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        onRequestMorePhotos = { permissionLauncher.launch(galleryPermissions()) },
        onOpenSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
            )
        },
    )
}

@Composable
internal fun AlbumScreen(
    uiState: AlbumUiState,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onIntent: (AlbumIntent) -> Unit,
    onCameraClick: () -> Unit,
    onRequestMorePhotos: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visiblePhotos = remember(uiState.photos, uiState.selectedBucketId) { uiState.visiblePhotos }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(BuyOrNotTheme.colors.gray0)
                .statusBarsPadding(),
    ) {
        AlbumTopBar(
            uiState = uiState,
            onBack = onBack,
            onConfirm = onConfirm,
            onSelectBucket = { onIntent(AlbumIntent.SelectBucket(it)) },
        )
        Box(modifier = Modifier.weight(1f)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_COLUMNS),
                horizontalArrangement = Arrangement.spacedBy(GridSpacing),
                verticalArrangement = Arrangement.spacedBy(GridSpacing),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                when (uiState.access) {
                    GalleryAccess.PARTIAL ->
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            AccessNotice(text = "선택한 사진만 보여요.", actionText = "사진 더 선택", onAction = onRequestMorePhotos)
                        }
                    GalleryAccess.DENIED ->
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            AccessNotice(text = "사진에 접근하려면 권한이 필요해요.", actionText = "설정에서 허용", onAction = onOpenSettings)
                        }
                    else -> Unit
                }
                item(key = "camera") { CameraCell(onClick = onCameraClick) }
                items(items = visiblePhotos, key = { it.uri.toString() }) { photo ->
                    val order = uiState.selected.indexOf(photo.uri)
                    PhotoCell(
                        uri = photo.uri,
                        order = order.takeIf { it >= 0 }?.plus(1),
                        onClick = { onIntent(AlbumIntent.TogglePhoto(photo.uri)) },
                    )
                }
            }
        }
        AiWritingOption(
            checked = uiState.useAi,
            onCheckedChange = { onIntent(AlbumIntent.SetUseAi(it)) },
        )
    }
}

@Composable
private fun AlbumTopBar(
    uiState: AlbumUiState,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onSelectBucket: (Long?) -> Unit,
) {
    var showBuckets by remember { mutableStateOf(false) }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(60.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp)
                    .size(40.dp)
                    .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BuyOrNotIcons.ArrowLeft.asImageVector(),
                contentDescription = "뒤로가기",
                modifier = Modifier.size(20.dp),
                tint = BuyOrNotTheme.colors.gray950,
            )
        }
        Box(modifier = Modifier.align(Alignment.Center)) {
            Row(
                modifier = Modifier.clickable { showBuckets = true },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = uiState.selectedBucketName ?: "최근 항목",
                    style = BuyOrNotTheme.typography.subTitleS2SemiBold,
                    color = BuyOrNotTheme.colors.gray950,
                )
                Icon(
                    imageVector = BuyOrNotIcons.ArrowDown.asImageVector(),
                    contentDescription = "앨범 선택",
                    modifier = Modifier.size(14.dp),
                    tint = BuyOrNotTheme.colors.gray950,
                )
            }
            DropdownMenu(
                expanded = showBuckets,
                onDismissRequest = { showBuckets = false },
                containerColor = BuyOrNotTheme.colors.gray0,
            ) {
                BucketMenuItem(name = "최근 항목", selected = uiState.selectedBucketId == null) {
                    showBuckets = false
                    onSelectBucket(null)
                }
                uiState.buckets.forEach { bucket ->
                    BucketMenuItem(name = bucket.name, selected = uiState.selectedBucketId == bucket.id) {
                        showBuckets = false
                        onSelectBucket(bucket.id)
                    }
                }
            }
        }
        Text(
            text = "확인",
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
                    .clickable(enabled = uiState.canConfirm, onClick = onConfirm),
            style = BuyOrNotTheme.typography.subTitleS2SemiBold,
            color = if (uiState.canConfirm) BuyOrNotTheme.colors.gray950 else BuyOrNotTheme.colors.gray600,
        )
    }
}

@Composable
private fun BucketMenuItem(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = name,
                style = BuyOrNotTheme.typography.subTitleS3SemiBold,
                color = if (selected) BuyOrNotTheme.colors.gray950 else BuyOrNotTheme.colors.gray700,
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun AccessNotice(
    text: String,
    actionText: String,
    onAction: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(BuyOrNotTheme.colors.gray100)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = BuyOrNotTheme.typography.bodyB5Medium,
            color = BuyOrNotTheme.colors.gray800,
        )
        Text(
            text = actionText,
            modifier = Modifier.clickable(onClick = onAction),
            style = BuyOrNotTheme.typography.subTitleS4SemiBold,
            color = BuyOrNotTheme.colors.gray950,
        )
    }
}

@Composable
private fun CameraCell(onClick: () -> Unit) {
    Column(
        modifier =
            Modifier
                .aspectRatio(1f)
                .background(BuyOrNotTheme.colors.gray0)
                .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = BuyOrNotIcons.Camera.asImageVector(),
            contentDescription = null,
            modifier = Modifier.size(width = 24.dp, height = 22.dp),
            tint = BuyOrNotTheme.colors.gray950,
        )
        Text(
            text = "카메라",
            style = BuyOrNotTheme.typography.subTitleS4SemiBold,
            color = BuyOrNotTheme.colors.gray950,
        )
    }
}

/** @param order 고른 순서(1부터). 고르지 않았으면 null. */
@Composable
private fun PhotoCell(
    uri: Uri,
    order: Int?,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .aspectRatio(1f)
                .background(BuyOrNotTheme.colors.gray400)
                .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (order != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(BuyOrNotTheme.colors.black.copy(alpha = 0.5f)),
            )
        }
        SelectionBadge(
            order = order,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp),
        )
    }
}

@Composable
private fun SelectionBadge(
    order: Int?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(22.dp)
                .background(
                    color = if (order != null) BuyOrNotTheme.colors.gray100 else BuyOrNotTheme.colors.gray100.copy(alpha = 0.2f),
                    shape = CircleShape,
                ).border(width = 2.dp, color = BuyOrNotTheme.colors.gray0, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (order != null) {
            Text(
                text = order.toString(),
                style = BuyOrNotTheme.typography.titleT6Bold,
                color = BuyOrNotTheme.colors.gray950,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AiWritingOption(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = shape, ambientColor = Color.Black, spotColor = Color.Black)
                .clip(shape)
                .background(BuyOrNotTheme.colors.gray0)
                .navigationBarsPadding()
                .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    imageVector = BuyOrNotIcons.AiWriting.asImageVector(),
                    contentDescription = null,
                )
                Text(
                    text = "AI로 글쓰기",
                    style = BuyOrNotTheme.typography.titleT5Bold,
                    color = BuyOrNotTheme.colors.gray950,
                )
            }
            Text(
                text = "AI가 선택하신 사진을 분석해 투표 글을 작성해요.",
                style = BuyOrNotTheme.typography.bodyB5Medium,
                color = BuyOrNotTheme.colors.gray700,
            )
        }
        Spacer(modifier = Modifier.width(40.dp))
        BuyOrNotToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun galleryPermissions(): Array<String> =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

private fun Context.galleryAccess(): GalleryAccess {
    fun granted(permission: String) = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && granted(Manifest.permission.READ_MEDIA_IMAGES) -> GalleryAccess.FULL
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> GalleryAccess.PARTIAL
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && granted(Manifest.permission.READ_EXTERNAL_STORAGE) -> GalleryAccess.FULL
        else -> GalleryAccess.DENIED
    }
}

@Preview
@Composable
private fun AlbumScreenPreview() {
    BuyOrNotTheme {
        AlbumScreen(
            uiState = AlbumUiState(access = GalleryAccess.PARTIAL),
            onBack = {},
            onConfirm = {},
            onIntent = {},
            onCameraClick = {},
            onRequestMorePhotos = {},
            onOpenSettings = {},
        )
    }
}
