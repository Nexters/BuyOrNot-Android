package com.sseotdabwa.buyornot.feature.mypage.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.sseotdabwa.buyornot.core.ui.crop.EDIT_RESULT_KEY
import com.sseotdabwa.buyornot.core.ui.crop.EDIT_RESULT_SKIPPED
import com.sseotdabwa.buyornot.core.ui.crop.EDIT_RESULT_SPEC_KEY
import com.sseotdabwa.buyornot.core.ui.crop.navigateToEdit
import com.sseotdabwa.buyornot.core.ui.crop.state.AspectRatio
import com.sseotdabwa.buyornot.core.ui.webview.navigateToFeedBack
import com.sseotdabwa.buyornot.core.ui.webview.navigateToPrivacyPolicy
import com.sseotdabwa.buyornot.core.ui.webview.navigateToTerms
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.MyPageIntent
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.MyPageViewModel
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.ProfileEditIntent
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.ProfileEditViewModel
import kotlinx.serialization.Serializable
import com.sseotdabwa.buyornot.feature.mypage.ui.AccountSettingRoute as AccountSettingScreen
import com.sseotdabwa.buyornot.feature.mypage.ui.BlockedAccountsRoute as BlockedAccountsScreen
import com.sseotdabwa.buyornot.feature.mypage.ui.MyPageRoute as MyPageScreen
import com.sseotdabwa.buyornot.feature.mypage.ui.PolicyRoute as PolicyScreen
import com.sseotdabwa.buyornot.feature.mypage.ui.ProfileEditRoute as ProfileEditScreen
import com.sseotdabwa.buyornot.feature.mypage.ui.WithdrawalRoute as WithdrawalScreen

@Serializable
data object MyPageGraph

@Serializable
data object MyPageMainRoute

@Serializable
data object AccountSettingRoute

@Serializable
data object PolicyRoute

@Serializable
data object WithdrawalRoute

@Serializable
data object BlockedAccountsRoute

@Serializable
data object ProfileEditRoute

// 프로필 수정 성공 시 이전 화면(마이페이지)의 savedStateHandle에 남기는 갱신 신호
private const val PROFILE_UPDATED_KEY = "profileUpdated"

fun NavController.navigateToMyPage() {
    navigate(MyPageGraph)
}

fun NavController.navigateToAccountSetting() {
    navigate(AccountSettingRoute)
}

fun NavController.navigateToPolicy() {
    navigate(PolicyRoute)
}

fun NavController.navigateToWithdrawal() {
    navigate(WithdrawalRoute)
}

fun NavController.navigateToBlockedAccounts() {
    navigate(BlockedAccountsRoute)
}

fun NavController.navigateToProfileEdit() {
    navigate(ProfileEditRoute)
}

fun NavGraphBuilder.myPageGraph(
    navController: NavHostController,
    versionName: String,
    onNavigateToLogin: () -> Unit,
) {
    navigation<MyPageGraph>(startDestination = MyPageMainRoute) {
        composable<MyPageMainRoute> { backStackEntry ->
            val viewModel = hiltViewModel<MyPageViewModel>()
            OnProfileUpdated(backStackEntry) { viewModel.handleIntent(MyPageIntent.RefreshProfile) }
            MyPageScreen(
                versionName = versionName,
                onBackClick = navController::popBackStack,
                onProfileClick = navController::navigateToProfileEdit,
                onAccountSettingClick = navController::navigateToAccountSetting,
                onBlockedAccountsClick = navController::navigateToBlockedAccounts,
                onPolicyClick = navController::navigateToPolicy,
                onFeedbackClick = navController::navigateToFeedBack,
                viewModel = viewModel,
            )
        }

        composable<AccountSettingRoute> {
            AccountSettingScreen(
                onBackClick = navController::popBackStack,
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToWithdrawal = navController::navigateToWithdrawal,
            )
        }

        composable<ProfileEditRoute> { backStackEntry ->
            val viewModel = hiltViewModel<ProfileEditViewModel>()

            val editResult by backStackEntry.savedStateHandle
                .getStateFlow<String?>(EDIT_RESULT_KEY, null)
                .collectAsStateWithLifecycle()

            LaunchedEffect(editResult) {
                val result = editResult ?: return@LaunchedEffect
                backStackEntry.savedStateHandle.remove<String>(EDIT_RESULT_KEY)
                backStackEntry.savedStateHandle.remove<String>(EDIT_RESULT_SPEC_KEY)
                // 자르기를 취소하면 이전에 고른 이미지를 그대로 둔다.
                if (result != EDIT_RESULT_SKIPPED) {
                    viewModel.handleIntent(ProfileEditIntent.SelectImage(result))
                }
            }

            ProfileEditScreen(
                onBackClick = navController::popBackStack,
                onNavigateToCrop = { uri: Uri ->
                    navController.navigateToEdit(uri = uri, lockedRatio = AspectRatio.R1x1)
                },
                onProfileUpdated = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(PROFILE_UPDATED_KEY, true)
                    navController.popBackStack()
                },
                viewModel = viewModel,
            )
        }

        composable<PolicyRoute> {
            PolicyScreen(
                onBackClick = navController::popBackStack,
                onNavigateToTerms = navController::navigateToTerms,
                onNavigateToPrivacyPolicy = navController::navigateToPrivacyPolicy,
            )
        }

        composable<WithdrawalRoute> {
            WithdrawalScreen(
                onBackClick = navController::popBackStack,
                onNavigateToLogin = onNavigateToLogin,
            )
        }

        composable<BlockedAccountsRoute> {
            BlockedAccountsScreen(
                onBackClick = navController::popBackStack,
            )
        }
    }
}

/** 프로필 수정 화면이 남긴 갱신 신호를 한 번만 소비한다. */
@Composable
private fun OnProfileUpdated(
    backStackEntry: NavBackStackEntry,
    onUpdated: () -> Unit,
) {
    val isUpdated by backStackEntry.savedStateHandle
        .getStateFlow(PROFILE_UPDATED_KEY, false)
        .collectAsStateWithLifecycle()

    LaunchedEffect(isUpdated) {
        if (!isUpdated) return@LaunchedEffect
        backStackEntry.savedStateHandle[PROFILE_UPDATED_KEY] = false
        onUpdated()
    }
}
