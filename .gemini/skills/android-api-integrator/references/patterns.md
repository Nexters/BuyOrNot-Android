# Android API Integration Patterns

## 1. Network Layer (`core:network`)

### DTOs
- Use `@Serializable` from Kotlinx Serialization.
- Use `@SerialName` for JSON field mapping.
- Location: `core/network/src/main/java/com/sseotdabwa/buyornot/core/network/dto/`

```kotlin
@Serializable
data class MyRequest(
    @SerialName("title") val title: String
)

@Serializable
data class MyResponse(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String
)
```

### Retrofit Service
- Use `BaseResponse<T>` wrapper.
- Use `suspend` functions.
- Location: `core/network/src/main/java/com/sseotdabwa/buyornot/core/network/api/`

```kotlin
interface MyApiService {
    @POST("/api/v1/resource")
    suspend fun createResource(@Body request: MyRequest): BaseResponse<MyResponse>
}
```

## 2. Domain Layer (`domain`)

### Domain Model
- Pure Kotlin data class.
- Location: `domain/src/main/java/com/sseotdabwa/buyornot/domain/model/`

### Repository Interface
- Location: `domain/src/main/java/com/sseotdabwa/buyornot/domain/repository/`

```kotlin
interface MyRepository {
    suspend fun createResource(title: String): Resource
}
```

## 3. Data Layer (`core:data`)

### Repository Implementation
- Inject ApiService.
- Use `.getOrThrow()` on `BaseResponse`.
- Map DTO to Domain.
- Location: `core/data/src/main/java/com/sseotdabwa/buyornot/core/data/repository/`

```kotlin
class MyRepositoryImpl @Inject constructor(
    private val myApiService: MyApiService
) : MyRepository {
    override suspend fun createResource(title: String): Resource {
        return myApiService.createResource(MyRequest(title))
            .getOrThrow()
            .toDomain()
    }
}
```

## 4. DI Layer (`core:data` and `core:network`)

### DataModule
- Binds implementation to interface.
- Location: `core/data/src/main/java/com/sseotdabwa/buyornot/core/data/di/DataModule.kt`

### NetworkModule
- Provides Retrofit Service.
- Location: `core/network/src/main/java/com/sseotdabwa/buyornot/core/network/di/NetworkModule.kt`
