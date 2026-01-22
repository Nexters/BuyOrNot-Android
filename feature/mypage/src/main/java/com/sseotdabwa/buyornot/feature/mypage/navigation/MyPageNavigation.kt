package com.sseotdabwa.buyornot.feature.mypage.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sseotdabwa.buyornot.feature.mypage.ui.MyPageRoute

const val MYPAGE_ROUTE = "mypage"

fun NavGraphBuilder.myPageScreen() {
    composable(route = MYPAGE_ROUTE) {
        MyPageRoute()
    }
}
