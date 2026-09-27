package com.sseotdabwa.buyornot.core.ui.nickname

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NicknamePolicyTest {
    @Test
    fun `10자_이하_입력은_그대로_유지한다`() {
        assertEquals("살까말까고밍", NicknamePolicy.limit("살까말까고밍"))
    }

    @Test
    fun `10자를_넘는_입력은_앞_10자만_남긴다`() {
        assertEquals("가나다라마바사아자차", NicknamePolicy.limit("가나다라마바사아자차카타"))
    }

    @Test
    fun `닉네임_정책_에러_코드는_정책_문구로_바꾼다`() {
        assertEquals("특수문자는 사용할 수 없어요.", NicknamePolicy.errorMessageOf("USER_007"))
        assertEquals("띄어쓰기는 사용할 수 없어요.", NicknamePolicy.errorMessageOf("USER_008"))
        assertEquals("최소 3자 이상 입력해주세요.", NicknamePolicy.errorMessageOf("USER_009"))
        assertEquals("한글, 영문, 숫자를 조합해 입력해주세요.", NicknamePolicy.errorMessageOf("USER_010"))
        assertEquals("이미 사용 중인 닉네임이에요.", NicknamePolicy.errorMessageOf("USER_011"))
        assertEquals("사용할 수 없는 닉네임이에요.", NicknamePolicy.errorMessageOf("USER_012"))
        assertEquals("최대 10자까지 입력할 수 있어요.", NicknamePolicy.errorMessageOf("USER_014"))
    }

    @Test
    fun `닉네임_정책과_무관한_에러_코드는_null을_반환한다`() {
        assertNull(NicknamePolicy.errorMessageOf("USER_006"))
        assertNull(NicknamePolicy.errorMessageOf(null))
    }
}
