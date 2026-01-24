package com.sseotdabwa.buyornot.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val LightColorScheme =
    BuyOrNotColorTheme(
        // Black & Gray
        black = Color(0xFF000000),
        gray1000 = Color(0xFF1A1C20),
        gray900 = Color(0xFF2A3038),
        gray800 = Color(0xFF565D6D),
        gray700 = Color(0xFF868B94),
        gray600 = Color(0xFFB1B3BB),
        gray500 = Color(0xFFD2D3D9),
        gray400 = Color(0xFFDDDEE4),
        gray300 = Color(0xFFEEEFF1),
        gray200 = Color(0xFFF3F4F5),
        gray100 = Color(0xFFF7F8F9),
        gray50 = Color(0xFFFBFBFC),
        gray0 = Color(0xFFFFFFFF),
        // Chromatic
        green200 = Color(0xFF0DAC7D),
        green100 = Color(0xFF42C694),
        red100 = Color(0xFFFF3830),
        blue100 = Color(0xFF217CF9),
    )

@Immutable
data class BuyOrNotColorTheme(
    // Black & Gray
    val black: Color,
    val gray1000: Color,
    val gray900: Color,
    val gray800: Color,
    val gray700: Color,
    val gray600: Color,
    val gray500: Color,
    val gray400: Color,
    val gray300: Color,
    val gray200: Color,
    val gray100: Color,
    val gray50: Color,
    val gray0: Color,
    // Chromatic
    val green200: Color,
    val green100: Color,
    val red100: Color,
    val blue100: Color,
)

val LocalColorScheme =
    staticCompositionLocalOf {
        BuyOrNotColorTheme(
            // Black & Gray
            black = Color.Unspecified,
            gray1000 = Color.Unspecified,
            gray900 = Color.Unspecified,
            gray800 = Color.Unspecified,
            gray700 = Color.Unspecified,
            gray600 = Color.Unspecified,
            gray500 = Color.Unspecified,
            gray400 = Color.Unspecified,
            gray300 = Color.Unspecified,
            gray200 = Color.Unspecified,
            gray100 = Color.Unspecified,
            gray50 = Color.Unspecified,
            gray0 = Color.Unspecified,
            // Chromatic
            green200 = Color.Unspecified,
            green100 = Color.Unspecified,
            red100 = Color.Unspecified,
            blue100 = Color.Unspecified,
        )
    }
