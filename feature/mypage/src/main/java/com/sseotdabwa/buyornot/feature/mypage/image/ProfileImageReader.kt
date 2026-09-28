package com.sseotdabwa.buyornot.feature.mypage.image

import android.content.Context
import android.net.Uri
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 업로드할 프로필 이미지 파일
 *
 * @property fileName presigned URL 발급에 쓰는 파일명
 * @property contentType S3 업로드 요청의 Content-Type
 */
class ProfileImageFile(
    val bytes: ByteArray,
    val fileName: String,
    val contentType: String,
)

/**
 * 기기에 있는 이미지를 업로드용 바이트로 읽는다.
 *
 * ViewModel이 Context를 직접 들고 있지 않도록 분리해 단위 테스트에서 가짜로 바꿔 끼운다.
 */
interface ProfileImageReader {
    /**
     * @param uri 자르기 화면이 돌려준 이미지 URI 문자열
     * @throws IllegalStateException 파일을 읽을 수 없는 경우
     */
    suspend fun read(uri: String): ProfileImageFile
}

internal class ContentResolverProfileImageReader @Inject constructor(
    @ApplicationContext private val context: Context,
) : ProfileImageReader {
    override suspend fun read(uri: String): ProfileImageFile =
        withContext(Dispatchers.IO) {
            val parsed = Uri.parse(uri)
            val bytes =
                context.contentResolver.openInputStream(parsed)?.use { it.readBytes() }
                    ?: error("파일을 읽을 수 없습니다.")
            ProfileImageFile(
                bytes = bytes,
                fileName = parsed.lastPathSegment ?: DEFAULT_FILE_NAME,
                // 자르기 화면은 항상 JPEG로 저장한다.
                contentType = context.contentResolver.getType(parsed) ?: DEFAULT_CONTENT_TYPE,
            )
        }

    private companion object {
        const val DEFAULT_FILE_NAME = "profile_image.jpg"
        const val DEFAULT_CONTENT_TYPE = "image/jpeg"
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProfileImageModule {
    @Binds
    abstract fun bindProfileImageReader(impl: ContentResolverProfileImageReader): ProfileImageReader
}
