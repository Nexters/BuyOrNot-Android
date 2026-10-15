package com.sseotdabwa.buyornot.core.data.draft

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PostDraftParserTest {
    @Test
    fun `라벨 형식 응답에서 제목과 내용을 꺼낸다`() {
        val result = PostDraftParser.parse("제목: 나이키 에어포스 1\n내용: 데일리로 신기 좋을 것 같은데 비슷한 신발이 있어서 고민돼요.")

        assertEquals("나이키 에어포스 1" to "데일리로 신기 좋을 것 같은데 비슷한 신발이 있어서 고민돼요.", result)
    }

    @Test
    fun `마크다운 강조와 따옴표를 걷어낸다`() {
        val result = PostDraftParser.parse("**제목:** \"무선 이어폰\"\n**내용**: 음질이 좋다는데 가격이 부담돼요.")

        assertEquals("무선 이어폰" to "음질이 좋다는데 가격이 부담돼요.", result)
    }

    @Test
    fun `여러 줄 내용은 한 줄로 합친다`() {
        val result = PostDraftParser.parse("제목: 캠핑 의자\n내용: 가볍고 튼튼해 보여요.\n그런데 자주 쓸지 모르겠어요.")

        assertEquals("가볍고 튼튼해 보여요. 그런데 자주 쓸지 모르겠어요.", result?.second)
    }

    @Test
    fun `글자 수 제한을 넘으면 자른다`() {
        val result = PostDraftParser.parse("제목: ${"가".repeat(50)}\n내용: ${"나".repeat(150)}")

        assertEquals(PostDraftPrompt.MAX_TITLE_LENGTH, result?.first?.length)
        assertEquals(PostDraftPrompt.MAX_CONTENT_LENGTH, result?.second?.length)
    }

    @Test
    fun `내용이 없으면 빈 문자열로 둔다`() {
        assertEquals("향수" to "", PostDraftParser.parse("제목: 향수"))
    }

    @Test
    fun `제목을 찾지 못하면 null을 반환한다`() {
        assertNull(PostDraftParser.parse("죄송하지만 이미지를 인식할 수 없어요."))
    }
}
