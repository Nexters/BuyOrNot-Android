# 업로드 및 피드 생성 API 연동 계획

## 목표
`feature/upload` 모듈에서 이미지 업로드(Pre-signed URL)와 피드 생성을 구현합니다.
1. 이미지 업로드를 위한 Pre-signed URL을 발급받습니다 (`/api/v1/uploads/presigned-put`).
2. 발급받은 URL로 이미지를 업로드합니다 (별도 OkHttp 호출).
3. 업로드 성공 후 반환된 `s3ObjectKey`를 포함하여 피드를 생성합니다 (`/api/v1/feeds`).

## 1. Network Layer (`core:network`)

### DTO 정의
- **위치:** `core/network/src/main/java/com/sseotdabwa/buyornot/core/network/dto/`
- `PresignedUrlRequest`, `PresignedUrlResponse`: S3 업로드 URL 요청/응답
- `FeedRequest`, `FeedResponse`: 피드 생성 요청/응답

### API Service 정의
- **위치:** `core/network/src/main/java/com/sseotdabwa/buyornot/core/network/api/FeedApiService.kt`
```kotlin
interface FeedApiService {
    @POST("/api/v1/uploads/presigned-put")
    suspend fun getPresignedUrl(@Body request: PresignedUrlRequest): BaseResponse<PresignedUrlResponse>

    @POST("/api/v1/feeds")
    suspend fun createFeed(@Body request: FeedRequest): BaseResponse<FeedResponse>
}
```

## 2. Domain Layer (`domain`)

### Model 정의
- **위치:** `domain/src/main/java/com/sseotdabwa/buyornot/domain/model/`
- `UploadInfo`: `uploadUrl`, `s3ObjectKey` 등을 포함하는 모델
- `FeedCategory`: `LUXURY`, `FASHION` 등을 정의하는 Enum

### Repository Interface
- **위치:** `domain/src/main/java/com/sseotdabwa/buyornot/domain/repository/FeedRepository.kt`
```kotlin
interface FeedRepository {
    suspend fun getPresignedUrl(fileName: String, contentType: String): UploadInfo
    suspend fun createFeed(
        category: String,
        price: Int,
        content: String,
        s3ObjectKey: String,
        width: Int,
        height: Int
    ): Long
}
```

## 3. Data Layer (`core:data`)

### Repository Implementation
- **위치:** `core/data/src/main/java/com/sseotdabwa/buyornot/core/data/repository/FeedRepositoryImpl.kt`
- `FeedApiService`를 주입받아 구현.
- `BaseResponse.getOrThrow()`를 사용하여 에러 핸들링.
- DTO -> Domain 모델 매핑 로직 포함.

## 4. DI Setup

- **NetworkModule**: `FeedApiService` 제공 로직 추가.
- **DataModule**: `FeedRepository` 바인딩 추가.

## 5. Feature Layer (`feature:upload`)

### File Information Extraction
이미지 선택 후 `Uri`로부터 API 요청에 필요한 정보를 추출합니다.
- **`contentType`**: `contentResolver.getType(uri)`를 사용하여 MIME 타입을 가져옵니다 (예: `image/jpeg`).
- **`fileName`**: `contentResolver`를 통해 `OpenableColumns.DISPLAY_NAME`을 쿼리하여 원본 파일명을 가져옵니다.

### Implementation Strategy
- **UploadViewModel**: 
    - `Intent.UploadImage(uri)` 수신 시 `ContentResolver`를 사용하여 `fileName`과 `contentType`을 추출합니다.
    - 추출된 정보를 바탕으로 `FeedRepository.getPresignedUrl`을 호출합니다.
    - 반환된 `uploadUrl`로 이미지 바이너리(ByteArray 또는 InputStream)를 업로드합니다.
    - 업로드 성공 후 반환된 `s3ObjectKey`를 사용하여 `createFeed`를 호출합니다.
- **MVI Contract**: `UploadUiState`, `UploadIntent`, `UploadSideEffect` 정의.
