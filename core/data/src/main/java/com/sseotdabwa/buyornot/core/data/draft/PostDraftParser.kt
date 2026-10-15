package com.sseotdabwa.buyornot.core.data.draft

import com.sseotdabwa.buyornot.core.data.draft.PostDraftPrompt.MAX_CONTENT_LENGTH
import com.sseotdabwa.buyornot.core.data.draft.PostDraftPrompt.MAX_TITLE_LENGTH

internal object PostDraftParser {
    // 마크다운 강조(**제목:**) 등 라벨 앞뒤의 기호를 허용한다. \W는 한글도 포함하므로 쓰지 않는다.
    private val TITLE_LABEL = Regex("""^[^\p{L}\p{N}]*제목[^\p{L}\p{N}:：]*[:：]""")
    private val CONTENT_LABEL = Regex("""^[^\p{L}\p{N}]*내용[^\p{L}\p{N}:：]*[:：]""")
    private const val WRAPPING_MARKS = "\"'“”‘’*` "

    /** 모델 응답에서 제목·내용을 꺼내 글자 수 제한에 맞춘다. 제목을 찾지 못하면 null. */
    fun parse(text: String): Pair<String, String>? {
        var title: String? = null
        val contentLines = mutableListOf<String>()
        var inContent = false

        text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
            when {
                title == null && TITLE_LABEL.containsMatchIn(line) -> title = line.replace(TITLE_LABEL, "")
                CONTENT_LABEL.containsMatchIn(line) -> {
                    inContent = true
                    contentLines += line.replace(CONTENT_LABEL, "")
                }
                inContent -> contentLines += line
            }
        }

        val cleanedTitle = title?.clean()?.take(MAX_TITLE_LENGTH)?.trim()
        if (cleanedTitle.isNullOrEmpty()) return null
        val cleanedContent =
            contentLines
                .joinToString(" ")
                .clean()
                .take(MAX_CONTENT_LENGTH)
                .trim()
        return cleanedTitle to cleanedContent
    }

    private fun String.clean(): String = trim { it.isWhitespace() || it in WRAPPING_MARKS }
}
