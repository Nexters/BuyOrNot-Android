package com.sseotdabwa.buyornot.core.analytics

sealed class AnalyticsEvent {
    data class FeedViewed(
        val firstVisibleItemIndex: Int,
    ) : AnalyticsEvent()

    data class FeedExited(
        val timeSpentSeconds: Float,
        val lastVisibleItemIndex: Int,
    ) : AnalyticsEvent()

    data class VoteSubmitted(
        val feedId: Long,
        val voteChoice: String,
        val feedCategory: String,
    ) : AnalyticsEvent()

    data class VoteCreateStarted(
        val entrySource: String,
        val isLoggedIn: Boolean,
    ) : AnalyticsEvent()

    /**
     * @param writingMode [WritingMode] 값. AI 도입 전후·방식별 완료율을 나눠 보는 축이다.
     * @param aiEditedFields AI 초안이 채운 필드 중 게시 전에 사용자가 고친 것. AI로 쓰지 않았으면 null.
     */
    data class VoteCreateCompleted(
        val itemId: Long,
        val voteTitle: String,
        val optionCount: Int,
        val writingMode: String,
        val aiEditedFields: List<String>?,
    ) : AnalyticsEvent()

    /**
     * 입력이 하나라도 있는 상태에서 작성 화면을 떠남. 앨범 단계에서는 사진을 하나 이상 골랐을 때만 발행한다.
     *
     * @param step [Step] 값. 떠난 단계.
     * @param writingMode 앨범에서 확인하기 전에 떠났으면 null.
     */
    data class VoteCreateAbandoned(
        val filledFields: List<String>,
        val lastStep: String?,
        val step: String,
        val writingMode: String?,
    ) : AnalyticsEvent() {
        object Step {
            const val ALBUM = "album"
            const val FORM = "form"
        }
    }

    /** 앨범 화면에서 사진을 고르고 [확인]을 누름. 이 시점에 AI로 쓸지가 정해진다. */
    data class VoteCreatePhotosConfirmed(
        val imageCount: Int,
        val useAi: Boolean,
    ) : AnalyticsEvent()

    /**
     * AI 초안 요청이 끝남. 사진 업로드부터 초안 응답까지를 한 번으로 센다.
     *
     * @param result [Result] 값.
     * @param filledFields 초안이 실제로 채운 필드. 성공했을 때만 있다.
     */
    data class AiDraftCompleted(
        val result: String,
        val imageCount: Int,
        val durationMs: Long,
        val filledFields: List<String>?,
    ) : AnalyticsEvent() {
        object Result {
            const val SUCCESS = "success"

            /** 응답은 왔지만 제목과 내용이 모두 비어 있었다. */
            const val EMPTY = "empty"
            const val FAILED = "failed"

            /** 사용자가 기다리다 뒤로가기로 그만뒀다. */
            const val CANCELLED = "cancelled"
        }
    }

    object WritingMode {
        const val AI = "ai"

        /** AI를 켰지만 초안을 받지 못해 직접 썼다. */
        const val AI_FAILED = "ai_failed"
        const val MANUAL = "manual"
    }

    /**
     * 푸시 알림 탭. 유입 분석의 시작점이며 전환은 Mixpanel Funnel의 시간 순서로 잇는다.
     *
     * 마케팅 알림은 [feedId]·[notificationId]가 없으므로 null로 들어오고,
     * 이때는 Mixpanel 속성 자체를 생략한다.
     */
    data class PushOpened(
        val pushType: String,
        val feedId: Long?,
        val notificationId: Long?,
    ) : AnalyticsEvent()

    /**
     * 앱 링크(`https://{host}/feed/{feedId}`) 탭으로 앱이 열림.
     *
     * [PushOpened]와 절대 섞지 않는다 — 앱 링크 유입을 푸시 이벤트에 태우면
     * 푸시 CTR과 퍼널 전환율이 통째로 오염된다.
     *
     * 파싱에 실패해도([linkStatus] = `invalid`) 이벤트는 발행한다.
     * 발행을 건너뛰면 «링크가 안 온 것»과 «와서 깨진 것»을 구분할 수 없다.
     */
    data class AppLinkOpened(
        val linkStatus: String,
        val feedId: Long?,
        val referrer: String?,
        val utmSource: String?,
        val utmMedium: String?,
        val utmCampaign: String?,
    ) : AnalyticsEvent() {
        companion object {
            const val STATUS_RESOLVED = "resolved"
            const val STATUS_INVALID = "invalid"
        }
    }

    /**
     * 더보기 메뉴에서 «공유하기»를 탭해 공유 시트를 띄움.
     *
     * 공유 시트에서 실제로 어떤 앱을 골랐는지, 보내기를 완료했는지는 **알 수 없다** —
     * 시스템 시트의 선택 결과가 앱으로 돌아오지 않는다. 이 이벤트는 «공유 의도»까지만 센다.
     * 실제 도달은 [AppLinkOpened]로 반대편에서 관측하고, 두 이벤트를 이어 붙여
     * «공유 → 유입» 퍼널을 만든다.
     *
     * @param isOwner 내 피드를 공유했는가. 표를 모으려는 공유와 남의 피드를 퍼뜨리는 공유는
     *   동기가 달라 분리해서 본다.
     */
    data class ShareClicked(
        val feedId: Long,
        val isOwner: Boolean,
    ) : AnalyticsEvent()
}
