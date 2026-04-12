package com.sseotdabwa.buyornot.feature.upload.util

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinkValidatorTest {
    @Test
    fun `빈 문자열은 유효하다`() {
        assertTrue(LinkValidator.isValid(""))
    }

    @Test
    fun `http로 시작하는 정상 URL은 유효하다`() {
        assertTrue(LinkValidator.isValid("http://naver.com"))
    }

    @Test
    fun `https로 시작하는 정상 URL은 유효하다`() {
        assertTrue(LinkValidator.isValid("https://naver.com"))
    }

    @Test
    fun `https로 시작하는 경로가 포함된 URL은 유효하다`() {
        assertTrue(LinkValidator.isValid("https://www.naver.com/search?q=test"))
    }

    @Test
    fun `스킴이 없는 URL은 유효하지 않다`() {
        assertFalse(LinkValidator.isValid("naver.com"))
    }

    @Test
    fun `www로만 시작하는 URL은 유효하지 않다`() {
        assertFalse(LinkValidator.isValid("www.naver.com"))
    }

    @Test
    fun `ttps로 시작해 스킴 철자가 틀린 URL은 유효하지 않다`() {
        assertFalse(LinkValidator.isValid("ttps://naver.com"))
    }

    @Test
    fun `http에 콜론이 없는 URL은 유효하지 않다`() {
        assertFalse(LinkValidator.isValid("http//naver.com"))
    }

    @Test
    fun `https에 슬래시가 없는 URL은 유효하지 않다`() {
        assertFalse(LinkValidator.isValid("https:naver.com"))
    }

    @Test
    fun `도메인에 한글이 포함된 URL은 유효하다`() {
        assertTrue(LinkValidator.isValid("https://네이버.com"))
    }

    @Test
    fun `경로에 한글이 포함된 URL은 유효하다`() {
        assertTrue(LinkValidator.isValid("https://naver.com/한글경로"))
    }

    @Test
    fun `URL 중간에 공백이 있으면 유효하지 않다`() {
        assertFalse(LinkValidator.isValid("https://naver .com"))
    }

    @Test
    fun `URL 앞에 공백이 있으면 유효하지 않다`() {
        assertFalse(LinkValidator.isValid(" https://naver.com"))
    }

    @Test
    fun `URL 뒤에 공백이 있으면 유효하지 않다`() {
        assertFalse(LinkValidator.isValid("https://naver.com "))
    }
}
