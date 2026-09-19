package com.sseotdabwa.buyornot.core.designsystem.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.sseotdabwa.buyornot.core.designsystem.R

/**
 * 프리뷰 전용 샘플 이미지 (#153).
 *
 * `@Preview`가 외부 URL(picsum 등)을 때리면 스크린샷 비교 CI가 네트워크 상태에 좌우된다.
 * 아무도 UI를 건드리지 않은 PR에서 diff가 뜨는 원인이 되므로, 프리뷰는 여기의 로컬 리소스 URI만 쓴다.
 *
 * Coil이 `android.resource://` 스킴을 지원하므로 파라미터 타입이 `String`인 채로 로컬 리소스를 넘길 수 있다.
 *
 * 비율을 셋으로 나눠 둔 이유: FeedCard 프리뷰가 1:1 / 5:4 / 4:5 레이아웃 회귀를 잡으려고 만들어졌다.
 * 전부 같은 이미지로 치환하면 그 세 프리뷰가 픽셀 단위로 같아져 검증 의도가 사라진다.
 */
object PreviewImages {
    /** 1:1 상품 이미지 */
    @Composable
    fun square(): String = resourceUri(R.drawable.preview_square)

    /** 5:4 가로 상품 이미지 */
    @Composable
    fun landscape(): String = resourceUri(R.drawable.preview_landscape)

    /** 4:5 세로 상품 이미지 */
    @Composable
    fun portrait(): String = resourceUri(R.drawable.preview_portrait)

    /** 프로필 이미지 */
    @Composable
    fun avatar(): String = resourceUri(R.drawable.preview_avatar)

    @Composable
    private fun resourceUri(resId: Int): String {
        val packageName = LocalContext.current.packageName
        return remember(packageName, resId) { "android.resource://$packageName/$resId" }
    }
}
