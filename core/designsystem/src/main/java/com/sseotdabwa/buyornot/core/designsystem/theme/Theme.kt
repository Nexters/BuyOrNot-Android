package com.sseotdabwa.buyornot.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun BuyOrNotTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalColorScheme provides LightColorScheme,
        LocalTypography provides Typography,
    ) {
        content()
    }
}

object BuyOrNotTheme {
    val colors: BuyOrNotColorTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColorScheme.current

    val typography: BuyOrNotTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current
}
