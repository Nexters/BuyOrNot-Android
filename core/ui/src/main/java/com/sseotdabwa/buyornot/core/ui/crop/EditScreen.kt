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

@Composable
fun EditScreen(
    imageUri: Uri,
    onConfirm: (Uri) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var mode by remember { mutableStateOf(EditMode.Idle) }
    var editSpec by remember { mutableStateOf(EditSpec()) }
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
                isConfirmEnabled = !isProcessing,
                onLeftAction = {
                    if (isProcessing) return@EditTopBar
                    when (mode) {
                        EditMode.Idle -> onCancel()
                        EditMode.Crop -> {
                            cropController = null
                            mode = EditMode.Idle
                        }
                    }
                },
                onConfirm = {
                    if (isProcessing) return@EditTopBar
                    when (mode) {
                        EditMode.Idle -> {
                            isProcessing = true
                            scope.launch {
                                val result = processToFile(context, imageUri, editSpec)
                                isProcessing = false
                                result.fold(
                                    onSuccess = { onConfirm(it) },
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
        onConfirm = {},
        onCancel = {},
    )
}
