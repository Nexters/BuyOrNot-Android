package com.sseotdabwa.buyornot.core.designsystem.icon

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.sseotdabwa.buyornot.core.designsystem.R

/**
 * BuyOrNot 프로젝트 전체에서 사용하는 아이콘 모음
 * Drawable 리소스의 커스텀 SVG 아이콘을 통합 관리
 *
 * 사용 예시:
 * ```
 * Icon(
 *     imageVector = BuyOrNotIcons.Add.asImageVector(),
 *     contentDescription = "추가"
 * )
 * ```
 */
object BuyOrNotIcons {
    val AppLogo = IconResource(R.drawable.ic_app_logo)

    // 로그인 화면 아이콘
    val GoogleLogo = IconResource(R.drawable.ic_google_logo)
    val KakaoLogo = IconResource(R.drawable.ic_kakao_logo)

    // 액션 아이콘
    val Add = IconResource(R.drawable.ic_add)
    val Close = IconResource(R.drawable.ic_close)
    val More = IconResource(R.drawable.ic_more)
    val Expand = IconResource(R.drawable.ic_expand)
    val Check = IconResource(R.drawable.ic_check)
    val Clock = IconResource(R.drawable.ic_clock)
    val Sort = IconResource(R.drawable.ic_sort)

    // 네비게이션 아이콘
    val ArrowLeft = IconResource(R.drawable.ic_arrow_left)
    val ArrowRight = IconResource(R.drawable.ic_arrow_right)
    val ArrowDown = IconResource(R.drawable.ic_arrow_down)

    // 기능 아이콘
    val CheckCircle = IconResource(R.drawable.ic_check_circle)
    val Camera = IconResource(R.drawable.ic_camera)
    val Link = IconResource(R.drawable.ic_link)
    val Vote = IconResource(R.drawable.ic_vote)
    val VoteDone = IconResource(R.drawable.ic_vote_done)
    val Bag = IconResource(R.drawable.ic_bag)
    val Profile = IconResource(R.drawable.ic_profile)
    val Notification = IconResource(R.drawable.ic_notification)
    val NotificationFilled = IconResource(R.drawable.ic_notification_filled)

    val NoVote = IconResource(R.drawable.ic_no_vote)

    val Won = IconResource(R.drawable.ic_won)
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
value class IconResource(
    @param:DrawableRes val resId: Int,
)

/**
 * IconResource를 ImageVector로 변환하는 확장 함수
 * Composable 컨텍스트에서만 사용 가능
 */
@Composable
fun IconResource.asImageVector(): ImageVector = ImageVector.vectorResource(id = resId)
