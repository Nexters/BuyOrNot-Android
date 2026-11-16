package com.sseotdabwa.buyornot.feature.upload.album

import android.net.Uri
import com.sseotdabwa.buyornot.feature.upload.ui.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

private class FakeGallerySource : GallerySource {
    var gallery = Gallery(photos = emptyList(), buckets = emptyList())

    /** 조회를 붙잡아 두고 싶을 때 바꿔 끼운다. */
    var beforeLoad: suspend () -> Unit = {}

    override suspend fun load(): Gallery {
        val current = gallery
        beforeLoad()
        return current
    }
}

// android.net.Uri가 필요해 Robolectric으로 돌리고, 로컬 JDK 17에서도 뜨는 SDK로 고정한다.
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AlbumViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val source = FakeGallerySource()
    private val captured = Uri.parse("content://media/9")

    @Test
    fun `찍은_사진을_읽는_중에_화면_복귀로_다시_읽어도_찍은_사진을_고른다`() {
        val gate = CompletableDeferred<Unit>()
        source.beforeLoad = { gate.await() }
        source.gallery = Gallery(photos = listOf(GalleryPhoto(captured, bucketId = 1)), buckets = emptyList())
        val viewModel = AlbumViewModel(source)

        viewModel.handleIntent(AlbumIntent.OnPhotoCaptured(captured))
        source.beforeLoad = {}
        viewModel.handleIntent(AlbumIntent.OnAccessChanged(GalleryAccess.FULL))

        assertEquals(listOf(captured), viewModel.uiState.value.selected)
    }

    @Test
    fun `찍은_사진은_한_번만_자동으로_고르고_이후_조회에서는_사용자_선택을_따른다`() {
        source.gallery = Gallery(photos = listOf(GalleryPhoto(captured, bucketId = 1)), buckets = emptyList())
        val viewModel = AlbumViewModel(source)
        viewModel.handleIntent(AlbumIntent.OnPhotoCaptured(captured))
        viewModel.handleIntent(AlbumIntent.TogglePhoto(captured))

        viewModel.handleIntent(AlbumIntent.OnAccessChanged(GalleryAccess.FULL))

        assertEquals(emptyList(), viewModel.uiState.value.selected)
    }
}
