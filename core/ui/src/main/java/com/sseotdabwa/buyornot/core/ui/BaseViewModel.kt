package com.sseotdabwa.buyornot.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI 아키텍처를 위한 Base ViewModel
 *
 * UiState, Intent, SideEffect를 관리하는 기본 ViewModel 클래스입니다.
 *
 * @param S UiState 타입 - 화면의 상태를 나타냅니다.
 * @param I Intent 타입 - 사용자 액션이나 이벤트를 나타냅니다.
 * @param E SideEffect 타입 - 일회성 이벤트(네비게이션, 스낵바 등)를 나타냅니다.
 * @param initialState 초기 UiState 값
 *
 * @sample
 * ```kotlin
 * // 1. UiState, Intent, SideEffect 정의
 * data class HomeUiState(
 *     val isLoading: Boolean = false,
 *     val items: List<Item> = emptyList(),
 * )
 *
 * sealed interface HomeIntent {
 *     data object LoadItems : HomeIntent
 *     data class ItemClicked(val id: Long) : HomeIntent
 * }
 *
 * sealed interface HomeSideEffect {
 *     data class NavigateToDetail(val id: Long) : HomeSideEffect
 *     data class ShowSnackbar(val message: String) : HomeSideEffect
 * }
 *
 * // 2. ViewModel 구현
 * @HiltViewModel
 * class HomeViewModel @Inject constructor(
 *     private val getItemsUseCase: GetItemsUseCase,
 * ) : BaseViewModel<HomeUiState, HomeIntent, HomeSideEffect>(HomeUiState()) {
 *
 *     override fun handleIntent(intent: HomeIntent) {
 *         when (intent) {
 *             is HomeIntent.LoadItems -> loadItems()
 *             is HomeIntent.ItemClicked -> {
 *                 sendSideEffect(HomeSideEffect.NavigateToDetail(intent.id))
 *             }
 *         }
 *     }
 *
 *     private fun loadItems() {
 *         viewModelScope.launch {
 *             updateState { it.copy(isLoading = true) }
 *             getItemsUseCase()
 *                 .onSuccess { items ->
 *                     updateState { it.copy(isLoading = false, items = items) }
 *                 }
 *                 .onFailure {
 *                     updateState { it.copy(isLoading = false) }
 *                     sendSideEffect(HomeSideEffect.ShowSnackbar("로드 실패"))
 *                 }
 *         }
 *     }
 * }
 *
 * // 3. Composable에서 사용
 * @Composable
 * fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *
 *     LaunchedEffect(Unit) {
 *         viewModel.sideEffect.collect { effect ->
 *             when (effect) {
 *                 is HomeSideEffect.NavigateToDetail -> { /* 네비게이션 처리 */ }
 *                 is HomeSideEffect.ShowSnackbar -> { /* 스낵바 표시 */ }
 *             }
 *         }
 *     }
 *
 *     // UI 구현
 * }
 * ```
 */
abstract class BaseViewModel<S, I, E>(
    initialState: S,
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _sideEffect = Channel<E>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    abstract fun handleIntent(intent: I)

    protected fun updateState(reducer: (S) -> S) {
        _uiState.update(reducer)
    }

    protected fun sendSideEffect(effect: E) {
        viewModelScope.launch {
            _sideEffect.send(effect)
        }
    }

    protected val currentState: S
        get() = _uiState.value
}
