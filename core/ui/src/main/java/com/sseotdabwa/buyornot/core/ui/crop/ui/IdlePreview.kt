package com.sseotdabwa.buyornot.core.ui.crop.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.sseotdabwa.buyornot.core.ui.crop.processing.produceEditedPreview
import com.sseotdabwa.buyornot.core.ui.crop.state.EditSpec

@Composable
internal fun IdlePreview(
    imageUri: Uri,
    editSpec: EditSpec,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var bitmap by remember(imageUri, editSpec) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUri, editSpec) {
        produceEditedPreview(context, imageUri, editSpec).onSuccess { bitmap = it }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 375, heightDp = 812)
@Composable
private fun IdlePreviewPlaceholderPreview() {
    IdlePreview(
        imageUri = Uri.EMPTY,
        editSpec = EditSpec(),
    )
}
