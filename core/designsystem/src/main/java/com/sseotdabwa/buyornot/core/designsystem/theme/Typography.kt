package com.sseotdabwa.buyornot.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.sseotdabwa.buyornot.core.designsystem.R

internal val pretendardFamily =
    FontFamily(
        Font(R.font.pretendardbold, FontWeight.Bold),
        Font(R.font.pretendardsemibold, FontWeight.SemiBold),
        Font(R.font.pretendardmedium, FontWeight.Medium),
        Font(R.font.pretendardregular, FontWeight.Normal),
    )

private val baseTextStyle =
    TextStyle(
        fontFamily = pretendardFamily,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle =
            LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None,
            ),
    )

internal val Typography =
    BuyOrNotTypography(
        // Display
        displayD1Bold =
            baseTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                lineHeight = (36 * 1.5f).sp,
            ),
        displayD2Bold =
            baseTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = (32 * 1.45f).sp,
            ),
        // Heading
        headingH1Bold =
            baseTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = (28 * 1.4f).sp,
            ),
        headingH2Bold =
            baseTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = (24 * 1.4f).sp,
            ),
        headingH3Bold =
            baseTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = (22 * 1.4f).sp,
            ),
        headingH4Bold =
            baseTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = (20 * 1.35f).sp,
            ),
        headingH1SemiBold =
            baseTextStyle.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = (24 * 1.4f).sp,
            ),
        // Title
        titleT1Bold =
            baseTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = (18 * 1.25f).sp,
            ),
        titleT2Bold =
            baseTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = (16 * 1.25f).sp,
            ),
        titleT3Bold =
            baseTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = (15 * 1.25f).sp,
            ),
        titleT4Bold =
            baseTextStyle.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = (14 * 1.25f).sp,
            ),
        // Sub Title
        subTitleS1SemiBold =
            baseTextStyle.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = (18 * 1.2f).sp,
            ),
        subTitleS2SemiBold =
            baseTextStyle.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = (16 * 1.25f).sp,
            ),
        subTitleS3SemiBold =
            baseTextStyle.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = (15 * 1.2f).sp,
            ),
        subTitleS4SemiBold =
            baseTextStyle.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = (14 * 1.25f).sp,
            ),
        subTitleS5SemiBold =
            baseTextStyle.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = (13 * 1.25f).sp,
            ),
        // Body
        bodyB1Medium =
            baseTextStyle.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = (18 * 1.25f).sp,
            ),
        bodyB2Medium =
            baseTextStyle.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = (16 * 1.25f).sp,
            ),
        bodyB3Medium =
            baseTextStyle.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = (15 * 1.25f).sp,
            ),
        bodyB4Medium =
            baseTextStyle.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = (14 * 1.25f).sp,
            ),
        bodyB5Medium =
            baseTextStyle.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = (13 * 1.25f).sp,
            ),
        bodyB6Medium =
            baseTextStyle.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = (12 * 1.25f).sp,
            ),
        bodyB7Medium =
            baseTextStyle.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = (11 * 1.25f).sp,
            ),
        // Caption
        captionC1Medium =
            baseTextStyle.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = (12 * 1.4f).sp,
            ),
        captionC2Medium =
            baseTextStyle.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = (11 * 1.4f).sp,
            ),
        captionC3Medium =
            baseTextStyle.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                lineHeight = (10 * 1.4f).sp,
            ),
        captionC1Regular =
            baseTextStyle.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = (12 * 1.4f).sp,
            ),
        captionC2Regular =
            baseTextStyle.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = (11 * 1.4f).sp,
            ),
        captionC3Regular =
            baseTextStyle.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                lineHeight = (10 * 1.4f).sp,
            ),
    )

@Immutable
data class BuyOrNotTypography(
    val displayD1Bold: TextStyle,
    val displayD2Bold: TextStyle,
    val headingH1Bold: TextStyle,
    val headingH2Bold: TextStyle,
    val headingH3Bold: TextStyle,
    val headingH4Bold: TextStyle,
    val headingH1SemiBold: TextStyle,
    val titleT1Bold: TextStyle,
    val titleT2Bold: TextStyle,
    val titleT3Bold: TextStyle,
    val titleT4Bold: TextStyle,
    val subTitleS1SemiBold: TextStyle,
    val subTitleS2SemiBold: TextStyle,
    val subTitleS3SemiBold: TextStyle,
    val subTitleS4SemiBold: TextStyle,
    val subTitleS5SemiBold: TextStyle,
    val bodyB1Medium: TextStyle,
    val bodyB2Medium: TextStyle,
    val bodyB3Medium: TextStyle,
    val bodyB4Medium: TextStyle,
    val bodyB5Medium: TextStyle,
    val bodyB6Medium: TextStyle,
    val bodyB7Medium: TextStyle,
    val captionC1Medium: TextStyle,
    val captionC2Medium: TextStyle,
    val captionC3Medium: TextStyle,
    val captionC1Regular: TextStyle,
    val captionC2Regular: TextStyle,
    val captionC3Regular: TextStyle,
)

val LocalTypography =
    staticCompositionLocalOf {
        Typography
    }
