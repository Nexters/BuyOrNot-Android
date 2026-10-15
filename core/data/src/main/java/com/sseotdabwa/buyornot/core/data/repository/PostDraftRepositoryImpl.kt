package com.sseotdabwa.buyornot.core.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.Firebase
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import com.sseotdabwa.buyornot.core.data.datasource.RemoteConfigDataSource
import com.sseotdabwa.buyornot.core.data.draft.PostDraftParser
import com.sseotdabwa.buyornot.core.data.draft.PostDraftPrompt
import com.sseotdabwa.buyornot.core.datastore.AppPreferencesDataSource
import com.sseotdabwa.buyornot.domain.exception.PostDraftLimitExceededException
import com.sseotdabwa.buyornot.domain.model.FeedCategory
import com.sseotdabwa.buyornot.domain.model.PostDraft
import com.sseotdabwa.buyornot.domain.model.PostDraftSource
import com.sseotdabwa.buyornot.domain.repository.PostDraftRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// 하이브리드 추론(OnDeviceConfig·InferenceSource)이 Experimental API다.
@OptIn(PublicPreviewAPI::class)
@Singleton
class PostDraftRepositoryImpl @Inject constructor(
    private val appPreferencesDataSource: AppPreferencesDataSource,
    private val remoteConfigDataSource: RemoteConfigDataSource,
) : PostDraftRepository {
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = CLOUD_MODEL_NAME,
            generationConfig =
                generationConfig {
                    maxOutputTokens = MAX_OUTPUT_TOKENS
                    thinkingConfig = thinkingConfig { thinkingLevel = ThinkingLevel.MINIMAL }
                },
            // Nano를 쓸 수 없는 기기에서는 클라우드 모델로 자동 폴백한다.
            onDeviceConfig = OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE, maxOutputTokens = MAX_OUTPUT_TOKENS),
        )
    }

    override suspend fun generateDraft(
        image: ByteArray,
        category: FeedCategory?,
        price: Int?,
    ): PostDraft {
        if (!appPreferencesDataSource.tryConsumePostDraftQuota(LocalDate.now().toString(), DAILY_LIMIT)) {
            throw PostDraftLimitExceededException()
        }
        val bitmap = withContext(Dispatchers.Default) { decodeScaled(image) }
        val instruction = remoteConfigDataSource.fetchAiDraftInstruction()
        // 온디바이스 추론은 이미지 1장 + 텍스트만 받으므로 그 형태를 유지한다.
        val response =
            model.generateContent(
                content {
                    image(bitmap)
                    text(PostDraftPrompt.build(instruction, category, price))
                },
            )
        val (title, content) =
            PostDraftParser.parse(response.text.orEmpty())
                ?: throw IllegalStateException("AI 응답 형식이 올바르지 않습니다.")
        return PostDraft(
            title = title,
            content = content,
            source = if (response.inferenceSource == InferenceSource.ON_DEVICE) PostDraftSource.ON_DEVICE else PostDraftSource.CLOUD,
        )
    }

    private fun decodeScaled(bytes: ByteArray): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= MAX_IMAGE_SIDE) sampleSize *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: throw IllegalArgumentException("이미지를 읽을 수 없습니다.")
    }

    private companion object {
        const val CLOUD_MODEL_NAME = "gemini-3.1-flash-lite"
        const val MAX_OUTPUT_TOKENS = 256
        const val DAILY_LIMIT = 10

        // 업로드 사진은 수천 px라 그대로 넘기면 메모리를 크게 쓴다. 상품 식별에는 이 정도면 충분하다.
        const val MAX_IMAGE_SIDE = 768
    }
}
