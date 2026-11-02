package com.sseotdabwa.buyornot.core.ui.image

import com.sseotdabwa.buyornot.core.designsystem.components.ImageAspectRatio
import com.sseotdabwa.buyornot.core.designsystem.image.ImageModel
import com.sseotdabwa.buyornot.domain.model.FeedImage
import com.sseotdabwa.buyornot.domain.model.ImageSource

fun ImageSource.toImageModel(): ImageModel = ImageModel.Network(url)

/** 프로필 이미지가 없는 사용자는 서버·로컬 저장소에 빈 문자열로 오므로, 빈 주소는 이미지 없음(null)으로 돌려준다. */
fun ImageSource?.toProfileImageModel(): ImageModel? = this?.takeIf { it.url.isNotEmpty() }?.toImageModel()

fun FeedImage.toAspectRatio(): ImageAspectRatio =
    when {
        imageWidth > imageHeight -> ImageAspectRatio.LANDSCAPE
        imageWidth < imageHeight -> ImageAspectRatio.PORTRAIT
        else -> ImageAspectRatio.SQUARE
    }
