package com.sseotdabwa.buyornot.feature.auth.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotTextField
import com.sseotdabwa.buyornot.core.designsystem.components.PrimaryButton
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.nickname.NicknamePolicy
import com.sseotdabwa.buyornot.core.ui.performance.ReportScreenRendered
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState

@Composable
fun NicknameSetupRoute(
    onSetupComplete: () -> Unit,
    viewModel: NicknameSetupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = LocalSnackbarState.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                NicknameSetupSideEffect.NavigateToHome -> onSetupComplete()
                is NicknameSetupSideEffect.ShowSnackbar ->
                    snackbarState.show(
                        message = sideEffect.message,
                        icon = sideEffect.icon,
                        iconTint = sideEffect.iconTint,
                    )
            }
        }
    }

    // 원격 데이터를 기다리지 않고 바로 그려지는 화면이다.
    ReportScreenRendered(ready = true)

    NicknameSetupScreen(
        uiState = uiState,
        onNicknameChange = { viewModel.handleIntent(NicknameSetupIntent.UpdateNickname(it)) },
        onSubmit = { viewModel.handleIntent(NicknameSetupIntent.Submit) },
    )
}

@Composable
fun NicknameSetupScreen(
    uiState: NicknameSetupUiState,
    onNicknameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = true,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // 진입하자마자 키패드를 띄운다.
    LaunchedEffect(autoFocus) {
        if (autoFocus) focusRequester.requestFocus()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .imePadding()
                .pointerInput(Unit) {
                    // 입력창 밖을 누르면 키패드를 내린다.
                    detectTapGestures { focusManager.clearFocus() }
                },
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(69.dp))
            Text(
                text = "닉네임을 설정해 주세요!",
                style = BuyOrNotTheme.typography.headingH3Bold,
                color = BuyOrNotTheme.colors.gray950,
            )
            Spacer(modifier = Modifier.height(30.dp))
            BuyOrNotTextField(
                value = uiState.nickname,
                onValueChange = onNicknameChange,
                modifier = Modifier.focusRequester(focusRequester),
                placeholder = "닉네임을 입력해주세요",
                errorMessage = uiState.errorMessage,
                maxLength = NicknamePolicy.MAX_LENGTH,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            )
        }

        // 키패드 위에 붙는다.
        PrimaryButton(
            text = "시작하기",
            enabled = uiState.isSubmitEnabled,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            onClick = onSubmit,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NicknameSetupScreenEmptyPreview() {
    BuyOrNotTheme {
        NicknameSetupScreen(
            uiState = NicknameSetupUiState(),
            onNicknameChange = {},
            onSubmit = {},
            autoFocus = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NicknameSetupScreenErrorPreview() {
    BuyOrNotTheme {
        NicknameSetupScreen(
            uiState =
                NicknameSetupUiState(
                    nickname = "살까말까!",
                    errorMessage = "특수문자는 사용할 수 없어요.",
                ),
            onNicknameChange = {},
            onSubmit = {},
            autoFocus = false,
        )
    }
}
