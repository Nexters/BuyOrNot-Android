package com.sseotdabwa.buyornot.domain.model

/**
 * 사용자 유형
 */
enum class UserType {
    /** 로그인하지 않은 상태. 앱에 비회원 이용은 없고, 저장된 로그인 정보가 없거나 로그아웃했을 때의 값이다. */
    GUEST,

    /** 소셜 로그인 사용자 (구글/카카오) */
    SOCIAL,
}
