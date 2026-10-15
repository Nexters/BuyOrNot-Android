package com.sseotdabwa.buyornot.core.data.draft

import com.sseotdabwa.buyornot.domain.model.FeedCategory
import java.text.DecimalFormat

internal object PostDraftPrompt {
    const val MAX_TITLE_LENGTH = 40
    const val MAX_CONTENT_LENGTH = 100

    /** Remote Config `ai_draft_instruction`의 기본값. 콘솔에서는 역할·작성 규칙만 바꾼다. */
    val DEFAULT_INSTRUCTION =
        """
        너는 쇼핑 고민 투표 앱 '살까말까'의 글쓰기 도우미야.
        사진 속 상품을 살지 말지 고민하는 사용자가 되어 투표 글 초안을 한국어로 써줘.

        [작성 규칙]
        - 제목: 어떤 상품인지 알 수 있게 짧게 쓴다.
        - 내용: 왜 사고 싶은지와 무엇이 망설여지는지를 1~2문장으로 쓴다.
        - 친근한 존댓말(~요)을 쓰고, 이모지·해시태그는 쓰지 않는다.
        - 브랜드나 모델명을 확신할 수 없으면 지어내지 말고 일반적인 이름으로 쓴다.
        """.trimIndent()

    // 상품 정보와 출력 형식은 파서가 의존하므로 원격으로 바꾸지 않는다.
    // 온디바이스(Gemini Nano)는 구조화 출력을 지원하지 않아 라벨 형식의 텍스트로 받는다.
    fun build(
        instruction: String,
        category: FeedCategory?,
        price: Int?,
    ): String =
        buildString {
            appendLine(instruction.trim())
            appendLine()
            appendLine("[상품 정보]")
            category?.let { appendLine("- 카테고리: ${it.displayName}") }
            price?.let { appendLine("- 가격: ${DecimalFormat("#,###").format(it)}원") }
            appendLine("- 사진: 첨부한 이미지")
            appendLine()
            appendLine("[출력 형식] 아래 두 줄만 출력한다. 제목은 ${MAX_TITLE_LENGTH}자, 내용은 ${MAX_CONTENT_LENGTH}자 이내.")
            appendLine("제목: <제목>")
            append("내용: <내용>")
        }
}
