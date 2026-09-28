package com.sseotdabwa.buyornot.core.ui.crop

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.sseotdabwa.buyornot.core.ui.crop.processing.processToFile
import com.sseotdabwa.buyornot.core.ui.crop.state.AspectRatio
import com.sseotdabwa.buyornot.core.ui.crop.state.EditEvent
import com.sseotdabwa.buyornot.core.ui.crop.state.EditMode
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec
import com.sseotdabwa.buyornot.core.ui.crop.state.reduce
import com.sseotdabwa.buyornot.core.ui.crop.ui.CropPane
import com.sseotdabwa.buyornot.core.ui.crop.ui.CropPaneController
import com.sseotdabwa.buyornot.core.ui.crop.ui.EditTopBar
import com.sseotdabwa.buyornot.core.ui.crop.ui.IdleActionBar
import com.sseotdabwa.buyornot.core.ui.crop.ui.IdlePreview
import kotlinx.coroutines.launch

/**
 * @param lockedRatio 지정하면 해당 비율로만 자를 수 있다. 아직 자르지 않았다면 자르기 모드로 바로 시작한다.
 */
@Composable
fun EditScreen(
    imageUri: Uri,
    onConfirm: (Uri, EditSpec) -> Unit,
    onCancel: () -> Unit,
    initialSpec: EditSpec = EditSpec(),
    lockedRatio: AspectRatio? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var editSpec by remember { mutableStateOf(initialSpec) }
    // 비율이 고정됐는데 아직 그 비율로 자르지 않았는지 여부. 이 상태로는 편집을 끝낼 수 없다.
    val needsLockedCrop = lockedRatio != null && editSpec.crop?.ratio != lockedRatio
    var mode by remember { mutableStateOf(if (needsLockedCrop) EditMode.Crop else EditMode.Idle) }
    var isProcessing by remember { mutableStateOf(false) }
    var pendingError by remember { mutableStateOf<String?>(null) }

    var cropController by remember { mutableStateOf<CropPaneController?>(null) }

    LaunchedEffect(pendingError) {
        pendingError?.let {
            snackbarHostState.showSnackbar(it)
            pendingError = null
        }
    }

    Scaffold(
        topBar = {
            EditTopBar(
                mode = mode,
                // 자르기 모드에서는 이미지가 로드되어 컨트롤러가 준비된 뒤에만 확정할 수 있다.
                isConfirmEnabled = !isProcessing && (mode != EditMode.Crop || cropController != null),
                onLeftAction = {
                    if (isProcessing) return@EditTopBar
                    when (mode) {
                        EditMode.Idle -> onCancel()
                        EditMode.Crop -> {
                            cropController = null
                            // 고정 비율로 자르기 전이면 돌아갈 편집 화면이 없으므로 편집 자체를 취소한다.
                            if (needsLockedCrop) onCancel() else mode = EditMode.Idle
                        }
                    }
                },
                onConfirm = {
                    if (isProcessing) return@EditTopBar
                    when (mode) {
                        EditMode.Idle -> {
                            if (needsLockedCrop) {
                                mode = EditMode.Crop
                                return@EditTopBar
                            }
                            isProcessing = true
                            scope.launch {
                                val result = processToFile(context, imageUri, editSpec)
                                isProcessing = false
                                result.fold(
                                    onSuccess = { onConfirm(it, editSpec) },
                                    onFailure = { pendingError = it.message ?: "이미지 처리에 실패했습니다" },
                                )
                            }
                        }
                        EditMode.Crop -> {
                            cropController?.let {
                                editSpec = reduce(editSpec, EditEvent.CommitCrop(it.commit()))
                            }
                            cropController = null
                            mode = EditMode.Idle
                        }
                    }
                },
            )
        },
        bottomBar = {
            when (mode) {
                EditMode.Idle ->
                    IdleActionBar(
                        onCropClick = { mode = EditMode.Crop },
                        onRotateClick = {
                            editSpec =
                                reduce(
                                    editSpec,
                                    EditEvent.CommitRotate(editSpec.rotationQuarters + 1),
                                )
                        },
                    )
                EditMode.Crop -> { /* mode-specific bottom is inside the Pane */ }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (mode) {
                EditMode.Idle -> IdlePreview(imageUri = imageUri, editSpec = editSpec)
                EditMode.Crop ->
                    CropPane(
                        imageUri = imageUri,
                        editSpec = editSpec,
                        onControllerReady = { cropController = it },
                        lockedRatio = lockedRatio,
                        onPreviewError = { pendingError = "이미지를 불러오지 못했습니다" },
                    )
            }
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 375, heightDp = 812)
@Composable
private fun EditScreenIdlePreview() {
    EditScreen(
        imageUri = Uri.EMPTY,
        onConfirm = { _, _ -> },
        onCancel = {},
    )
}
