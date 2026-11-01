package com.sseotdabwa.buyornot.domain.model

/**
 * 사용자 유형
 */
enum class UserType {
    /** 저장된 로그인 정보가 없거나 로그아웃한 상태. 앱에는 비회원 이용이 없다. */
    NOT_LOGGED_IN,

    /** 소셜 로그인 사용자 (구글/카카오) */
    SOCIAL,
}
