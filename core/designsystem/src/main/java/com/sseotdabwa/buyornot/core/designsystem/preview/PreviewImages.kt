package com.sseotdabwa.buyornot.core.designsystem.preview

import com.sseotdabwa.buyornot.core.designsystem.R
import com.sseotdabwa.buyornot.core.designsystem.image.ImageModel

/**
 * 프리뷰 전용 샘플 이미지.
 *
 * `@Preview`가 외부 URL(picsum 등)을 때리면 스크린샷 비교 CI가 네트워크 상태에 좌우된다.
 * 아무도 UI를 건드리지 않은 PR에서 diff가 뜨는 원인이 되므로, 프리뷰는 여기의 로컬 리소스만 쓴다.
 *
 * 비율별로 나눠 둔 이유: FeedCard 프리뷰가 비율별 레이아웃 회귀를 잡으려고 만들어졌다.
 * 전부 같은 이미지로 치환하면 비율별 프리뷰가 픽셀 단위로 같아져 검증 의도가 사라진다.
 */
object PreviewImages {
    val square: ImageModel = ImageModel.Resource(R.drawable.preview_square)

    val landscape: ImageModel = ImageModel.Resource(R.drawable.preview_landscape)

    val portrait: ImageModel = ImageModel.Resource(R.drawable.preview_portrait)

    val avatar: ImageModel = ImageModel.Resource(R.drawable.preview_avatar)
}
