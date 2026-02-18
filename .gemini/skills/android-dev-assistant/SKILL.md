---
name: android-dev-assistant
description: Code review and preview generation for BuyOrNot-Android project. Use when reviewing pull requests, generating Compose previews, or ensuring architectural compliance with modularization and tech stack (Hilt, Compose 2.3.0, Kotlin 2.3.0).
---

# Android Dev Assistant

## Overview

The `android-dev-assistant` skill provides specialized knowledge for the **BuyOrNot-Android** project. It focuses on maintaining architectural boundaries (e.g., pure Kotlin `domain`), ensuring consistent Hilt and Compose usage, and generating high-quality Jetpack Compose previews using the project's design system.

## Task Category 1: Code Review

Use this skill when performing a code review to ensure adherence to project standards.

### Architectural Compliance
- **Module Boundaries**: Verify that modules depend only on allowed layers. See [modularization.md](references/modularization.md).
- **Domain Layer**: Ensure `domain` has NO Android dependencies (e.g., `Context`, `Intent`, `ViewModel`).
- **Feature Layer**: Ensure UI logic is in `feature:*` and uses `ViewModel` and `Compose UI`.
- **Hilt Setup**: Check for correct Hilt annotations (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`).

### Compose & UI
- **Design System**: Ensure all colors, fonts, and dimensions are sourced from `core:designsystem`.
- **State Hoisting**: Check for proper state management in Composables.
- **ktlint**: Verify code matches `ktlint` standards (v14.0.1).

## Task Category 3: MVI Architecture

Use this skill to ensure the ViewModel follows the project's MVI pattern.

### MVI Guidelines
- **BaseViewModel**: Ensure ViewModel inherits from `BaseViewModel<S, I, E>`.
- **Intent-Driven**: Logic must be triggered via `handleIntent(I)`.
- **Contract.kt**: Check if `UiState`, `Intent`, and `SideEffect` are correctly defined.
- **SideEffect**: Use `sendSideEffect(E)` for navigation, snackbars, and one-time events.
- **Reference**: See [mvi.md](references/mvi.md) for architecture rules.

## Task Category 4: Preview Generation
...
## References
- **[Modularization and Tech Stack](references/modularization.md)**: Detailed project structure and dependency list.
- **[Compose and Preview Guidelines](references/compose.md)**: UI best practices and preview templates.
- **[MVI Architecture Guidelines](references/mvi.md)**: Rules for BaseViewModel and Contract definition.
