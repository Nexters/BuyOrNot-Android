# Compose and Preview Guidelines

## Compose Best Practices

- **State Hoisting**: Elevate state to the ViewModel or parent Composable to make components testable and reusable.
- **Stable Types**: Use `@Stable` or `@Immutable` annotations for UI models to optimize recomposition.
- **Design System Tokens**: Always use tokens from `core:designsystem` (e.g., `BuyOrNotTheme.colors`, `BuyOrNotTheme.typography`) instead of hardcoding values.
- **ViewModel Usage**: UI in `feature:*` modules should interact with `ViewModel` to manage state and handle events.
- **Navigation**: Define `Route` objects for navigation within each `feature` module.

## Preview Generation Patterns

- **Theme Wrapper**: Always wrap `@Preview` functions in the project's theme (e.g., `BuyOrNotTheme`).
- **Surface**: Use a `Surface` with `color = BuyOrNotTheme.colors.background` for accurate preview backgrounds.
- **Multiple Previews**: Provide previews for:
  - Dark Mode: `@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)`
  - Different Screen Sizes: `@Preview(device = Devices.PIXEL_7)`
- **Sample Data**: Use hardcoded sample data for previews to avoid complex mocking.

### Preview Template

```kotlin
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MyComponentPreview() {
    BuyOrNotTheme {
        Surface(color = BuyOrNotTheme.colors.background) {
            MyComponent(
                state = MyState(
                    title = "Sample Title",
                    description = "This is a sample description."
                ),
                onEvent = {}
            )
        }
    }
}
```
