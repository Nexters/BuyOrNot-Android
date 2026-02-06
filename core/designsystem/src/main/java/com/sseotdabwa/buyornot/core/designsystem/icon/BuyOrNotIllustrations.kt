package com.sseotdabwa.buyornot.core.designsystem.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.sseotdabwa.buyornot.core.designsystem.R

object BuyOrNotIllustrations {
    val Coin: Painter
        @Composable
        get() = painterResource(id = R.drawable.img_illust_coin)

    val TShirt: Painter
        @Composable
        get() = painterResource(id = R.drawable.img_illust_tshirt)

    val Clap: Painter
        @Composable
        get() = painterResource(id = R.drawable.img_illust_clap)

    val Phone: Painter
        @Composable
        get() = painterResource(id = R.drawable.img_illust_phone)

    val Drawer: Painter
        @Composable
        get() = painterResource(id = R.drawable.img_illust_drawer)

    val Socks: Painter
        @Composable
        get() = painterResource(id = R.drawable.img_illust_socks)

    val Book: Painter
        @Composable
        get() = painterResource(id = R.drawable.img_illust_book)

    val Pants: Painter
        @Composable
        get() = painterResource(id = R.drawable.img_illust_pants)

    val Bill: Painter
        @Composable
        get() = painterResource(id = R.drawable.img_illust_bill)
}
