package com.sseotdabwa.buyornot.core.data.draft

import com.sseotdabwa.buyornot.domain.model.FeedCategory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PostDraftPromptTest {
    @Test
    fun `원격 지시문을 바꿔도 출력 형식은 유지된다`() {
        val prompt = PostDraftPrompt.build(instruction = "반말로 짧게 써줘.", category = null, price = null)

        assertTrue(prompt.startsWith("반말로 짧게 써줘."))
        assertTrue(prompt.endsWith("제목: <제목>\n내용: <내용>"))
    }

    @Test
    fun `카테고리와 가격은 입력된 것만 넣는다`() {
        val prompt = PostDraftPrompt.build(PostDraftPrompt.DEFAULT_INSTRUCTION, FeedCategory.FASHION, 129000)

        assertTrue(prompt.contains("- 카테고리: 패션 ∙ 잡화"))
        assertTrue(prompt.contains("- 가격: 129,000원"))
        assertFalse(PostDraftPrompt.build(PostDraftPrompt.DEFAULT_INSTRUCTION, null, null).contains("- 가격"))
    }
}
