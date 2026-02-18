# MVI Architecture Guidelines

The project uses a strict MVI (Model-View-Intent) architecture based on `core:ui`'s `BaseViewModel`.

## 1. MVI Components

- **UiState (S)**: A `data class` representing the single source of truth for the UI state.
- **Intent (I)**: A `sealed interface` representing user actions or events that trigger logic.
- **SideEffect (E)**: A `sealed interface` for one-time events like Navigation, Snackbar, or Toast.

## 2. Contract Definition (`Contract.kt`)

Always define the MVI components in a separate file (e.g., `LoginContract.kt`) or at the top of the ViewModel file if small.

```kotlin
data class MyUiState(
    val isLoading: Boolean = false,
    val data: List<String> = emptyList()
)

sealed interface MyIntent {
    data object LoadData : MyIntent
    data class OnItemClick(val id: String) : MyIntent
}

sealed interface MySideEffect {
    data class ShowSnackbar(val message: String) : MySideEffect
    data object NavigateToNext : MySideEffect
}
```

## 3. ViewModel Implementation

- Inherit from `BaseViewModel<S, I, E>`.
- Override `handleIntent(intent: I)`.
- Use `updateState { ... }` to modify state.
- Use `sendSideEffect(E)` for one-time events.

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor() :
    BaseViewModel<MyUiState, MyIntent, MySideEffect>(MyUiState()) {

    override fun handleIntent(intent: MyIntent) {
        when (intent) {
            is MyIntent.LoadData -> { /* 로직 수행 */ }
            // ...
        }
    }
}
```

## 4. Composable Usage

- Use `collectAsStateWithLifecycle()` to observe `uiState`.
- Use `LaunchedEffect` to collect `sideEffect`.

```kotlin
@Composable
fun MyRoute(viewModel: MyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is MySideEffect.ShowSnackbar -> { /* 스낵바 표시 */ }
                // ...
            }
        }
    }

    MyScreen(uiState = uiState, onIntent = viewModel::handleIntent)
}
```
