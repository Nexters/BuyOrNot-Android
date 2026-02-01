package com.sseotdabwa.buyornot.core.designsystem.icon

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.sseotdabwa.buyornot.core.designsystem.R

/**
 * BuyOrNot 프로젝트 전체에서 사용하는 이미지 모음
 * Drawable 리소스의 커스텀 png, jpg 등의 이미지들을 통합 관리
 *
 */
object BuyOrNotImgs {
    //로그인 화면 배경
    val img_login_bg = IconResource(R.drawable.img_login_bg)
}

/**
 * Drawable 리소스를 래핑하는 타입
 * SVG 아이콘을 타입 안전하게 관리하기 위해 사용
 *
 * 사용 예시:
 * ```
 * Icon(
 *     imageVector = BuyOrNotIcons.Vote.asImageVector(),
 *     contentDescription = "투표"
 * )
 * ```
 */
@JvmInline
value class ImgsResource(
    @param:DrawableRes val resId: Int,
)

/**
 * ImgsResource를 ImageVector로 변환하는 확장 함수
 * Composable 컨텍스트에서만 사용 가능
 */
@Composable
fun ImgsResource.asImageVector(): ImageVector = ImageVector.vectorResource(id = resId)
