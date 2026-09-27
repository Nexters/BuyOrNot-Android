package com.sseotdabwa.buyornot

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BuyOrNotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 릴리스에는 트리를 심지 않아 앱 로그가 logcat에 출력되지 않는다.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}
