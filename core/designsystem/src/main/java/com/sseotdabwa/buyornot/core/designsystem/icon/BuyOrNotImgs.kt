package com.sseotdabwa.buyornot.core.designsystem.icon

import androidx.annotation.DrawableRes
import com.sseotdabwa.buyornot.core.designsystem.R

/**
 * BuyOrNot 프로젝트 전체에서 사용하는 비트맵 이미지 모음
 *
 * PNG, JPG 등의 비트맵 이미지 리소스를 통합 관리합니다.
 * 벡터 아이콘(SVG)은 [BuyOrNotIcons]를 사용하세요.
 *
 * 사용 예시:
 * ```
 * Image(
 *     painter = painterResource(id = BuyOrNotImgs.LoginBackground.resId),
 *     contentDescription = "로그인 배경"
 * )
 * ```
 */
object BuyOrNotImgs {
    /**
     * 로그인 화면 배경 이미지
     */
    val LoginBackground = ImgsResource(R.drawable.img_login_bg)

    val HomeBanner = ImgsResource(R.drawable.img_home_banner)
}

/**
 * Drawable 비트맵 리소스를 래핑하는 타입
 *
 * PNG, JPG 등의 비트맵 이미지를 타입 안전하게 관리하기 위해 사용합니다.
 * painterResource()와 함께 사용하여 Compose에서 이미지를 표시합니다.
 *
 * @property resId drawable 리소스 ID
 */
@JvmInline
value class ImgsResource(
    @param:DrawableRes val resId: Int,
)
