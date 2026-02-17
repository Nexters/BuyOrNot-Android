package com.sseotdabwa.buyornot.core.designsystem.icon

import androidx.annotation.RawRes
import com.sseotdabwa.buyornot.core.designsystem.R

@JvmInline
value class LottieResource(
    @param:RawRes val resId: Int,
)

object BuyOrNotLotties {
    val SplashLoading = LottieResource(R.raw.splash_loading)
}
