package com.sseotdabwa.buyornot.feature.mypage.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sseotdabwa.buyornot.core.designsystem.components.BackTopBarWithTitle
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotButtonDefaults
import com.sseotdabwa.buyornot.core.designsystem.components.BuyOrNotEmptyView
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotImgs
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.ui.snackbar.LocalSnackbarState
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.BlockedAccountsIntent
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.BlockedAccountsSideEffect
import com.sseotdabwa.buyornot.feature.mypage.viewmodel.BlockedAccountsViewModel

data class BlockedUserItem(
    val userId: Long,
    val profileImageUrl: String,
    val nickname: String,
)

@Composable
fun BlockedAccountsRoute(
    onBackClick: () -> Unit,
    viewModel: BlockedAccountsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = LocalSnackbarState.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is BlockedAccountsSideEffect.ShowSnackbar -> {
                    snackbarState.show(
                        message = sideEffect.message,
                        icon = sideEffect.icon,
                        iconTint = sideEffect.iconTint,
                    )
                }
            }
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        BlockedAccountsScreen(
            blockedUsers = uiState.blockedUsers,
            onUnblockClick = { userId, nickname ->
                viewModel.handleIntent(BlockedAccountsIntent.UnblockUser(userId, nickname))
            },
            onBackClick = onBackClick,
        )
    }
}

@Composable
fun BlockedAccountsScreen(
    modifier: Modifier = Modifier,
    blockedUsers: List<BlockedUserItem> = emptyList(),
    onUnblockClick: (userId: Long, nickname: String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackTopBarWithTitle(
            title = "차단된 계정",
            onBackClick = onBackClick,
        )

        if (blockedUsers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                BuyOrNotEmptyView(
                    title = "차단된 사용자가 없어요",
                    description = "사용자를 차단하면 투표를 볼 수 없어요.",
                    image = BuyOrNotImgs.NoBlockedUser.resId,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(blockedUsers, key = { it.userId }) { user ->
                    BlockedUser(
                        profileImageUrl = user.profileImageUrl,
                        nickname = user.nickname,
                        onUnblockClick = { onUnblockClick(user.userId, user.nickname) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockedUser(
    profileImageUrl: String,
    nickname: String,
    onUnblockClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                modifier =
                    Modifier
                        .background(
                            color = BuyOrNotTheme.colors.gray500,
                            shape = CircleShape,
                        ).size(42.dp)
                        .clip(CircleShape),
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(profileImageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = "UserProfileImage",
                contentScale = ContentScale.Crop,
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = nickname,
                style = BuyOrNotTheme.typography.paragraphP2Medium,
                color = BuyOrNotTheme.colors.gray900,
            )
        }

        UnblockButton(onClick = onUnblockClick)
    }
}

@Composable
private fun UnblockButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val colors = BuyOrNotButtonDefaults.primaryButtonColors()

    val containerColor by animateColorAsState(
        targetValue =
            when {
                isPressed -> colors.pressedContainer
                isHovered -> colors.hoverContainer
                else -> colors.defaultContainer
            },
        label = "unblockButtonContainerColor",
    )

    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(12.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = colors.content,
                disabledContainerColor = colors.disabledContainer,
                disabledContentColor = colors.disabledContent,
            ),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        Text(
            text = "차단해제",
            style = BuyOrNotTheme.typography.subTitleS5SemiBold,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockedUserPreview() {
    BuyOrNotTheme {
        BlockedUser(
            profileImageUrl = "",
            nickname = "결정장애",
            onUnblockClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockedAccountsScreenEmptyPreview() {
    BuyOrNotTheme {
        Scaffold(
            containerColor = BuyOrNotTheme.colors.gray0,
        ) { paddingValues ->
            BlockedAccountsScreen(
                modifier = Modifier.padding(paddingValues),
                blockedUsers = emptyList(),
                onBackClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockedAccountsScreenPreview() {
    BuyOrNotTheme {
        Scaffold(
            containerColor = BuyOrNotTheme.colors.gray0,
        ) { paddingValues ->
            BlockedAccountsScreen(
                modifier = Modifier.padding(paddingValues),
                blockedUsers =
                    listOf(
                        BlockedUserItem(userId = 1L, profileImageUrl = "", nickname = "결정장애"),
                        BlockedUserItem(userId = 2L, profileImageUrl = "", nickname = "패션피플"),
                    ),
                onBackClick = {},
            )
        }
    }
}
