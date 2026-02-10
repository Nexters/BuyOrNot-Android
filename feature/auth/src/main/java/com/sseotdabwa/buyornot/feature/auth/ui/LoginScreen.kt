package com.sseotdabwa.buyornot.feature.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotImgs
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.domain.model.AppConstants

/**
 * Auth 화면의 네비게이션 진입점
 *
 * 로그인 화면을 표시하며, 네비게이션 그래프에서 호출됩니다.
 * 사용자의 소셜 로그인 액션을 처리하고, 약관 및 개인정보처리방침은
 * UriHandler를 통해 외부 브라우저로 열립니다.
 *
 * @param onGoogleLoginClick 구글 로그인 버튼 클릭 시 실행될 콜백
 * @param onKakaoLoginClick 카카오 로그인 버튼 클릭 시 실행될 콜백
 */
@Composable
fun AuthRoute(
    onGoogleLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onNavigateToWebView: (title: String, url: String) -> Unit,
) {
    LoginScreen(
        onGoogleLoginClick = onGoogleLoginClick,
        onKakaoLoginClick = onKakaoLoginClick,
        onTermsClick = {
            onNavigateToWebView(
                "서비스 약관",
                AppConstants.TERMS_URL,
            )
        },
        onPrivacyClick = {
            onNavigateToWebView(
                "개인정보처리방침",
                AppConstants.PRIVACY_URL,
            )
        },
    )
}

/**
 * 로그인 화면의 메인 컴포저블
 *
 * 상단에는 그래픽 섹션(배경 이미지),
 * 하단에는 소셜 로그인 버튼과 약관 동의 텍스트가 표시됩니다.
 *
 * @param onGoogleLoginClick 구글 로그인 버튼 클릭 콜백
 * @param onKakaoLoginClick 카카오 로그인 버튼 클릭 콜백
 * @param onTermsClick 서비스 약관 링크 클릭 콜백
 * @param onPrivacyClick 개인정보처리방침 링크 클릭 콜백
 */
@Composable
private fun LoginScreen(
    onGoogleLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(BuyOrNotTheme.colors.gray0),
    ) {
        LoginGraphicSection(modifier = Modifier.weight(1f))

        LoginInteractionSection(
            onGoogleLoginClick = onGoogleLoginClick,
            onKakaoLoginClick = onKakaoLoginClick,
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
        )
    }
}

/**
 * 로그인 화면의 상단 그래픽 영역
 *
 * 배경 이미지를 전체 너비로 표시합니다.
 * 이미지는 로컬 drawable 리소스에서 로드됩니다.
 *
 * @param modifier 레이아웃 수정자
 */
@Composable
private fun LoginGraphicSection(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = BuyOrNotImgs.LoginBackground.resId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillWidth,
        )
    }
}

/**
 * 로그인 화면의 하단 인터랙션 영역
 *
 * 타이틀 텍스트, 소셜 로그인 버튼(구글, 카카오),
 * 그리고 서비스 약관 동의 텍스트를 포함합니다.
 *
 * @param onGoogleLoginClick 구글 로그인 버튼 클릭 콜백
 * @param onKakaoLoginClick 카카오 로그인 버튼 클릭 콜백
 * @param onTermsClick 서비스 약관 링크 클릭 콜백
 * @param onPrivacyClick 개인정보처리방침 링크 클릭 콜백
 */
@Composable
private fun LoginInteractionSection(
    onGoogleLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "현명한 소비를 위한\n합리적인 비교 방법",
            style = BuyOrNotTheme.typography.headingH1SemiBold,
            color = BuyOrNotTheme.colors.gray900,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(60.dp))

        SocialLoginButton(
            text = "구글 계정으로 시작하기",
            iconResId = BuyOrNotIcons.GoogleLogo.resId,
            containerColor = Color.White,
            contentColor = BuyOrNotTheme.colors.gray900,
            hasBorder = true,
            onClick = onGoogleLoginClick,
        )

        Spacer(modifier = Modifier.height(12.dp))

        SocialLoginButton(
            text = "카카오로 시작하기",
            iconResId = BuyOrNotIcons.KakaoLogo.resId,
            containerColor = Color(0xFFFEE500),
            contentColor = Color(0xFF191919),
            hasBorder = false,
            onClick = onKakaoLoginClick,
        )

        Spacer(modifier = Modifier.height(30.dp))

        LoginFooterTermsText(
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
        )

        Spacer(modifier = Modifier.height(105.dp))
    }
}

/**
 * 소셜 로그인 버튼 컴포넌트
 *
 * @param text 버튼에 표시될 텍스트
 * @param iconResId 버튼 왼쪽에 표시될 아이콘의 drawable 리소스 ID
 * @param containerColor 버튼 배경색
 * @param contentColor 버튼 텍스트 및 아이콘 색상
 * @param hasBorder 테두리 표시 여부
 * @param onClick 버튼 클릭 시 실행될 콜백
 */
@Composable
private fun SocialLoginButton(
    text: String,
    iconResId: Int,
    containerColor: Color,
    contentColor: Color,
    hasBorder: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        border = if (hasBorder) BorderStroke(1.dp, BuyOrNotTheme.colors.gray300) else null,
        elevation = null,
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(end = 10.dp)
                        .align(Alignment.CenterVertically),
            )
            Text(
                text = text,
                style = BuyOrNotTheme.typography.bodyB3Medium,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
}

/**
 * 로그인 화면 하단의 약관 동의 텍스트
 *
 * LinkAnnotation을 사용하여 클릭 가능한 링크를 구현합니다.
 * 서비스 약관 및 개인정보처리방침 링크가 포함된 안내 문구를 표시합니다.
 *
 * @param onTermsClick 서비스 약관 링크 클릭 시 실행될 콜백
 * @param onPrivacyClick 개인정보처리방침 링크 클릭 시 실행될 콜백
 */
@Composable
private fun LoginFooterTermsText(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    val baseColor = BuyOrNotTheme.colors.gray600

    val annotatedString =
        buildAnnotatedString {
            withStyle(style = SpanStyle(color = baseColor)) {
                append("가입을 진행하시면 ")

                // 서비스 약관 링크
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "terms",
                        styles =
                            TextLinkStyles(
                                style =
                                    SpanStyle(
                                        color = baseColor,
                                        textDecoration = TextDecoration.Underline,
                                    ),
                            ),
                        linkInteractionListener = {
                            onTermsClick()
                        },
                    ),
                ) {
                    append("서비스 약관")
                }

                append(" 및 ")

                // 개인정보처리방침 링크
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "privacy",
                        styles =
                            TextLinkStyles(
                                style =
                                    SpanStyle(
                                        color = baseColor,
                                        textDecoration = TextDecoration.Underline,
                                    ),
                            ),
                        linkInteractionListener = {
                            onPrivacyClick()
                        },
                    ),
                ) {
                    append("개인정보처리방침")
                }

                append("에\n동의 하시는 것으로 간주합니다.")
            }
        }

    Text(
        text = annotatedString,
        style =
            BuyOrNotTheme.typography.captionC2Medium.copy(
                textAlign = TextAlign.Center,
                color = baseColor,
            ),
        modifier = Modifier.fillMaxWidth(),
    )
}

// --- PREVIEWS ---

/**
 * 로그인 화면 프리뷰
 */
@Preview(name = "LoginScreen - Pixel 5", device = "id:pixel_5", showBackground = true)
@Composable
private fun LoginScreenPreview() {
    BuyOrNotTheme {
        LoginScreen(
            onGoogleLoginClick = {},
            onKakaoLoginClick = {},
            onTermsClick = {},
            onPrivacyClick = {},
        )
    }
}
