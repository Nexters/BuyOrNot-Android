package com.sseotdabwa.buyornot.core.designsystem.image

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

/** 화면에 그릴 이미지가 어디서 오는지를 타입으로 구분한다. */
@Immutable
sealed interface ImageModel {
    @JvmInline
    value class Network(
        val url: String,
    ) : ImageModel

    @JvmInline
    value class Resource(
        @DrawableRes val resId: Int,
    ) : ImageModel

    @JvmInline
    value class Local(
        val uri: Uri,
    ) : ImageModel
}

/** Coil `AsyncImage(model = …)`·`ImageRequest.Builder.data(…)`에 넘길 값. */
fun ImageModel.toCoilModel(): Any =
    when (this) {
        is ImageModel.Network -> url
        is ImageModel.Resource -> resId
        is ImageModel.Local -> uri
    }
