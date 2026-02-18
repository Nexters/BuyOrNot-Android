# Advanced API Integration Patterns

## 1. Paging 3 Integration

For endpoints that return a list and support pagination.

### Network Layer
- Use `page` and `size` parameters.
- DTO should represent a single page response.

```kotlin
@GET("/api/v1/posts")
suspend fun getPosts(
    @Query("page") page: Int,
    @Query("size") size: Int
): BaseResponse<List<PostDto>>
```

### Data Layer (PagingSource)
- Implement `PagingSource<Int, DomainModel>`.
- Use the repository to fetch data.

## 2. Token Handling & Post-processing

For authentication APIs (Login, Signup, Refresh).

### Repository Implementation
- Inject `UserPreferencesDataSource`.
- Update tokens after a successful API call.

```kotlin
override suspend fun login(idToken: String) {
    val response = authApiService.login(LoginRequest(idToken)).getOrThrow()
    userPreferencesDataSource.updateTokens(
        accessToken = response.accessToken,
        refreshToken = response.refreshToken
    )
}
```

## 3. Error Handling

- Use `ErrorCode` from `BaseResponse`.
- Use `runCatchingCancellable` for safe API calls.
- Map custom error codes to domain-specific exceptions if necessary.
