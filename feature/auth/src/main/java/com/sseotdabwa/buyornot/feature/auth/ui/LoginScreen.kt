package com.sseotdabwa.buyornot.feature.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotImgs
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState

@Composable
fun AuthRoute(
    onLoginSuccess: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = LocalSnackbarState.current
    val context = LocalContext.current

    // SideEffect 처리
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect {
            when (it) {
                is LoginSideEffect.NavigateToHome -> onLoginSuccess()
                is LoginSideEffect.ShowSnackbar -> {
                    snackbarState.show(
                        message = it.message,
                        icon = it.icon,
                        iconTint = it.iconTint,
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LoginScreen(
            isLoading = uiState.isLoading,
            onGoogleLoginClick = { viewModel.handleIntent(LoginIntent.GoogleLogin(context)) },
            onKakaoLoginClick = { viewModel.handleIntent(LoginIntent.KakaoLogin(context)) },
            onGuestStartClick = { viewModel.handleIntent(LoginIntent.SkipLogin) },
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
        )
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}

/**
 * 로그인 화면의 메인 컴포저블
 */
@Composable
private fun LoginScreen(
    isLoading: Boolean,
    onGoogleLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onGuestStartClick: () -> Unit,
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
            isLoading = isLoading,
            onGoogleLoginClick = onGoogleLoginClick,
            onKakaoLoginClick = onKakaoLoginClick,
            onGuestStartClick = onGuestStartClick,
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
        )
    }
}

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

@Composable
private fun LoginInteractionSection(
    isLoading: Boolean,
    onGoogleLoginClick: () -> Unit,
    onKakaoLoginClick: () -> Unit,
    onGuestStartClick: () -> Unit,
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
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "현명한 소비를 위한\n집단지성 비교 방법",
            style = BuyOrNotTheme.typography.headingH1SemiBold,
            color = BuyOrNotTheme.colors.gray950,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(60.dp))

        SocialLoginButton(
            text = "구글 계정으로 시작하기",
            iconResId = BuyOrNotIcons.GoogleLogo.resId,
            containerColor = Color.White,
            contentColor = BuyOrNotTheme.colors.gray950,
            hasBorder = true,
            enabled = !isLoading,
            onClick = onGoogleLoginClick,
        )

        Spacer(modifier = Modifier.height(12.dp))

        SocialLoginButton(
            text = "카카오로 시작하기",
            iconResId = BuyOrNotIcons.KakaoLogo.resId,
            containerColor = Color(0xFFFEE500),
            contentColor = Color(0xFF191919),
            hasBorder = false,
            enabled = !isLoading,
            onClick = onKakaoLoginClick,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "비회원으로 시작하기",
            modifier =
                Modifier.clickable(enabled = !isLoading) {
                    onGuestStartClick()
                },
            style = BuyOrNotTheme.typography.captionC2Medium,
            color = BuyOrNotTheme.colors.gray700,
            textDecoration = TextDecoration.Underline,
        )

        Spacer(modifier = Modifier.height(28.dp))

        LoginFooterTermsText(
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
        )

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    iconResId: Int,
    containerColor: Color,
    contentColor: Color,
    hasBorder: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
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
                        linkInteractionListener = { onTermsClick() },
                    ),
                ) {
                    append("서비스 약관")
                }
                append(" 및 ")
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
                        linkInteractionListener = { onPrivacyClick() },
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

@Preview(name = "LoginScreen - Pixel 5", device = "id:pixel_5", showBackground = true)
@Composable
private fun LoginScreenPreview() {
    BuyOrNotTheme {
        LoginScreen(
            isLoading = false,
            onGoogleLoginClick = {},
            onKakaoLoginClick = {},
            onGuestStartClick = {},
            onTermsClick = {},
            onPrivacyClick = {},
        )
    }
}
