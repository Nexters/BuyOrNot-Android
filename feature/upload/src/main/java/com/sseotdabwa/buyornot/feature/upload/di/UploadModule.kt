package com.sseotdabwa.buyornot.feature.upload.di

import com.sseotdabwa.buyornot.feature.upload.upload.ContentResolverFeedImageUploader
import com.sseotdabwa.buyornot.feature.upload.upload.FeedImageUploader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UploadModule {
    @Binds
    abstract fun bindFeedImageUploader(uploader: ContentResolverFeedImageUploader): FeedImageUploader
}
