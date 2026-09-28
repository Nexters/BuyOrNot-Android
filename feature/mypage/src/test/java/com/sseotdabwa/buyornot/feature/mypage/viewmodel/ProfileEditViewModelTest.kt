package com.sseotdabwa.buyornot.feature.mypage.viewmodel

import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.domain.exception.ApiException
import com.sseotdabwa.buyornot.domain.model.UserProfile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileEditViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val log = CallLog()
    private val userRepository = FakeUserRepository(log)
    private val feedRepository = FakeFeedRepository(log)
    private val userPreferencesRepository = FakeUserPreferencesRepository()
    private val imageReader = FakeProfileImageReader(log)

    private fun createViewModel() =
        ProfileEditViewModel(
            userRepository = userRepository,
            feedRepository = feedRepository,
            userPreferencesRepository = userPreferencesRepository,
            profileImageReader = imageReader,
        )

    @Test
    fun `진입하면_현재_닉네임과_프로필_이미지를_보여준다`() {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals("서따봐", state.originalNickname)
        assertEquals("서따봐", state.nickname)
        assertEquals("https://cdn/old.jpg", state.profileImageUrl)
    }

    @Test
    fun `프로필을_불러오기_전에는_입력을_받지_않고_완료_버튼이_비활성화된다`() {
        val pending = CompletableDeferred<UserProfile>()
        userRepository.loadProfile = { pending.await() }
        val viewModel = createViewModel()

        viewModel.handleIntent(ProfileEditIntent.UpdateNickname("살까말까"))
        viewModel.handleIntent(ProfileEditIntent.SelectImage("file:///cropped.jpg"))

        assertEquals("", viewModel.uiState.value.nickname)
        assertFalse(viewModel.uiState.value.isProfileLoaded)
        assertFalse(viewModel.uiState.value.isSubmitEnabled)

        pending.complete(userRepository.profile)

        assertEquals("서따봐", viewModel.uiState.value.nickname)
        assertTrue(viewModel.uiState.value.isProfileLoaded)
    }

    @Test
    fun `프로필을_불러오지_못하면_스낵바를_띄우고_이전_화면으로_돌아간다`() =
        runTest {
            userRepository.loadProfile = { throw IllegalStateException("network") }
            val viewModel = createViewModel()

            assertFalse(viewModel.uiState.value.isProfileLoaded)
            assertFalse(viewModel.uiState.value.isSubmitEnabled)
            assertEquals(
                listOf(
                    ProfileEditSideEffect.ShowSnackbar("프로필을 불러오지 못했어요."),
                    ProfileEditSideEffect.NavigateBack,
                ),
                viewModel.sideEffect.take(2).toList(),
            )
        }

    @Test
    fun `닉네임을_바꾸지_않으면_완료_버튼이_비활성화된다`() {
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun `앞뒤_공백만_다른_닉네임은_바뀌지_않은_것으로_보고_완료_버튼이_비활성화된다`() {
        val viewModel = createViewModel()

        viewModel.handleIntent(ProfileEditIntent.UpdateNickname(" 서따봐 "))

        assertFalse(viewModel.uiState.value.isNicknameChanged)
        assertFalse(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun `닉네임을_바꾸면_완료_버튼이_활성화된다`() {
        val viewModel = createViewModel()

        viewModel.handleIntent(ProfileEditIntent.UpdateNickname("살까말까"))

        assertTrue(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun `닉네임을_모두_지우면_완료_버튼이_비활성화된다`() {
        val viewModel = createViewModel()

        viewModel.handleIntent(ProfileEditIntent.UpdateNickname(""))

        assertFalse(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun `에러_문구가_있으면_완료_버튼이_비활성화된다`() {
        val state =
            ProfileEditUiState(
                originalNickname = "서따봐",
                nickname = "살까말까",
                errorMessage = "이미 사용 중인 닉네임이에요.",
                isProfileLoaded = true,
            )

        assertFalse(state.isSubmitEnabled)
    }

    @Test
    fun `닉네임을_다시_입력하면_에러_문구를_지운다`() {
        userRepository.updateError = ApiException(code = "USER_011", message = null)
        val viewModel = createViewModel()
        viewModel.handleIntent(ProfileEditIntent.UpdateNickname("살까말까"))
        viewModel.handleIntent(ProfileEditIntent.Submit)
        viewModel.handleIntent(ProfileEditIntent.ConfirmNicknameChange)

        viewModel.handleIntent(ProfileEditIntent.UpdateNickname("살까말까2"))

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `이미지만_바꿔도_완료_버튼이_활성화된다`() {
        val viewModel = createViewModel()

        viewModel.handleIntent(ProfileEditIntent.SelectImage("file:///cropped.jpg"))

        assertTrue(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun `닉네임을_바꾸고_완료하면_저장하지_않고_확인_다이얼로그를_띄운다`() {
        val viewModel = createViewModel()
        viewModel.handleIntent(ProfileEditIntent.UpdateNickname("살까말까"))

        viewModel.handleIntent(ProfileEditIntent.Submit)

        assertTrue(viewModel.uiState.value.isNicknameDialogVisible)
        assertEquals(0, userRepository.updateCount)
    }

    @Test
    fun `확인_다이얼로그에서_취소하면_저장하지_않는다`() {
        val viewModel = createViewModel()
        viewModel.handleIntent(ProfileEditIntent.UpdateNickname("살까말까"))
        viewModel.handleIntent(ProfileEditIntent.Submit)

        viewModel.handleIntent(ProfileEditIntent.DismissNicknameDialog)

        assertFalse(viewModel.uiState.value.isNicknameDialogVisible)
        assertEquals(0, userRepository.updateCount)
    }

    @Test
    fun `확인_다이얼로그에서_변경하면_공백을_지운_닉네임만_저장하고_돌아간다`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.handleIntent(ProfileEditIntent.UpdateNickname(" 살까말까 "))
            viewModel.handleIntent(ProfileEditIntent.Submit)

            viewModel.handleIntent(ProfileEditIntent.ConfirmNicknameChange)

            assertEquals("살까말까", userRepository.updatedNickname)
            assertNull(userRepository.updatedProfileImage)
            assertEquals("살까말까", userPreferencesRepository.displayName)
            val sideEffects = viewModel.sideEffect.take(2).toList()
            assertEquals(
                listOf(
                    ProfileEditSideEffect.ShowSnackbar(message = "프로필을 수정했어요.", icon = BuyOrNotIcons.CheckCircle),
                    ProfileEditSideEffect.NavigateBackWithUpdate,
                ),
                sideEffects,
            )
        }

    @Test
    fun `이미지만_바꾸고_완료하면_다이얼로그_없이_바로_저장한다`() {
        val viewModel = createViewModel()
        viewModel.handleIntent(ProfileEditIntent.SelectImage("file:///cropped.jpg"))

        viewModel.handleIntent(ProfileEditIntent.Submit)

        assertFalse(viewModel.uiState.value.isNicknameDialogVisible)
        assertEquals(1, userRepository.updateCount)
        assertNull(userRepository.updatedNickname)
    }

    @Test
    fun `이미지를_S3에_올린_뒤_viewUrl로_프로필을_수정한다`() {
        val viewModel = createViewModel()
        viewModel.handleIntent(ProfileEditIntent.SelectImage("file:///cropped.jpg"))

        viewModel.handleIntent(ProfileEditIntent.Submit)

        assertEquals(listOf("read", "getPresignedUrl", "uploadImage", "updateProfile"), log.calls)
        assertEquals("https://s3/put", feedRepository.uploadedUrl)
        assertTrue(imageReader.bytes.contentEquals(feedRepository.uploadedBytes))
        assertEquals("https://cdn/new.jpg", userRepository.updatedProfileImage)
        assertEquals("https://cdn/new.jpg", userPreferencesRepository.profileImageUrl)
    }

    @Test
    fun `공백만_입력하고_완료하면_글자_수_에러를_보여준다`() {
        val viewModel = createViewModel()
        viewModel.handleIntent(ProfileEditIntent.UpdateNickname("   "))

        viewModel.handleIntent(ProfileEditIntent.Submit)

        assertEquals("최소 3자 이상 입력해주세요.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isNicknameDialogVisible)
        assertEquals(0, userRepository.updateCount)
    }

    @Test
    fun `중복_닉네임_에러_USER_011은_입력창_아래_문구로_보여준다`() {
        userRepository.updateError = ApiException(code = "USER_011", message = null)
        val viewModel = createViewModel()
        viewModel.handleIntent(ProfileEditIntent.UpdateNickname("살까말까"))
        viewModel.handleIntent(ProfileEditIntent.Submit)

        viewModel.handleIntent(ProfileEditIntent.ConfirmNicknameChange)

        assertEquals("이미 사용 중인 닉네임이에요.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `변경_주기_제한_에러_USER_013은_입력창_아래_문구로_보여준다`() {
        userRepository.updateError = ApiException(code = "USER_013", message = null)
        val viewModel = createViewModel()
        viewModel.handleIntent(ProfileEditIntent.UpdateNickname("살까말까"))
        viewModel.handleIntent(ProfileEditIntent.Submit)

        viewModel.handleIntent(ProfileEditIntent.ConfirmNicknameChange)

        assertEquals("닉네임은 20일마다 1번 수정할 수 있어요.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun `닉네임_정책과_무관한_실패는_스낵바로_알린다`() =
        runTest {
            userRepository.updateError = IllegalStateException("network")
            val viewModel = createViewModel()
            viewModel.handleIntent(ProfileEditIntent.SelectImage("file:///cropped.jpg"))

            viewModel.handleIntent(ProfileEditIntent.Submit)

            assertNull(viewModel.uiState.value.errorMessage)
            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(
                ProfileEditSideEffect.ShowSnackbar("프로필을 수정하지 못했어요. 다시 시도해주세요."),
                viewModel.sideEffect.first(),
            )
        }
}
