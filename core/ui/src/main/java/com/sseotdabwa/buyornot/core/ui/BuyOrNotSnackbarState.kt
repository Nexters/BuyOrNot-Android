package com.sseotdabwa.buyornot.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.sseotdabwa.buyornot.core.designsystem.components.SnackBarIconTint
import com.sseotdabwa.buyornot.core.designsystem.components.showBuyOrNotSnackBar
import com.sseotdabwa.buyornot.core.designsystem.icon.IconResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class BuyOrNotSnackbarState(
    val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope,
) {
    fun show(
        message: String,
        icon: IconResource? = null,
        iconTint: SnackBarIconTint = SnackBarIconTint.Success,
    ) {
        coroutineScope.launch {
            showBuyOrNotSnackBar(
                snackbarHostState = snackbarHostState,
                message = message,
                iconResource = icon,
                iconTint = iconTint,
            )
        }
    }
}

@Composable
fun rememberBuyOrNotSnackbarState(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): BuyOrNotSnackbarState =
    remember(snackbarHostState, coroutineScope) {
        BuyOrNotSnackbarState(snackbarHostState, coroutineScope)
    }

val LocalSnackbarState =
    compositionLocalOf<BuyOrNotSnackbarState> {
        error("SnackbarState not provided")
    }
