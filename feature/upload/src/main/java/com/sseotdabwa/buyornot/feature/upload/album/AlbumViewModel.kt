package com.sseotdabwa.buyornot.feature.upload.album

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.sseotdabwa.buyornot.core.common.util.runCatchingCancellable
import com.sseotdabwa.buyornot.core.ui.base.BaseViewModel
import com.sseotdabwa.buyornot.domain.model.MAX_FEED_IMAGE_COUNT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val gallerySource: GallerySource,
) : BaseViewModel<AlbumUiState, AlbumIntent, AlbumSideEffect>(AlbumUiState()) {
    override fun handleIntent(intent: AlbumIntent) {
        when (intent) {
            is AlbumIntent.OnAccessChanged -> onAccessChanged(intent.access)
            is AlbumIntent.TogglePhoto -> togglePhoto(intent.uri)
            is AlbumIntent.SelectBucket -> updateState { it.copy(selectedBucketId = intent.bucketId) }
            is AlbumIntent.SetUseAi -> updateState { it.copy(useAi = intent.useAi) }
            is AlbumIntent.OnPhotoCaptured -> loadGallery(captured = intent.uri)
        }
    }

    private fun onAccessChanged(access: GalleryAccess) {
        updateState { it.copy(access = access) }
        if (access == GalleryAccess.FULL || access == GalleryAccess.PARTIAL) loadGallery()
    }

    private fun togglePhoto(uri: Uri) {
        val selected = currentState.selected.toggled(uri, MAX_FEED_IMAGE_COUNT)
        if (selected == null) {
            sendSideEffect(AlbumSideEffect.ShowSnackbar("최대 ${MAX_FEED_IMAGE_COUNT}장까지 선택할 수 있어요"))
            return
        }
        updateState { it.copy(selected = selected) }
    }

    // 방금 찍은 사진은 목록을 다시 읽어야 보이고, 자리가 남았으면 바로 고른다.
    private fun loadGallery(captured: Uri? = null) {
        viewModelScope.launch {
            runCatchingCancellable { gallerySource.load() }
                .onSuccess { gallery ->
                    updateState { state ->
                        val available = gallery.photos.mapTo(HashSet()) { it.uri }
                        val kept = state.selected.filter { it in available }
                        val selected =
                            if (captured != null && captured !in kept && kept.size < MAX_FEED_IMAGE_COUNT) kept + captured else kept
                        state.copy(
                            photos = gallery.photos,
                            buckets = gallery.buckets,
                            selectedBucketId = state.selectedBucketId?.takeIf { id -> gallery.buckets.any { it.id == id } },
                            selected = selected,
                        )
                    }
                }.onFailure { e -> Timber.e(e, "Failed to load gallery") }
        }
    }
}
