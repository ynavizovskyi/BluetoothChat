package com.bluetoothchat.app

import com.bluetoothchat.app.file.ApkExtractorImpl
import com.bluetoothchat.core.domain.AppInfoProvider
import com.bluetoothchat.core.filemanager.ApkExtractor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AppDiBindModule {

    @Singleton
    @Binds
    abstract fun bindAppInfoProvider(provider: AppInfoProviderImpl): AppInfoProvider

    @Singleton
    @Binds
    abstract fun bindApkExtractor(extractor: ApkExtractorImpl): ApkExtractor


}
