package com.sseotdabwa.buyornot.core.ui.nickname

/**
 * 닉네임 입력 정책
 *
 * 형식·중복 검사는 서버가 [시작하기]/[완료] 시점에 한 번 수행하고, 앱은 글자 수 제한과
 * 서버 에러 코드를 문구로 바꾸는 일만 맡는다. 회원가입 닉네임 설정과 프로필 수정이 함께 쓴다.
 */
object NicknamePolicy {
    const val MAX_LENGTH = 10

    /** 10자를 넘는 입력은 받지 않는다. 붙여넣기로 넘치면 앞 10자만 남긴다. */
    fun limit(input: String): String = input.take(MAX_LENGTH)

    /**
     * 서버 닉네임 에러 코드를 입력창 아래에 보여줄 문구로 바꾼다.
     *
     * @return 닉네임 정책 에러가 아니면 null
     */
    fun errorMessageOf(code: String?): String? =
        when (code) {
            "USER_007" -> "특수문자는 사용할 수 없어요."
            "USER_008" -> "띄어쓰기는 사용할 수 없어요."
            "USER_009" -> "최소 3자 이상 입력해주세요."
            "USER_010" -> "한글, 영문, 숫자를 조합해 입력해주세요."
            "USER_011" -> "이미 사용 중인 닉네임이에요."
            "USER_012" -> "사용할 수 없는 닉네임이에요."
            "USER_014" -> "최대 10자까지 입력할 수 있어요."
            else -> null
        }
}
