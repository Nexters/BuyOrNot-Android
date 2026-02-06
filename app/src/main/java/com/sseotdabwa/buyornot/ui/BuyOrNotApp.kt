package com.sseotdabwa.buyornot.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sseotdabwa.buyornot.feature.auth.navigation.AUTH_ROUTE
import com.sseotdabwa.buyornot.feature.auth.navigation.SPLASH_ROUTE
import com.sseotdabwa.buyornot.feature.home.navigation.HOME_ROUTE
import com.sseotdabwa.buyornot.feature.mypage.navigation.MYPAGE_ROUTE
import com.sseotdabwa.buyornot.feature.upload.navigation.UPLOAD_ROUTE
import com.sseotdabwa.buyornot.navigation.BuyOrNotNavHost

/**
 * BuyOrNot 앱의 메인 컴포저블
 *
 * 네비게이션과 하단 네비게이션 바를 포함한 앱의 전체 구조를 정의합니다.
 * 스플래시 및 로그인 화면에서는 하단 바가 표시되지 않습니다.
 *
 *
 * 전체 화면이 필요하면 → bottomBarPadding() 함수의 리스트에 라우트 추가
 *
 * 일반 화면이면 → 아무 것도 하지 않아도 자동으로 패딩 적용
 *
 */
@Composable
fun BuyOrNotApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentDestination = navBackStackEntry?.destination

    // 하단 바를 숨길 화면 목록
    val routesWithoutBottomBar = setOf(SPLASH_ROUTE, AUTH_ROUTE)
    val showBottomBar = currentRoute !in routesWithoutBottomBar

    val destinations =
        listOf(
            TopLevelDestination(route = HOME_ROUTE, label = "홈", icon = Icons.Filled.Home),
            TopLevelDestination(route = UPLOAD_ROUTE, label = "업로드", icon = Icons.Filled.UploadFile),
            TopLevelDestination(route = MYPAGE_ROUTE, label = "마이페이지", icon = Icons.Filled.AccountCircle),
        )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BuyOrNotBottomBar(navController = navController, destinations = destinations)
            }
        },
    ) { innerPadding ->
        BuyOrNotNavHost(
            navController = navController,
            modifier = Modifier.bottomBarPadding(currentDestination, innerPadding),
        )
    }
}

/**
 * 하단 네비게이션 바 컴포넌트
 *
 * 메인 화면들(홈, 업로드, 마이페이지) 간 이동을 위한 네비게이션 바입니다.
 *
 * @param navController 네비게이션 컨트롤러
 * @param destinations 네비게이션 바에 표시될 목적지 목록
 */
@Composable
private fun BuyOrNotBottomBar(
    navController: NavHostController,
    destinations: List<TopLevelDestination>,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        destinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(imageVector = destination.icon, contentDescription = destination.label)
                },
                label = { Text(text = destination.label) },
            )
        }
    }
}

/**
 * 하단 네비게이션 바의 목적지 정보를 담는 데이터 클래스
 *
 * @property route 네비게이션 라우트
 * @property label 화면에 표시될 레이블
 * @property icon 네비게이션 아이템 아이콘
 */
private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

/**
 * 특정 화면(스플래시, 로그인)에서는 시스템 패딩을 제거하는 확장 함수
 *
 * NavHost에 적용되어, 전체 화면이 필요한 스플래시/로그인 화면에서는
 * 시스템 바 영역까지 확장되고, 일반 화면에서는 하단 바 패딩을 적용합니다.
 *
 * @param currentDestination 현재 네비게이션 목적지
 * @param padding Scaffold의 innerPadding (하단 바 높이 포함)
 * @return 조건에 따라 패딩이 적용되거나 제거된 Modifier
 */
private fun Modifier.bottomBarPadding(
    currentDestination: NavDestination?,
    padding: PaddingValues,
): Modifier =
    if (currentDestination?.route in listOf(SPLASH_ROUTE, AUTH_ROUTE)) {
        this
    } else {
        this.padding(padding)
    }
