package me.ash.reader.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import me.ash.reader.domain.data.ArticlePagingListUseCase
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.android.AndroidStringsHelper
import me.ash.reader.infrastructure.preference.SettingsProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ArticlePagingListModule {

    @Provides
    @Singleton
    fun providesUseCase(
        rssService: RssService,
        androidStringsHelper: AndroidStringsHelper,
        @ApplicationScope
        applicationScope: CoroutineScope,
        @IODispatcher
        ioDispatcher: CoroutineDispatcher,
        settingsProvider: SettingsProvider,
        diffMapHolder: DiffMapHolder,
    ): ArticlePagingListUseCase {
        return ArticlePagingListUseCase(
            rssService,
            androidStringsHelper,
            applicationScope,
            ioDispatcher,
            settingsProvider,
            diffMapHolder
        )
    }
}