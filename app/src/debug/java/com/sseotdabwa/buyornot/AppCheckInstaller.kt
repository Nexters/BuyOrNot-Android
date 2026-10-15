package com.sseotdabwa.buyornot

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

// 콘솔 App Check > 디버그 토큰에 logcat의 "DebugAppCheckProvider" 토큰을 등록해야 강제 적용 시에도 호출된다.
internal fun installAppCheck() {
    Firebase.appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
}
