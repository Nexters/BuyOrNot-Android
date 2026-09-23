package com.sseotdabwa.buyornot.core.analytics.di

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.sseotdabwa.buyornot.core.analytics.Analytics
import com.sseotdabwa.buyornot.core.analytics.BuildConfig
import com.sseotdabwa.buyornot.core.analytics.DebugAnalytics
import com.sseotdabwa.buyornot.core.analytics.IdentityBufferingAnalytics
import com.sseotdabwa.buyornot.core.analytics.MixpanelAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {
    @Provides
    @Singleton
    fun provideAnalytics(
        @ApplicationContext context: Context,
    ): Analytics {
        val appVersion =
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
                ?: "unknown"
        val delegate =
            // qa 빌드도 debuggable이라 DEBUG로는 구분할 수 없다.
            if (!BuildConfig.SEND_TO_MIXPANEL) {
                DebugAnalytics(appVersion)
            } else {
                val mixpanel =
                    MixpanelAPI.getInstance(
                        context,
                        BuildConfig.MIXPANEL_TOKEN,
                        true,
                    )
                MixpanelAnalytics(mixpanel, appVersion)
            }
        return IdentityBufferingAnalytics(delegate)
    }
}
