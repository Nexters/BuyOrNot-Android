# Modularization and Tech Stack

## Module Structure

The project follows a modularized structure defined in `docs/Modularization.md`.

- **app**: Application entry point, Hilt setup, and global navigation. No business logic.
- **feature:* **: Screen-level modules (e.g., `auth`, `home`, `upload`). Contains Compose UI, ViewModels, and Navigation routes.
- **domain**: Pure Kotlin module. Contains Domain Models, Repository interfaces, and Business Rules. **No Android dependencies.**
- **core:data**: Repository implementations, Retrofit/OkHttp setup, and data access.
- **core:network**: Network communication layer (Retrofit/OkHttp).
- **core:datastore**: Persistence layer (Jetpack DataStore).
- **core:ui**: UI common logic and shared UI utilities.
- **core:designsystem**: Design tokens (Color, Typography, Spacing) and common UI components.

## Tech Stack

- **Language**: Kotlin 2.3.0
- **UI Framework**: Jetpack Compose (BOM 2026.01.00)
- **Dependency Injection**: Hilt 2.58
- **Networking**: Retrofit 3.0.0, OkHttp 5.3.2, Kotlinx Serialization Json 1.9.0
- **Image Loading**: Coil 2.7.0
- **Navigation**: Navigation Compose 2.8.2
- **Persistence**: DataStore 1.2.0
- **Animations**: Lottie Compose 6.7.1
- **Code Quality**: ktlint 14.0.1

## Conventions

- Use **build-logic** convention plugins for Gradle configuration.
- Feature modules depend on `domain` and `core:*`.
- `app` module depends on `feature:*` and `core:*`.
- Avoid direct dependencies between `feature` modules.
