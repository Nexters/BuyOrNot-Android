package com.sseotdabwa.buyornot.core.ui.crop.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec

internal data class RotatePaneController(
    val commit: () -> Int,
)

@Composable
internal fun RotatePane(
    imageUri: Uri,
    editSpec: EditSpec,
    onControllerReady: (RotatePaneController) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tempQuarters by remember { mutableStateOf(editSpec.rotationQuarters) }

    DisposableEffect(Unit) {
        onControllerReady(RotatePaneController(commit = { tempQuarters }))
        onDispose { }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = -90f * tempQuarters },
                contentScale = ContentScale.Fit,
            )
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(
                onClick = { tempQuarters = (tempQuarters + 1) % 4 },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = BuyOrNotIcons.Close.asImageVector(),
                    contentDescription = "반시계 방향 90도 회전",
                    tint = Color.White,
                )
            }
        }
    }
}
