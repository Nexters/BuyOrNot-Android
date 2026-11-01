package com.sseotdabwa.buyornot.feature.home.ui

internal data class CreateNudgeAnchorObservation(
    val isAnchorVisible: Boolean,
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
    val content: Any?,
)

/**
 * 등록 넛지 경계를 "사용자가 스크롤해서 마주쳤는지" 판정한다.
 *
 * [observe]가 `true`를 돌려주면 넛지를 띄울 시점이다.
 */
internal class CreateNudgeAnchorTracker {
    private var previous: CreateNudgeAnchorObservation? = null

    // 경계가 안 보이는 동안 스크롤이 있었다. 헤더가 접히면서 스크롤 위치 변화 없이 경계가 드러나는 경우를 위해 남겨둔다.
    private var scrolledWhileHidden = false

    fun observe(current: CreateNudgeAnchorObservation): Boolean {
        val prev = previous
        previous = current
        if (prev == null) return false

        // 목록이 바뀌면 LazyList가 아이템 key로 위치를 옮기므로, 이때의 위치 변화는 스크롤이 아니다.
        val contentChanged = prev.content != current.content
        val scrolled =
            !contentChanged &&
                (
                    prev.firstVisibleItemIndex != current.firstVisibleItemIndex ||
                        prev.firstVisibleItemScrollOffset != current.firstVisibleItemScrollOffset
                )
        if (contentChanged) scrolledWhileHidden = false

        if (!current.isAnchorVisible) {
            if (scrolled) scrolledWhileHidden = true
            return false
        }

        val reached = scrolledWhileHidden || (scrolled && !prev.isAnchorVisible)
        scrolledWhileHidden = false
        return reached
    }
}
