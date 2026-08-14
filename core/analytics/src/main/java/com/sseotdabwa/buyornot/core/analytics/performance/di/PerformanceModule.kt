package com.sseotdabwa.buyornot.core.analytics.performance.di

import com.google.firebase.perf.FirebasePerformance
import com.sseotdabwa.buyornot.core.analytics.performance.FirebasePerformanceTracer
import com.sseotdabwa.buyornot.core.analytics.performance.Performance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PerformanceModule {
    @Provides
    @Singleton
    fun providePerformance(): Performance = FirebasePerformanceTracer(FirebasePerformance.getInstance())
}
