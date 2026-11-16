package com.sseotdabwa.buyornot.feature.upload.album

import android.net.Uri
import androidx.compose.runtime.Immutable

enum class GalleryAccess {
    UNKNOWN,
    FULL,

    /** Android 14부터 사용자가 일부 사진만 허용한 상태. 허용한 사진만 보인다. */
    PARTIAL,
    DENIED,
}

@Immutable
data class AlbumUiState(
    val access: GalleryAccess = GalleryAccess.UNKNOWN,
    val photos: List<GalleryPhoto> = emptyList(),
    val buckets: List<GalleryBucket> = emptyList(),
    /** null이면 모든 사진(최근 항목). */
    val selectedBucketId: Long? = null,
    /** 고른 순서를 지킨다. 순서가 배지 번호와 등록 화면의 사진 순서가 된다. */
    val selected: List<Uri> = emptyList(),
    val useAi: Boolean = true,
) {
    val visiblePhotos: List<GalleryPhoto>
        get() = selectedBucketId?.let { id -> photos.filter { it.bucketId == id } } ?: photos

    val selectedBucketName: String?
        get() = buckets.firstOrNull { it.id == selectedBucketId }?.name

    val canConfirm: Boolean
        get() = selected.isNotEmpty()
}

sealed interface AlbumIntent {
    data class OnAccessChanged(
        val access: GalleryAccess,
    ) : AlbumIntent

    data class TogglePhoto(
        val uri: Uri,
    ) : AlbumIntent

    data class SelectBucket(
        val bucketId: Long?,
    ) : AlbumIntent

    data class SetUseAi(
        val useAi: Boolean,
    ) : AlbumIntent

    data class OnPhotoCaptured(
        val uri: Uri,
    ) : AlbumIntent
}

sealed interface AlbumSideEffect {
    data class ShowSnackbar(
        val message: String,
    ) : AlbumSideEffect
}

/** [uri]를 고르거나 고르기를 취소한다. [maxCount]를 넘으면 null. */
internal fun List<Uri>.toggled(
    uri: Uri,
    maxCount: Int,
): List<Uri>? =
    when {
        uri in this -> this - uri
        size >= maxCount -> null
        else -> this + uri
    }
