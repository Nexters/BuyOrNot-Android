package com.sseotdabwa.buyornot.core.ui.imageviewer

import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.sseotdabwa.buyornot.core.designsystem.image.ImageModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

@Serializable
internal data class ImageViewerRoute(
    val images: List<ViewerImage>,
    val initialPage: Int = 0,
)

/** 라우트 인자로 직렬화할 수 있는 [ImageModel]. */
@Serializable
internal sealed interface ViewerImage {
    @Serializable
    @SerialName("network")
    data class Network(
        val url: String,
    ) : ViewerImage

    @Serializable
    @SerialName("resource")
    data class Resource(
        val resId: Int,
    ) : ViewerImage

    @Serializable
    @SerialName("local")
    data class Local(
        val uri: String,
    ) : ViewerImage
}

internal fun ImageModel.toViewerImage(): ViewerImage =
    when (this) {
        is ImageModel.Network -> ViewerImage.Network(url)
        is ImageModel.Resource -> ViewerImage.Resource(resId)
        is ImageModel.Local -> ViewerImage.Local(uri.toString())
    }

internal fun ViewerImage.toImageModel(): ImageModel =
    when (this) {
        is ViewerImage.Network -> ImageModel.Network(url)
        is ViewerImage.Resource -> ImageModel.Resource(resId)
        is ViewerImage.Local -> ImageModel.Local(Uri.parse(uri))
    }

internal val ViewerImagesNavType =
    object : NavType<List<ViewerImage>>(isNullableAllowed = false) {
        override fun get(
            bundle: Bundle,
            key: String,
        ): List<ViewerImage>? = bundle.getString(key)?.let(::parseValue)

        override fun put(
            bundle: Bundle,
            key: String,
            value: List<ViewerImage>,
        ) = bundle.putString(key, Json.encodeToString(value))

        override fun parseValue(value: String): List<ViewerImage> = Json.decodeFromString(value)

        override fun serializeAsValue(value: List<ViewerImage>): String = Uri.encode(Json.encodeToString(value))
    }

fun NavController.navigateToImageViewer(
    images: List<ImageModel>,
    initialPage: Int = 0,
) {
    navigate(ImageViewerRoute(images = images.map { it.toViewerImage() }, initialPage = initialPage))
}

fun NavGraphBuilder.imageViewerScreen(onBackClick: () -> Unit) {
    dialog<ImageViewerRoute>(
        typeMap = mapOf(typeOf<List<ViewerImage>>() to ViewerImagesNavType),
        dialogProperties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnClickOutside = false,
            ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<ImageViewerRoute>()
        ImageViewerScreen(
            images = route.images.map { it.toImageModel() },
            initialPage = route.initialPage,
            onBackClick = onBackClick,
        )
    }
}
