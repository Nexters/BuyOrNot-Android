---
name: android-api-integrator
description: Automates Android API integration from specification files. Use when you receive a markdown file (e.g., apiSpecification.md) describing REST endpoints and need to implement the full network/domain/data flow.
---

# Android API Integrator

## Overview

This skill automates the multi-layer implementation required for a new API endpoint. It covers DTO creation, Retrofit service updates, Domain layer models/interfaces, and Data layer implementations (including mapping and DI).

## Workflow

1. **Read & Analyze**: 
    - Read the provided API specification (endpoint, method, request/response body).
    - Identify which module/feature it belongs to (e.g., `feature:mypage` -> `UserApiService`, `UserRepository`).
2. **Implement Network Layer (`core:network`)**:
    - Create/Update DTOs (Request/Response) using `@Serializable`.
    - Add the `@POST/@GET/@DELETE` method to the appropriate `ApiService` interface.
    - Always return `BaseResponse<T>`.
3. **Implement Domain Layer (`domain`)**:
    - Create/Update pure Kotlin Domain models.
    - Add the suspend function to the `Repository` interface.
4. **Implement Data Layer (`core:data`)**:
    - Add the implementation to the `RepositoryImpl`.
    - Use `.getOrThrow()` to handle API errors.
    - Implement the mapping logic (`toDomain()`) from DTO to Domain model.
5. **Update Hilt DI**:
    - If a new `ApiService` or `Repository` is created, ensure it's registered in `NetworkModule` and `DataModule`.
6. **Advanced Handling**:
    - For list endpoints, consider **Paging 3** implementation as shown in [advanced_patterns.md](references/advanced_patterns.md).
    - For Auth APIs, implement **Token post-processing** using `UserPreferencesDataSource`.

## Key Patterns
- **BaseResponse**: All Retrofit calls MUST use `BaseResponse<T>` and call `.getOrThrow()` in the repository.
- **Pure Domain**: The `domain` module must have NO Android or Network (Serialization) dependencies.
- **Mapping**: Data layer is responsible for mapping Network DTOs to Domain models.

## Reference
- **[Integration Patterns](references/patterns.md)**: Detailed code examples and locations.
- **[Advanced Integration Patterns](references/advanced_patterns.md)**: Paging, Token handling, and Error handling.
